package com.usermanagement.usermanagement;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "UserServlet", urlPatterns = {"/user"})
public class UserServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(UserServlet.class.getName());
    private UserDAO userDAO;
    private final Gson gson = new Gson();

    public void init() {
        userDAO = new UserDAO();
        logger.info("UserServlet initialized");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        try {
            String action = request.getParameter("action");
            if (action == null || action.isEmpty()) {
                sendError(response, "Action parameter is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            logger.info("Received request with action: " + action);

            switch (action) {
                case "register":
                    handleRegistration(request, response);
                    break;
                case "login":
                    handleLogin(request, response);
                    break;
                case "insert":
                    handleInsert(request, response);
                    break;
                case "update":
                    handleUpdate(request, response);
                    break;
                case "delete":
                    handleDelete(request, response);
                    break;
                case "list":
                    handleList(request, response);
                    break;
                case "edit":
                    handleEdit(request, response);
                    break;
                default:
                    sendError(response, "Invalid action", HttpServletResponse.SC_BAD_REQUEST);
                    break;
            }
        } catch (JsonSyntaxException e) {
            logger.log(Level.SEVERE, "JSON parsing error", e);
            sendError(response, "Invalid JSON format", HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error", e);
            sendError(response, "Database operation failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error", e);
            sendError(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleRegistration(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        User user = parseUserFromRequest(request);

        // Validate required fields
        if (user.getNIC() == null || user.getNIC().isEmpty() ||
                user.getFirst_name() == null || user.getFirst_name().isEmpty() ||
                user.getLast_name() == null || user.getLast_name().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getPhone() == null || user.getPhone().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty())
        {
            sendError(response, "All fields are required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Check if NIC already exists
        if (userDAO.isNicExists(user.getNIC())) {
            sendError(response, "NIC already exists", HttpServletResponse.SC_CONFLICT);
            return;
        }

        // Check if email already exists
        if (userDAO.isEmailExists(user.getEmail())) {
            sendError(response, "Email already exists", HttpServletResponse.SC_CONFLICT);
            return;
        }

        // Insert the new user
        boolean success = userDAO.insertUser(user);
        if (success) {
            Map<String, String> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("redirect", request.getContextPath() + "/login.jsp");
            responseData.put("message", "Account created successfully! Login using your NIC: " + user.getNIC());
            response.getWriter().write(gson.toJson(responseData));
        } else {
            sendError(response, "Registration failed. Please try again.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        Map<String, String> credentials = parseJsonRequest(request);

        String nic = credentials.get("nic");
        String password = credentials.get("password");

        if (nic == null || nic.isEmpty() || password == null || password.isEmpty()) {
            sendError(response, "NIC and password are required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean isValid = userDAO.validateUser(nic, password);
        if (isValid) {
            HttpSession session = request.getSession();
            session.setAttribute("nic", nic);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("redirect", request.getContextPath() + "/dashboard.jsp");
            response.getWriter().write(gson.toJson(responseData));
        } else {
            sendError(response, "Invalid NIC or password", HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void handleInsert(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        User user = parseUserFromRequest(request);

        if (userDAO.isNicExists(user.getNIC())) {
            sendError(response, "NIC already exists", HttpServletResponse.SC_CONFLICT);
            return;
        }

        if (userDAO.isEmailExists(user.getEmail())) {
            sendError(response, "Email already exists", HttpServletResponse.SC_CONFLICT);
            return;
        }

        boolean success = userDAO.insertUser(user);
        if (success) {
            sendSuccess(response, "User created successfully");
        } else {
            sendError(response, "Failed to create user", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        User user = parseUserFromRequest(request);
        boolean success = userDAO.updateUser(user);
        if (success) {
            sendSuccess(response, "User updated successfully");
        } else {
            sendError(response, "Failed to update user", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        String nic = request.getParameter("nic");
        if (nic == null || nic.isEmpty()) {
            sendError(response, "NIC parameter is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean success = userDAO.deleteUser(nic);
        if (success) {
            sendSuccess(response, "User deleted successfully");
        } else {
            sendError(response, "Failed to delete user", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleList(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        List<User> users = userDAO.selectAllUsers();
        response.getWriter().write(gson.toJson(users));
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        String nic = request.getParameter("nic");
        if (nic == null || nic.isEmpty()) {
            sendError(response, "NIC parameter is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = userDAO.selectUser(nic);
        if (user != null) {
            response.getWriter().write(gson.toJson(user));
        } else {
            sendError(response, "User not found", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private User parseUserFromRequest(HttpServletRequest request) throws IOException {
        return gson.fromJson(request.getReader(), User.class);
    }

    private Map<String, String> parseJsonRequest(HttpServletRequest request) throws IOException {
        return gson.fromJson(request.getReader(), Map.class);
    }

    private void sendSuccess(HttpServletResponse response, String message) throws IOException {
        Map<String, String> responseData = new HashMap<>();
        responseData.put("status", "success");
        responseData.put("message", message);
        response.getWriter().write(gson.toJson(responseData));
    }

    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        response.getWriter().write(gson.toJson(error));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        sendError(response, "GET method not supported. Use POST.", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}