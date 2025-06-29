package com.carmanagement.carmanagement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VehicleDAO {
    private static final Logger logger = Logger.getLogger(VehicleDAO.class.getName());

    // Database configuration
    private final String jdbcURL = "jdbc:mysql://localhost:3306/carservicetracker";
    private final String jdbcUsername = "root";
    private final String jdbcPassword = "12345678";

    // SQL queries
    private static final String INSERT_VEHICLE_SQL = "INSERT INTO vehicles (numberplate, registereddistrict, enginenumber, enginecapacity, vehicletype, owner_nic) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_VEHICLES_BY_OWNER = "SELECT * FROM vehicles WHERE owner_nic = ?";
    private static final String SELECT_VEHICLE_BY_PLATE = "SELECT * FROM vehicles WHERE numberplate = ?";
    private static final String DELETE_VEHICLE_SQL = "DELETE FROM vehicles WHERE numberplate = ?";
    private static final String CHECK_USER_EXISTS = "SELECT 1 FROM users WHERE nic = ? LIMIT 1";
    private static final String CHECK_PLATE_EXISTS = "SELECT 1 FROM vehicles WHERE numberplate = ? LIMIT 1";

    /**
     * Establishes database connection with enhanced error handling
     */
    protected Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
            connection.setAutoCommit(true);
            logger.info("Database connection established successfully");
            return connection;
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to establish database connection", e);
            throw e;
        }
    }

    /**
     * Checks if vehicle with given number plate already exists
     */
    public boolean plateExists(String numberplate) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CHECK_PLATE_EXISTS)) {

            preparedStatement.setString(1, numberplate);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking plate existence: " + numberplate, e);
            throw e;
        }
    }

    /**
     * Inserts a new vehicle with validation checks
     */
    public boolean insertVehicle(Vehicle vehicle) throws SQLException {
        logger.info("Attempting to insert vehicle: " + vehicle.toString());

        // Validate owner exists first
        if (!userExists(vehicle.getNic())) {
            logger.warning("Attempt to register vehicle for non-existent user: " + vehicle.getNic());
            throw new SQLException("Owner NIC does not exist in system");
        }

        // Check if plate already exists
        if (plateExists(vehicle.getNumberplate())) {
            logger.warning("Attempt to register duplicate number plate: " + vehicle.getNumberplate());
            throw new SQLIntegrityConstraintViolationException("Number plate already exists: " + vehicle.getNumberplate());
        }

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_VEHICLE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, vehicle.getNumberplate());
            preparedStatement.setString(2, vehicle.getRegistereddistrict());
            preparedStatement.setString(3, vehicle.getEnginenumber());
            preparedStatement.setInt(4, vehicle.getEnginecapacity());
            preparedStatement.setString(5, vehicle.getVehicletype());
            preparedStatement.setString(6, vehicle.getNic());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                logger.warning("No rows affected when inserting vehicle");
                return false;
            }

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    logger.info("Vehicle registered successfully with ID: " + generatedKeys.getLong(1));
                }
            }

            logger.info("Insert vehicle operation successful for: " + vehicle.getNumberplate());
            return true;
        } catch (SQLIntegrityConstraintViolationException e) {
            String errorMsg = "Constraint violation while inserting vehicle: " + e.getMessage();
            logger.log(Level.WARNING, errorMsg, e);
            throw new SQLIntegrityConstraintViolationException(errorMsg, e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error inserting vehicle", e);
            throw e;
        }
    }

    /**
     * Checks if user exists in the system
     */
    private boolean userExists(String nic) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(CHECK_USER_EXISTS)) {

            preparedStatement.setString(1, nic);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking user existence", e);
            throw e;
        }
    }

    /**
     * Retrieves vehicles by owner NIC
     */
    public List<Vehicle> selectVehiclesByOwner(String ownerNic) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        logger.info("Fetching vehicles for owner: " + ownerNic);

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_VEHICLES_BY_OWNER)) {

            preparedStatement.setString(1, ownerNic);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Vehicle vehicle = new Vehicle(
                            rs.getString("numberplate"),
                            rs.getString("registereddistrict"),
                            rs.getString("enginenumber"),
                            rs.getInt("enginecapacity"),
                            rs.getString("vehicletype"),
                            rs.getString("owner_nic")
                    );
                    vehicles.add(vehicle);
                    logger.fine("Found vehicle: " + vehicle.getNumberplate());
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching vehicles for owner: " + ownerNic, e);
            throw e;
        }

        logger.info("Found " + vehicles.size() + " vehicles for owner " + ownerNic);
        return vehicles;
    }

    /**
     * Retrieves a single vehicle by number plate
     */
    public Vehicle selectVehicleByPlate(String numberplate) throws SQLException {
        logger.info("Looking up vehicle by plate: " + numberplate);

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_VEHICLE_BY_PLATE)) {

            preparedStatement.setString(1, numberplate);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    Vehicle vehicle = new Vehicle(
                            rs.getString("numberplate"),
                            rs.getString("registereddistrict"),
                            rs.getString("enginenumber"),
                            rs.getInt("enginecapacity"),
                            rs.getString("vehicletype"),
                            rs.getString("owner_nic")
                    );
                    logger.info("Found vehicle: " + vehicle.toString());
                    return vehicle;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error looking up vehicle: " + numberplate, e);
            throw e;
        }

        logger.info("No vehicle found with plate: " + numberplate);
        return null;
    }

    /**
     * Deletes a vehicle by number plate
     */
    public boolean deleteVehicle(String numberplate) throws SQLException {
        logger.info("Attempting to delete vehicle: " + numberplate);

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_VEHICLE_SQL)) {

            statement.setString(1, numberplate);
            int rowsAffected = statement.executeUpdate();
            logger.info("Delete operation affected " + rowsAffected + " rows");

            if (rowsAffected > 0) {
                logger.info("Successfully deleted vehicle: " + numberplate);
                return true;
            } else {
                logger.warning("No vehicle found to delete with plate: " + numberplate);
                return false;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting vehicle: " + numberplate, e);
            throw e;
        }
    }
}