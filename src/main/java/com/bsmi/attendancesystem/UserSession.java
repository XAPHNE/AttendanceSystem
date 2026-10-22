package com.bsmi.attendancesystem;

import com.bsmi.attendancesystem.service.UserService;

/**
 * Remembers who is logged in, from a successful login until logout. The login and dashboard
 * screens are separate FXML controllers, so this is how the dashboard knows the user.
 */
public final class UserSession {
    private static UserData current;

    private UserSession() {}

    public static void start(UserData user) {
        current = user;
    }

    /** @throws IllegalStateException if nobody is logged in */
    public static UserData current() {
        if (current == null) {
            throw new IllegalStateException("Nobody is logged in");
        }
        return current;
    }

    public static boolean isAdmin() {
        return current != null && UserService.ADMIN.equals(current.getUserRole());
    }

    public static void end() {
        current = null;
    }
}
