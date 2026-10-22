package com.bsmi.attendancesystem.dao;

import com.bsmi.attendancesystem.UserData;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads and writes the {@code admin} table, which holds every login. Passwords are only ever
 * stored and read as bcrypt hashes.
 */
public final class UserDao {
    private final Database database;

    public UserDao(Database database) {
        this.database = database;
    }

    /** @return every user without their password hash, in the order they were added */
    public List<UserData> findAll() {
        String sql = "SELECT id, username, user_role FROM admin ORDER BY id";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rows = statement.executeQuery()) {
            List<UserData> users = new ArrayList<>();
            while (rows.next()) {
                users.add(toUser(rows));
            }
            return users;
        } catch (SQLException e) {
            throw new DataAccessException("load the users", e);
        }
    }

    public Optional<UserData> findById(int userId) {
        String sql = "SELECT id, username, user_role FROM admin WHERE id = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(toUser(rows)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("load user #" + userId, e);
        }
    }

    /** @return the user together with their password hash, for checking a login */
    public Optional<StoredUser> findWithPasswordHash(String username) {
        String sql = "SELECT id, username, user_role, password_hash FROM admin WHERE username = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next()
                        ? Optional.of(new StoredUser(toUser(rows), rows.getString("password_hash")))
                        : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("load user " + username, e);
        }
    }

    public Optional<Integer> findIdOf(String username) {
        String sql = "SELECT id FROM admin WHERE username = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(rows.getInt(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("check whether user " + username + " exists", e);
        }
    }

    public int countWithRole(String role) {
        String sql = "SELECT COUNT(*) FROM admin WHERE user_role = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("count the " + role + " users", e);
        }
    }

    /**
     * @throws DataAccessException also when the username is taken or a value breaks a column
     *     rule, e.g. an unknown role
     */
    public void add(String username, String passwordHash, String role) {
        String sql = "INSERT INTO admin (username, password_hash, user_role) VALUES (?, ?, ?)";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, role);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("add user " + username, e);
        }
    }

    /**
     * Changes the username, password and role of the user with the same id.
     *
     * @return false if no user has that id
     */
    public boolean update(UserData user, String passwordHash) {
        String sql = "UPDATE admin SET username = ?, password_hash = ?, user_role = ? WHERE id = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUserName());
            statement.setString(2, passwordHash);
            statement.setString(3, user.getUserRole());
            statement.setInt(4, user.getUserId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("update user #" + user.getUserId(), e);
        }
    }

    /** @return false if no user has that id */
    public boolean delete(int userId) {
        String sql = "DELETE FROM admin WHERE id = ?";
        try (Connection connection = database.open();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("delete user #" + userId, e);
        }
    }

    private static UserData toUser(ResultSet rows) throws SQLException {
        return new UserData(
                rows.getInt("id"), rows.getString("username"), rows.getString("user_role"));
    }

    /** A user as stored, including the bcrypt hash of their password. */
    public record StoredUser(UserData user, String passwordHash) {}
}
