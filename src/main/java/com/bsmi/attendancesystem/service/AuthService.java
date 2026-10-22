package com.bsmi.attendancesystem.service;

import static com.bsmi.attendancesystem.service.Fields.requireFilled;
import static com.bsmi.attendancesystem.service.Fields.trimmedOrEmpty;

import com.bsmi.attendancesystem.UserData;
import com.bsmi.attendancesystem.dao.UserDao;
import java.util.Optional;

/** Checks a username and password on the login screen. */
public final class AuthService {
    private final UserDao users;
    private final Passwords passwords;

    public AuthService(UserDao users) {
        this(users, new Passwords());
    }

    AuthService(UserDao users, Passwords passwords) {
        this.users = users;
        this.passwords = passwords;
    }

    /**
     * An unknown username and a wrong password give the same empty result, so the login screen
     * can't reveal which usernames exist.
     *
     * @return the user, if the password is theirs
     * @throws MissingFieldException if the username or password is empty
     */
    public Optional<UserData> login(String username, String password) {
        requireFilled(username, UserService.USERNAME);
        requireFilled(password, UserService.PASSWORD);
        return users.findWithPasswordHash(trimmedOrEmpty(username))
                .filter(stored -> passwords.matches(password, stored.passwordHash()))
                .map(UserDao.StoredUser::user);
    }
}
