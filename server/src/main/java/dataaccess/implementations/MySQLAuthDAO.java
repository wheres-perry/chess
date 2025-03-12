package dataaccess.implementations;

import dataaccess.interfaces.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

import java.sql.*;
import java.util.UUID;

public class MySQLAuthDAO implements AuthDAO, AutoCloseable {
    private final Connection connection;

    public MySQLAuthDAO() throws DataAccessException {
        connection = DatabaseManager.getConnection();
        createAuthTable();
    }

    private void createAuthTable() throws DataAccessException {
        try {
            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS auth (
                        auth_token VARCHAR(255) NOT NULL,
                        username VARCHAR(255) NOT NULL,
                        PRIMARY KEY (auth_token)
                    )
                    """;

            try (PreparedStatement stmt = connection.prepareStatement(createTableSQL)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth table: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try {
            String sql = "DELETE FROM auth";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auth data: " + e.getMessage());
        }
    }

    @Override
    public String createAuth(String username) throws DataAccessException {
        if (username == null || username.isEmpty()) {
            throw new DataAccessException("Username cannot be empty");
        }

        String authToken = UUID.randomUUID().toString();

        try {
            String sql = "INSERT INTO auth (auth_token, username) VALUES (?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, authToken);
                stmt.setString(2, username);
                stmt.executeUpdate();
            }

            return authToken;
        } catch (SQLException e) {
            throw new DataAccessException("Error creating auth token: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("Auth token cannot be empty");
        }

        try {
            String sql = "DELETE FROM auth WHERE auth_token = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, authToken);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth token: " + e.getMessage());
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            return null;
        }

        try {
            String sql = "SELECT auth_token, username FROM auth WHERE auth_token = ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, authToken);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(
                                rs.getString("auth_token"),
                                rs.getString("username"));
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving auth data: " + e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}