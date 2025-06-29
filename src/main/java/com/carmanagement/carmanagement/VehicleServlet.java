package com.carmanagement.carmanagement;

import com.google.gson.Gson;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "VehicleServlet", value = "/vehicle")
public class VehicleServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(VehicleServlet.class.getName());
    private VehicleDAO vehicleDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() {
        try {
            vehicleDAO = new VehicleDAO();
            logger.info("VehicleServlet initialized successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize VehicleDAO", e);
            throw new RuntimeException("Failed to initialize servlet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("nic") == null) {
                logger.warning("Unauthorized access attempt");
                sendErrorResponse(response, "Unauthorized - Please login", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String ownerNic = (String) session.getAttribute("nic");
            String action = request.getParameter("action");

            if (action == null || action.isEmpty()) {
                logger.warning("Missing action parameter");
                sendErrorResponse(response, "Action parameter is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            switch (action.toLowerCase()) {
                case "add":
                    handleAddVehicle(request, response, ownerNic);
                    break;
                case "list":
                    handleListVehicles(response, ownerNic);
                    break;
                case "delete":
                    handleDeleteVehicle(request, response, ownerNic);
                    break;
                case "get":
                    handleGetVehicle(request, response, ownerNic);
                    break;
                case "checkplate":
                    handleCheckPlate(request, response);
                    break;
                default:
                    logger.warning("Invalid action requested: " + action);
                    sendErrorResponse(response, "Invalid action", HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in doPost", e);
            sendErrorResponse(response, "Internal server error: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String path = request.getServletPath();
            if (path != null && path.endsWith("myvehicles.jsp")) {
                HttpSession session = request.getSession(false);
                if (session == null || session.getAttribute("nic") == null) {
                    logger.warning("Unauthorized access attempt to myvehicles.jsp");
                    response.sendRedirect("login.jsp");
                    return;
                }

                String ownerNic = (String) session.getAttribute("nic");
                List<Vehicle> vehicles = vehicleDAO.selectVehiclesByOwner(ownerNic);
                request.setAttribute("vehicles", vehicles);
                request.getRequestDispatcher("/myvehicles.jsp").forward(request, response);
            } else {
                logger.warning("Invalid GET request to vehicle endpoint");
                response.setContentType("application/json");
                sendErrorResponse(response, "GET method not supported for this endpoint",
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in doGet", e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleAddVehicle(HttpServletRequest request, HttpServletResponse response, String ownerNic)
            throws IOException {
        try {
            // Validate and parse parameters
            String numberplate = validateParameter(request, "numberplate");
            String registereddistrict = validateParameter(request, "registereddistrict");
            String enginenumber = validateParameter(request, "enginenumber");
            int enginecapacity = Integer.parseInt(validateParameter(request, "enginecapacity"));
            String vehicletype = validateParameter(request, "vehicletype");

            // Additional validation
            if (numberplate.length() < 3 || numberplate.length() > 15) {
                throw new IllegalArgumentException("Number plate must be 3-15 characters");
            }
            if (enginecapacity <= 0) {
                throw new IllegalArgumentException("Engine capacity must be positive");
            }

            Vehicle vehicle = new Vehicle(
                    numberplate,
                    registereddistrict,
                    enginenumber,
                    enginecapacity,
                    vehicletype,
                    ownerNic
            );

            logger.info("Attempting to register vehicle: " + vehicle.toString());

            boolean success = vehicleDAO.insertVehicle(vehicle);
            if (success) {
                logger.info("Vehicle registered successfully: " + numberplate);
                sendSuccessResponse(response, "Vehicle added successfully");
            } else {
                logger.warning("Failed to register vehicle: " + numberplate);
                sendErrorResponse(response, "Failed to add vehicle - please try again",
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            String errorMsg = "Number plate already exists";
            if (e.getMessage().contains("owner_nic")) {
                errorMsg = "Owner not found in system";
            }
            logger.warning("Constraint violation: " + errorMsg);
            sendErrorResponse(response, errorMsg, HttpServletResponse.SC_CONFLICT);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error: " + e.getMessage(), e);
            sendErrorResponse(response, "Database error: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error: " + e.getMessage(), e);
            sendErrorResponse(response, "Registration failed: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleCheckPlate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            String numberplate = validateParameter(request, "numberplate");
            logger.info("Checking plate availability: " + numberplate);

            boolean exists = vehicleDAO.plateExists(numberplate);
            response.getWriter().write(gson.toJson(new PlateCheckResponse(!exists)));

        } catch (IllegalArgumentException e) {
            logger.warning("Missing numberplate parameter");
            sendErrorResponse(response, "Number plate is required", HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking plate availability", e);
            sendErrorResponse(response, "Error checking plate availability",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String validateParameter(HttpServletRequest request, String paramName) {
        String value = request.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required parameter: " + paramName);
        }
        return value.trim();
    }

    private void handleListVehicles(HttpServletResponse response, String ownerNic) throws IOException {
        try {
            logger.info("Fetching vehicles for owner: " + ownerNic);
            List<Vehicle> vehicles = vehicleDAO.selectVehiclesByOwner(ownerNic);
            response.getWriter().write(gson.toJson(vehicles));
            logger.info("Returned " + vehicles.size() + " vehicles for owner " + ownerNic);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching vehicles", e);
            sendErrorResponse(response, "Failed to retrieve vehicles", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleDeleteVehicle(HttpServletRequest request, HttpServletResponse response, String ownerNic)
            throws IOException {
        try {
            String numberplate = validateParameter(request, "numberplate");
            logger.info("Attempting to delete vehicle: " + numberplate + " for owner: " + ownerNic);

            // Verify ownership before deletion
            Vehicle vehicle = vehicleDAO.selectVehicleByPlate(numberplate);
            if (vehicle == null || !vehicle.getNic().equals(ownerNic)) {
                logger.warning("Unauthorized deletion attempt for vehicle: " + numberplate);
                sendErrorResponse(response, "Vehicle not found or unauthorized", HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            boolean success = vehicleDAO.deleteVehicle(numberplate);
            if (success) {
                logger.info("Vehicle deleted successfully: " + numberplate);
                sendSuccessResponse(response, "Vehicle deleted successfully");
            } else {
                logger.warning("Failed to delete vehicle: " + numberplate);
                sendErrorResponse(response, "Failed to delete vehicle", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (IllegalArgumentException e) {
            logger.warning("Missing numberplate parameter");
            sendErrorResponse(response, "Number plate is required", HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting vehicle", e);
            sendErrorResponse(response, "Database operation failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleGetVehicle(HttpServletRequest request, HttpServletResponse response, String ownerNic)
            throws IOException {
        try {
            String numberplate = validateParameter(request, "numberplate");
            logger.info("Fetching vehicle: " + numberplate + " for owner: " + ownerNic);

            Vehicle vehicle = vehicleDAO.selectVehicleByPlate(numberplate);
            if (vehicle == null || !vehicle.getNic().equals(ownerNic)) {
                logger.warning("Vehicle not found or unauthorized: " + numberplate);
                sendErrorResponse(response, "Vehicle not found or unauthorized", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.getWriter().write(gson.toJson(vehicle));
            logger.info("Returned vehicle details: " + numberplate);
        } catch (IllegalArgumentException e) {
            logger.warning("Missing numberplate parameter");
            sendErrorResponse(response, "Number plate is required", HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error fetching vehicle", e);
            sendErrorResponse(response, "Database operation failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(new MessageResponse("success", message)));
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json"); // Ensure JSON content type
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(new MessageResponse("error", message)));
    }

    private static class MessageResponse {
        private final String status;
        private final String message;

        public MessageResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class PlateCheckResponse {
        private final boolean available;

        public PlateCheckResponse(boolean available) {
            this.available = available;
        }

        public boolean isAvailable() {
            return available;
        }
    }
}