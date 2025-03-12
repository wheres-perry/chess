package dataaccess.implementations;

import dataaccess.DataAccessException;
import dataaccess.interfaces.UserDAO;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class MySQLUserDAO implements UserDAO, AutoCloseable {
    private final Connection connection;

    public MySQLUserDAO() throws DataAccessException {
        connection = DatabaseManager.getConnection();
        createUserTable();
    }

    private void createUserTable() throws DataAccessException {
        try {
            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS users (
                        username VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        PRIMARY KEY (username)
                    )
                    """;

            try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user table: " + e.getMessage());
        }
    }

    @Override
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public boolean checkPassword(String password, String hash) {
        return BCrypt.checkpw(password, hash);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null || user.password() == null || user.email() == null) {
            throw new DataAccessException("Invalid user data");
        }

        try {
            String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

            String hashedPassword = hashPassword(user.password());

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user.username());
                stmt.setString(2, hashedPassword);
                stmt.setString(3, user.email());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating user: " + e.getMessage());
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username == null || username.isEmpty()) {
            return null;
        }

        try {
            String sql = "SELECT username, password, email FROM users WHERE username = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email"));
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user: " + e.getMessage());
        }
    }

    @Override
    public boolean verifyPassword(String username, String password) throws DataAccessException {
        UserData user = getUser(username);

        if (user == null) {
            return false;
        }

        return checkPassword(password, user.password());
    }

    @Override
    public void clear() throws DataAccessException {
        try {
            String sql = "DELETE FROM users";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing users: " + e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}