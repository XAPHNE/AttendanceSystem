package com.bsmi.attendancesystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bsmi.attendancesystem.UserData;
import com.bsmi.attendancesystem.dao.DataAccessException;
import com.bsmi.attendancesystem.dao.Database;
import com.bsmi.attendancesystem.dao.TestDatabases;
import com.bsmi.attendancesystem.dao.UserDao;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserServiceTest {
    private static final String STRONG = "Secret123";

    private Database database;
    private UserDao users;
    private Passwords passwords;
    private UserService service;
    private int adminId;
    private int employeeId;

    @BeforeEach
    void useFreshDatabase() {
        database = TestDatabases.migratedInMemory();
        users = new UserDao(database);
        passwords = new Passwords(4);
        service = new UserService(users, passwords);
        adminId = users.findIdOf("admin").orElseThrow();
        employeeId = users.findIdOf("employee").orElseThrow();
    }

    private static UserForm newUser(String username, String role) {
        return new UserForm(null, username, STRONG, STRONG, role);
    }

    private static UserForm changed(int id, String username, String role) {
        return new UserForm(id, username, STRONG, STRONG, role);
    }

    private boolean storedPasswordIs(String username, String password) {
        return passwords.matches(
                password, users.findWithPasswordHash(username).orElseThrow().passwordHash());
    }

    @Test
    void listAllReturnsStoredUsers() {
        assertThat(service.listAll()).extracting(UserData::getUserName).containsExactly("admin", "employee");
    }

    @Test
    void addStoresTrimmedUsernameAndHashedPassword() {
        service.add(newUser("  clerk ", "Employee"));

        assertThat(users.findWithPasswordHash("clerk").orElseThrow().passwordHash())
                .isNotEqualTo(STRONG);
        assertThat(storedPasswordIs("clerk", STRONG)).isTrue();
    }

    @Test
    void addWithTakenUsernameIsRejected() {
        assertThatThrownBy(() -> service.add(newUser(" admin ", "Employee")))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("User admin already exists");
    }

    @Test
    void addStoresInjectionTextLiterally() {
        service.add(newUser("x' OR '1'='1", "Employee"));

        assertThat(service.listAll()).hasSize(3);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankUsernameNamesTheUsernameField(String blank) {
        assertThatThrownBy(() -> service.add(newUser(blank, "Admin")))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the Username field");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void blankPasswordNamesThePasswordField(String blank) {
        assertThatThrownBy(() -> service.add(new UserForm(null, "new", blank, STRONG, "Admin")))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the Password field");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void blankConfirmationNamesTheConfirmPasswordField(String blank) {
        assertThatThrownBy(() -> service.add(new UserForm(null, "new", STRONG, blank, "Admin")))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the Confirm Password field");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void missingRoleNamesTheUserRoleField(String blank) {
        assertThatThrownBy(() -> service.add(newUser("new", blank)))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the User Role field");
    }

    @Test
    void unknownRoleIsRejected() {
        assertThatThrownBy(() -> service.add(newUser("new", "Boss")))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("User Role must be Admin or Employee");
    }

    @Test
    void usernameLongerThanTheColumnIsRejected() {
        assertThatThrownBy(() -> service.add(newUser("u".repeat(46), "Admin")))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("Username must be at most 45 characters long");
    }

    @Test
    void usernameOfExactlyColumnSizeIsAccepted() {
        assertThatCode(() -> service.add(newUser("u".repeat(45), "Admin"))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Short1", "alllowercase", "ALLUPPERCASE", "12345678", "Abcdefg"})
    void weakPasswordIsRejected(String weak) {
        assertThatThrownBy(() -> service.add(new UserForm(null, "new", weak, weak, "Admin")))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage(
                        "Password must be at least 8 characters long and contain both uppercase"
                                + " and lowercase letters");
    }

    @Test
    void eightCharactersWithBothCasesIsStrongEnough() {
        assertThatCode(() -> service.add(new UserForm(null, "new", "Abcdefgh", "Abcdefgh", "Admin")))
                .doesNotThrowAnyException();
    }

    @Test
    void differentConfirmationIsRejected() {
        assertThatThrownBy(
                        () -> service.add(new UserForm(null, "new", STRONG, "Secret124", "Admin")))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("Confirm Password must be the same as Password");
    }

    @Test
    void updateChangesUsernameRoleAndPassword() {
        service.update(new UserForm(employeeId, " clerk ", "NewPass99", "NewPass99", "Admin"));

        UserData stored = users.findById(employeeId).orElseThrow();
        assertThat(stored.getUserName()).isEqualTo("clerk");
        assertThat(stored.getUserRole()).isEqualTo("Admin");
        assertThat(storedPasswordIs("clerk", "NewPass99")).isTrue();
    }

    @Test
    void updateMayKeepTheSameUsername() {
        assertThatCode(() -> service.update(changed(employeeId, "employee", "Employee")))
                .doesNotThrowAnyException();
    }

    @Test
    void updateToAnotherUsersNameIsRejected() {
        assertThatThrownBy(() -> service.update(changed(employeeId, "admin", "Employee")))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("User admin already exists");
    }

    @Test
    void requireChosenReturnsTheStoredUser() {
        assertThat(service.requireChosen(adminId).getUserName()).isEqualTo("admin");
    }

    @Test
    void requireChosenWithoutChosenUserIsRejected() {
        assertThatThrownBy(() -> service.requireChosen(null))
                .isInstanceOf(NotSelectedException.class);
    }

    @Test
    void requireChosenOfDeletedUserIsRejected() {
        assertThatThrownBy(() -> service.requireChosen(9999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateWithoutChosenUserIsRejected() {
        assertThatThrownBy(() -> service.update(new UserForm(null, "x", STRONG, STRONG, "Admin")))
                .isInstanceOf(NotSelectedException.class)
                .hasMessage("Please choose a user in the table first");
    }

    @Test
    void updateOfDeletedUserIsRejected() {
        users.delete(employeeId);

        assertThatThrownBy(() -> service.update(changed(employeeId, "x", "Employee")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("The chosen user does not exist");
    }

    @Test
    void updateChecksTheFormToo() {
        assertThatThrownBy(() -> service.update(changed(employeeId, "", "Employee")))
                .isInstanceOf(MissingFieldException.class);
    }

    @Test
    void onlyAdminCannotBecomeAnEmployee() {
        assertThatThrownBy(() -> service.update(changed(adminId, "admin", "Employee")))
                .isInstanceOf(NotAllowedException.class)
                .hasMessage("User admin is the only Admin, so it must stay one");
    }

    @Test
    void adminCanBecomeAnEmployeeWhenAnotherAdminExists() {
        service.update(changed(employeeId, "employee", "Admin"));

        service.update(changed(adminId, "admin", "Employee"));

        assertThat(users.findById(adminId).orElseThrow().getUserRole()).isEqualTo("Employee");
    }

    @Test
    void deleteRemovesTheUser() {
        service.delete(employeeId, "admin");

        assertThat(users.findById(employeeId)).isEmpty();
    }

    @Test
    void deleteWithoutChosenUserIsRejected() {
        assertThatThrownBy(() -> service.delete(null, "admin"))
                .isInstanceOf(NotSelectedException.class);
    }

    @Test
    void deleteOfDeletedUserIsRejected() {
        users.delete(employeeId);

        assertThatThrownBy(() -> service.delete(employeeId, "admin"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void loggedInUserCannotDeleteThemselves() {
        service.update(changed(employeeId, "employee", "Admin"));

        assertThatThrownBy(() -> service.delete(employeeId, "employee"))
                .isInstanceOf(NotAllowedException.class)
                .hasMessage("You can't delete the user you are logged in as");
    }

    @Test
    void onlyAdminCannotBeDeleted() {
        assertThatThrownBy(() -> service.delete(adminId, "someoneElse"))
                .isInstanceOf(NotAllowedException.class)
                .hasMessage("User admin is the only Admin, so it can't be deleted");
    }

    @Test
    void adminCanBeDeletedWhenAnotherAdminExists() throws SQLException {
        TestDatabases.execute(database, "UPDATE admin SET user_role = 'Admin'");

        service.delete(adminId, "employee");

        assertThat(service.listAll()).extracting(UserData::getUserName).containsExactly("employee");
    }

    @Test
    void unreachableDatabaseIsReported() {
        UserService broken = new UserService(new UserDao(TestDatabases.unreachable()));

        assertThatThrownBy(broken::listAll).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.add(newUser("new", "Admin")))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> broken.delete(1, "admin")).isInstanceOf(DataAccessException.class);
    }
}
