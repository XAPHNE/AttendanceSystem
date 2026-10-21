package com.bsmi.attendancesystem.service;

import static com.bsmi.attendancesystem.service.Fields.requireFilled;
import static com.bsmi.attendancesystem.service.Fields.trimmedOrEmpty;

import com.bsmi.attendancesystem.UserData;
import com.bsmi.attendancesystem.dao.UserDao;
import java.util.List;

/**
 * The rules for the Manage Users screen: usernames are unique, passwords are strong and typed
 * twice, and the app always keeps at least one Admin and never deletes the user who is logged in.
 */
public final class UserService {
    public static final String ADMIN = "Admin";
    public static final String EMPLOYEE = "Employee";
    public static final List<String> ROLES = List.of(ADMIN, EMPLOYEE);

    static final String USERNAME = "Username";
    static final String PASSWORD = "Password";
    static final String CONFIRM_PASSWORD = "Confirm Password";
    static final String ROLE = "User Role";

    /** The size of the {@code admin.username} column. */
    private static final int MAX_USERNAME_LENGTH = 45;

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserDao users;
    private final Passwords passwords;

    public UserService(UserDao users) {
        this(users, new Passwords());
    }

    UserService(UserDao users, Passwords passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    public List<UserData> listAll() {
        return users.findAll();
    }

    /**
     * Lets the UI check the form before asking the user to confirm.
     *
     * @throws MissingFieldException naming the first empty field
     * @throws InvalidValueException if the username is too long, the role is unknown, the
     *     password is weak or the two passwords differ
     */
    public void requireValid(UserForm form) {
        String username = trimmedOrEmpty(form.username());
        requireFilled(username, USERNAME);
        requireFilled(form.password(), PASSWORD);
        requireFilled(form.confirmPassword(), CONFIRM_PASSWORD);
        requireFilled(form.role(), ROLE);
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new InvalidValueException(
                    USERNAME, "at most " + MAX_USERNAME_LENGTH + " characters long");
        }
        if (!ROLES.contains(form.role())) {
            throw new InvalidValueException(ROLE, String.join(" or ", ROLES));
        }
        if (!isStrong(form.password())) {
            throw new InvalidValueException(
                    PASSWORD,
                    "at least "
                            + MIN_PASSWORD_LENGTH
                            + " characters long and contain both uppercase and lowercase"
                            + " letters");
        }
        if (!form.password().equals(form.confirmPassword())) {
            throw new InvalidValueException(CONFIRM_PASSWORD, "the same as " + PASSWORD);
        }
    }

    /** @throws AlreadyExistsException if the username is taken */
    public void add(UserForm form) {
        requireValid(form);
        String username = trimmedOrEmpty(form.username());
        if (users.findIdOf(username).isPresent()) {
            throw new AlreadyExistsException(describe(username));
        }
        users.add(username, passwords.hash(form.password()), form.role());
    }

    /**
     * Replaces the chosen user's username, password and role.
     *
     * @throws NotSelectedException if no user is chosen
     * @throws NotFoundException if the chosen user was deleted meanwhile
     * @throws AlreadyExistsException if another user has the new username
     * @throws NotAllowedException if this would turn the only Admin into an Employee
     */
    public void update(UserForm form) {
        UserData current = requireChosen(form.userId());
        requireValid(form);
        String username = trimmedOrEmpty(form.username());
        users.findIdOf(username)
                .filter(owner -> !owner.equals(current.getUserId()))
                .ifPresent(
                        other -> {
                            throw new AlreadyExistsException(describe(username));
                        });
        if (isOnlyAdmin(current) && !ADMIN.equals(form.role())) {
            throw new NotAllowedException(
                    describe(current.getUserName()) + " is the only Admin, so it must stay one");
        }
        UserData changed = new UserData(current.getUserId(), username, form.role());
        if (!users.update(changed, passwords.hash(form.password()))) {
            throw new NotFoundException(describe(current.getUserName()));
        }
    }

    /**
     * @param loggedInUsername the user of this session, who can't delete themselves
     * @throws NotSelectedException if no user is chosen
     * @throws NotFoundException if the chosen user was deleted meanwhile
     * @throws NotAllowedException if this is the logged-in user or the only Admin
     */
    public void delete(Integer userId, String loggedInUsername) {
        UserData user = requireChosen(userId);
        if (user.getUserName().equals(loggedInUsername)) {
            throw new NotAllowedException("You can't delete the user you are logged in as");
        }
        if (isOnlyAdmin(user)) {
            throw new NotAllowedException(
                    describe(user.getUserName()) + " is the only Admin, so it can't be deleted");
        }
        if (!users.delete(user.getUserId())) {
            throw new NotFoundException(describe(user.getUserName()));
        }
    }

    /**
     * Lets the UI check the chosen user before asking the user to confirm.
     *
     * @return the chosen user as stored
     * @throws NotSelectedException if no user is chosen
     * @throws NotFoundException if the chosen user was deleted meanwhile
     */
    public UserData requireChosen(Integer userId) {
        if (userId == null) {
            throw new NotSelectedException("user");
        }
        return users.findById(userId)
                .orElseThrow(() -> new NotFoundException("The chosen user"));
    }

    private boolean isOnlyAdmin(UserData user) {
        return ADMIN.equals(user.getUserRole()) && users.countWithRole(ADMIN) == 1;
    }

    private static boolean isStrong(String password) {
        return password.length() >= MIN_PASSWORD_LENGTH
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(Character::isLowerCase);
    }

    private static String describe(String username) {
        return "User " + username;
    }
}
