package com.bsmi.attendancesystem.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.bsmi.attendancesystem.UserData;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class UserDaoTest {
    private static final String INJECTION = "x' OR '1'='1";

    private Database database;
    private UserDao users;

    @BeforeEach
    void useFreshDatabase() {
        database = TestDatabases.migratedInMemory();
        users = new UserDao(database);
    }

    @Test
    void findAllReturnsDemoUsersInInsertOrder() {
        assertThat(users.findAll())
                .extracting(UserData::getUserName, UserData::getUserRole)
                .containsExactly(
                        tuple("admin", "Admin"),
                        tuple("employee", "Employee"));
    }

    @Test
    void findAllOnEmptyTableReturnsEmptyList() throws SQLException {
        TestDatabases.execute(database, "DELETE FROM admin");

        assertThat(users.findAll()).isEmpty();
    }

    @Test
    void findByIdReturnsTheUser() {
        int adminId = users.findIdOf("admin").orElseThrow();

        assertThat(users.findById(adminId)).get().extracting(UserData::getUserName).isEqualTo("admin");
    }

    @Test
    void findByIdOfUnknownIdIsEmpty() {
        assertThat(users.findById(9999)).isEmpty();
    }

    @Test
    void findWithPasswordHashReturnsTheBcryptHash() {
        UserDao.StoredUser stored = users.findWithPasswordHash("admin").orElseThrow();

        assertThat(stored.user().getUserRole()).isEqualTo("Admin");
        assertThat(stored.passwordHash()).startsWith("$2a$12$");
    }

    @Test
    void findWithPasswordHashOfUnknownUserIsEmpty() {
        assertThat(users.findWithPasswordHash("nobody")).isEmpty();
    }

    @Test
    void findWithPasswordHashTreatsInjectionAsPlainText() {
        assertThat(users.findWithPasswordHash(INJECTION)).isEmpty();
    }

    @Test
    void findIdOfUnknownUserIsEmpty() {
        assertThat(users.findIdOf("nobody")).isEmpty();
    }

    @Test
    void countWithRoleCountsOnlyThatRole() {
        users.add("second", "hash", "Admin");

        assertThat(users.countWithRole("Admin")).isEqualTo(2);
        assertThat(users.countWithRole("Employee")).isEqualTo(1);
        assertThat(users.countWithRole("Unknown")).isZero();
    }

    @Test
    void addedUserIsStoredWithItsHash() {
        users.add(INJECTION, "hash", "Employee");

        UserDao.StoredUser stored = users.findWithPasswordHash(INJECTION).orElseThrow();
        assertThat(stored.passwordHash()).isEqualTo("hash");
        assertThat(users.findAll()).hasSize(3);
    }

    @Test
    void addingTakenUsernameFails() {
        assertThatThrownBy(() -> users.add("admin", "hash", "Admin"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("add user admin");
    }

    @Test
    void addingUnknownRoleFails() {
        assertThatThrownBy(() -> users.add("new", "hash", "Boss"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void updateChangesEveryColumn() {
        int employeeId = users.findIdOf("employee").orElseThrow();

        boolean updated = users.update(new UserData(employeeId, "renamed", "Admin"), "newHash");

        assertThat(updated).isTrue();
        UserDao.StoredUser stored = users.findWithPasswordHash("renamed").orElseThrow();
        assertThat(stored.user().getUserId()).isEqualTo(employeeId);
        assertThat(stored.user().getUserRole()).isEqualTo("Admin");
        assertThat(stored.passwordHash()).isEqualTo("newHash");
        assertThat(users.findIdOf("employee")).isEmpty();
    }

    @Test
    void updateOfUnknownIdReturnsFalse() {
        assertThat(users.update(new UserData(9999, "x", "Admin"), "hash")).isFalse();
    }

    @Test
    void updateToTakenUsernameFails() {
        int employeeId = users.findIdOf("employee").orElseThrow();

        assertThatThrownBy(() -> users.update(new UserData(employeeId, "admin", "Admin"), "h"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("update user #" + employeeId);
    }

    @Test
    void deleteRemovesTheUser() {
        int employeeId = users.findIdOf("employee").orElseThrow();

        assertThat(users.delete(employeeId)).isTrue();
        assertThat(users.findById(employeeId)).isEmpty();
    }

    @Test
    void deleteOfUnknownIdReturnsFalse() {
        assertThat(users.delete(9999)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("com.bsmi.attendancesystem.dao.TestDatabases#broken")
    void everyMethodReportsABrokenDatabase(Database database) {
        UserDao broken = new UserDao(database);
        UserData user = new UserData(1, "x", "Admin");

        assertThatThrownBy(broken::findAll).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.findById(1)).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.findWithPasswordHash("x"))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.findIdOf("x")).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.countWithRole("Admin"))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.add("x", "h", "Admin"))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.update(user, "h")).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.delete(1)).isInstanceOf(DataAccessException.class);
    }
}
