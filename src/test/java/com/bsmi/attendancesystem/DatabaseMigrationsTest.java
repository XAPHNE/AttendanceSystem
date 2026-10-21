package com.bsmi.attendancesystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bsmi.attendancesystem.DatabaseMigrations.DatabaseSetupException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mindrot.jbcrypt.BCrypt;

class DatabaseMigrationsTest {
    private static final int MIGRATION_COUNT = 2;

    private DatabaseSettings settings;

    @BeforeEach
    void useFreshInMemoryDatabase() {
        // DB_CLOSE_DELAY keeps the database alive after Flyway closes its connection
        settings =
                new DatabaseSettings(
                        "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "admin", "admin");
    }

    @Test
    void freshDatabaseGetsEveryMigration() {
        assertThat(DatabaseMigrations.applyPending(settings)).isEqualTo(MIGRATION_COUNT);
    }

    @Test
    void secondRunAppliesNothingAndDoesNotReseed() throws SQLException {
        DatabaseMigrations.applyPending(settings);
        long studentsAfterFirstRun = count("SELECT COUNT(*) FROM student");

        assertThat(DatabaseMigrations.applyPending(settings)).isZero();
        assertThat(count("SELECT COUNT(*) FROM student")).isEqualTo(studentsAfterFirstRun);
    }

    @Test
    void demoAccountsLogInWithTheDocumentedPasswords() throws SQLException {
        DatabaseMigrations.applyPending(settings);

        assertThat(BCrypt.checkpw("admin123", passwordHashOf("admin"))).isTrue();
        assertThat(BCrypt.checkpw("employee123", passwordHashOf("employee"))).isTrue();
        assertThat(count("SELECT COUNT(*) FROM admin WHERE username = 'admin' AND user_role = 'Admin'"))
                .isOne();
    }

    @Test
    void demoAttendanceCoversTodayForEnrolledStudentsOnly() throws SQLException {
        DatabaseMigrations.applyPending(settings);

        long enrolled = count("SELECT COUNT(*) FROM student WHERE status = 'Enrolled'");
        assertThat(enrolled).isPositive();
        assertThat(count("SELECT COUNT(*) FROM student_attendance")).isEqualTo(enrolled * 5);
        assertThat(
                        count(
                                "SELECT COUNT(*) FROM student_attendance"
                                        + " WHERE CAST(entry_time AS DATE) = CURRENT_DATE"))
                .isEqualTo(enrolled);
        assertThat(
                        count(
                                "SELECT COUNT(*) FROM student_attendance a JOIN student s"
                                        + " ON s.id = a.student_id WHERE s.status <> 'Enrolled'"))
                .isZero();
    }

    @Test
    void fileDatabaseIsCreatedOnceAndReusedOnRestart(@TempDir Path dataDir) {
        DatabaseSettings fileSettings =
                new DatabaseSettings(DatabaseSettings.embeddedUrl(dataDir), "admin", "admin");

        assertThat(DatabaseMigrations.applyPending(fileSettings)).isEqualTo(MIGRATION_COUNT);
        assertThat(dataDir.resolve("attendance_system.mv.db")).exists();
        assertThat(DatabaseMigrations.applyPending(fileSettings)).isZero();
    }

    @Test
    void missingParentFoldersAreCreated(@TempDir Path tempDir) {
        Path dataDir = tempDir.resolve("not/yet/there");

        DatabaseMigrations.applyPending(
                new DatabaseSettings(DatabaseSettings.embeddedUrl(dataDir), "admin", "admin"));

        assertThat(dataDir.resolve("attendance_system.mv.db")).exists();
    }

    @Test
    void wrongPasswordForExistingDatabaseFailsWithSetupError(@TempDir Path dataDir) {
        String url = DatabaseSettings.embeddedUrl(dataDir);
        DatabaseMigrations.applyPending(new DatabaseSettings(url, "admin", "admin"));

        assertThatThrownBy(
                        () -> DatabaseMigrations.applyPending(new DatabaseSettings(url, "admin", "wrong")))
                .isInstanceOf(DatabaseSetupException.class)
                .hasMessageContaining(url)
                .hasCauseInstanceOf(FlywayException.class);
    }

    @Test
    void corruptDatabaseFileFailsWithSetupError(@TempDir Path dataDir) throws Exception {
        Files.writeString(dataDir.resolve("attendance_system.mv.db"), "this is not an H2 file");
        DatabaseSettings corrupt =
                new DatabaseSettings(DatabaseSettings.embeddedUrl(dataDir), "admin", "admin");

        assertThatThrownBy(() -> DatabaseMigrations.applyPending(corrupt))
                .isInstanceOf(DatabaseSetupException.class)
                .hasMessageContaining("attendance_system");
    }

    @Test
    void dataFolderBlockedByAFileFailsWithSetupError(@TempDir Path tempDir) throws Exception {
        Path blocker = Files.writeString(tempDir.resolve("blocker"), "a file, not a folder");
        DatabaseSettings blocked =
                new DatabaseSettings(DatabaseSettings.embeddedUrl(blocker), "admin", "admin");

        assertThatThrownBy(() -> DatabaseMigrations.applyPending(blocked))
                .isInstanceOf(DatabaseSetupException.class);
    }

    @Test
    void setupErrorNeverShowsThePassword(@TempDir Path dataDir) {
        String url = DatabaseSettings.embeddedUrl(dataDir);
        DatabaseMigrations.applyPending(new DatabaseSettings(url, "admin", "admin"));

        assertThatThrownBy(
                        () ->
                                DatabaseMigrations.applyPending(
                                        new DatabaseSettings(url, "admin", "leaked-secret")))
                .message()
                .doesNotContain("leaked-secret");
    }

    @Test
    void schemaRejectsUnknownRole() {
        DatabaseMigrations.applyPending(settings);

        assertThatThrownBy(
                        () ->
                                execute(
                                        "INSERT INTO admin (username, password_hash, user_role)"
                                                + " VALUES ('root', 'x', 'Root')"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    void schemaRejectsDuplicateUsername() {
        DatabaseMigrations.applyPending(settings);

        assertThatThrownBy(
                        () ->
                                execute(
                                        "INSERT INTO admin (username, password_hash, user_role)"
                                                + " VALUES ('admin', 'x', 'Admin')"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    void schemaRejectsDuplicateStudentNumber() {
        DatabaseMigrations.applyPending(settings);

        assertThatThrownBy(
                        () ->
                                execute(
                                        "INSERT INTO student (studentNum, semester, course, firstName,"
                                                + " lastName, gender, birthDate, status, image, added_on)"
                                                + " VALUES (1001, '1st Sem', 'BCA', 'A', 'B', 'Male',"
                                                + " DATE '2005-01-01', 'Enrolled', '', CURRENT_DATE)"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    void schemaRejectsUnknownAttendanceStatus() {
        DatabaseMigrations.applyPending(settings);

        assertThatThrownBy(() -> execute("UPDATE student_attendance SET status = 'Late'"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    void schemaRejectsAttendanceForMissingStudent() {
        DatabaseMigrations.applyPending(settings);

        assertThatThrownBy(
                        () ->
                                execute(
                                        "INSERT INTO student_attendance (student_id, course_id)"
                                                + " VALUES (999999, 1)"))
                .isInstanceOf(SQLException.class);
    }

    @Test
    void deletingStudentDeletesTheirAttendance() throws SQLException {
        DatabaseMigrations.applyPending(settings);

        execute("DELETE FROM student WHERE studentNum = 1001");

        assertThat(
                        count(
                                "SELECT COUNT(*) FROM student_attendance a LEFT JOIN student s"
                                        + " ON s.id = a.student_id WHERE s.id IS NULL"))
                .isZero();
        assertThat(count("SELECT COUNT(*) FROM student_attendance")).isPositive();
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(settings.url(), settings.user(), settings.password());
    }

    private long count(String sql) throws SQLException {
        try (Connection connection = open();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return result.getLong(1);
        }
    }

    private String passwordHashOf(String username) throws SQLException {
        try (Connection connection = open();
                var statement =
                        connection.prepareStatement("SELECT password_hash FROM admin WHERE username = ?")) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getString(1);
            }
        }
    }

    private void execute(String sql) throws SQLException {
        try (Connection connection = open();
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
