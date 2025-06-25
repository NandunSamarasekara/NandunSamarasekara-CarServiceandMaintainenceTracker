package com.usermanagement.usermanagement;

import com.usermanagement.usermanagement.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());

    private final String jdbcURL = "jdbc:mysql://localhost:3306/car_service_db";
    private final String jdbcUsername = "root";
    private final String jdbcPassword = "yourpassword";

    // SQL queries
    private static final String INSERT_USERS_SQL = "INSERT INTO users (nic, first_name, last_name, email, phone, password) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_USER_BY_NIC = "SELECT nic, first_name, last_name, email, phone, password FROM users WHERE nic = ?";
    private static final String SELECT_ALL_USERS = "SELECT nic, first_name, last_name, email, phone FROM users"; // Removed password from select all
    private static final String DELETE_USERS_SQL = "DELETE FROM users WHERE nic = ?";
    private static final String UPDATE_USERS_SQL = "UPDATE users SET first_name = ?, last_name = ?, email = ?, phone = ? WHERE nic = ?";
    private static final String UPDATE_PASSWORD_SQL = "UPDATE users SET password = ? WHERE nic = ?";
    private static final String CHECK_NIC_EXISTS = "SELECT 1 FROM users WHERE nic = ?";
    private static final String CHECK_EMAIL_EXISTS = "SELECT 1 FROM users WHERE email = ?";

    protected Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
            throw new SQLException("Database driver not found", e);
        }
    }

    // Create user
    public boolean insertUser(User user) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USERS_SQL)) {

            preparedStatement.setString(1, user.getNIC());
            preparedStatement.setString(2, user.getFist_name());
            preparedStatement.setString(3, user.getLast_name());
            preparedStatement.setString(4, user.getEmail());
            preparedStatement.setString(5, user.getPhone());
            preparedStatement.setString(6, user.getPassword());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Update user (without password)
    public boolean updateUser(User user) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_USERS_SQL)) {

            statement.setString(1, user.getFist_name());
            statement.setString(2, user.getLast_name());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getNIC());

            return statement.executeUpdate() > 0;
        }
    }

    // Update password separately for security
    public boolean updatePassword(String nic, String newPassword) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD_SQL)) {

            statement.setString(1, newPassword);
            statement.setString(2, nic);

            return statement.executeUpdate() > 0;
        }
    }

    // Select user by NIC
    public User selectUser(String nic) throws SQLException {
        User user = null;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_NIC)) {

            preparedStatement.setString(1, nic);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    user = new User(
                            rs.getString("nic"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("password") // Note: In production, passwords should be hashed
                    );
                }
            }
        }
        return user;
    }

    // Select all users (without passwords)
    public List<User> selectAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS);
             ResultSet rs = preparedStatement.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getString("nic"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        null // Don't return passwords in list operations
                ));
            }
        }
        return users;
    }

    // Delete user
    public boolean deleteUser(String nic) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_USERS_SQL)) {

            statement.setString(1, nic);
            return statement.executeUpdate() > 0;
        }
    }

    // Check if NIC exists
    public boolean isNicExists(String nic) throws SQLException {
        return checkExists(CHECK_NIC_EXISTS, nic);
    }

    // Check if email exists
    public boolean isEmailExists(String email) throws SQLException {
        return checkExists(CHECK_EMAIL_EXISTS, email);
    }

    // Helper method for existence checks
    private boolean checkExists(String query, String value) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, value);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Validate user credentials
    public boolean validateUser(String nic, String password) throws SQLException {
        String sql = "SELECT password FROM users WHERE nic = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nic);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    // In production, use password hashing comparison here
                    return storedPassword.equals(password);
                }
                return false;
            }
        }
    }
}