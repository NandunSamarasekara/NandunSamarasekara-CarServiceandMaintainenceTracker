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
            // First try to parse the request as JSON
            Map<String, String> requestData = parseJsonRequest(request);
            String action = requestData.get("action");

            // If action not found in JSON, try from parameters
            if (action == null || action.isEmpty()) {
                action = request.getParameter("action");
            }

            if (action == null || action.isEmpty()) {
                sendError(response, "Action parameter is required", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            logger.info("Received request with action: " + action);

            switch (action) {
                case "register":
                    handleRegistration(request, response, requestData);
                    break;
                case "login":
                    handleLogin(request, response, requestData);
                    break;
                case "insert":
                    handleInsert(request, response, requestData);
                    break;
                case "update":
                    handleUpdate(request, response, requestData);
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
                case "updateProfile":
                    handleProfileUpdate(request, response, requestData);
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

    private void handleRegistration(HttpServletRequest request, HttpServletResponse response,
                                    Map<String, String> requestData) throws IOException, SQLException {
        User user = new User(
                requestData.get("nic"),
                requestData.get("first_name"),
                requestData.get("last_name"),
                requestData.get("email"),
                requestData.get("phone"),
                requestData.get("password"),
                requestData.get("confpassword")
        );

        // Validate required fields
        if (user.getNIC() == null || user.getNIC().isEmpty() ||
                user.getFirst_name() == null || user.getFirst_name().isEmpty() ||
                user.getLast_name() == null || user.getLast_name().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getPhone() == null || user.getPhone().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty()) {
            sendError(response, "All fields are required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

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
            Map<String, String> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("redirect", request.getContextPath() + "/login.jsp");
            responseData.put("message", "Account created successfully!");
            response.getWriter().write(gson.toJson(responseData));
        } else {
            sendError(response, "Registration failed", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response,
                             Map<String, String> requestData) throws IOException, SQLException {
        String email = requestData.get("email");
        String password = requestData.get("password");

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            sendError(response, "Email and password are required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        User user = userDAO.getUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("email", email);
            session.setAttribute("nic", user.getNIC());
            session.setAttribute("first_name", user.getFirst_name());
            session.setAttribute("last_name", user.getLast_name());
            session.setAttribute("phone", user.getPhone());

            Map<String, String> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("redirect", request.getContextPath() + "/dashboard.jsp");
            response.getWriter().write(gson.toJson(responseData));
        } else {
            sendError(response, "Invalid credentials", HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void handleProfileUpdate(HttpServletRequest request, HttpServletResponse response,
                                     Map<String, String> requestData) throws IOException, SQLException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("email") == null) {
            sendError(response, "Session expired. Please login again.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String currentEmail = (String) session.getAttribute("email");
        User currentUser = userDAO.getUserByEmail(currentEmail);

        if (currentUser == null) {
            sendError(response, "User not found", HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Update fields from request data
        currentUser.setFirst_name(requestData.get("first_name"));
        currentUser.setLast_name(requestData.get("last_name"));
        currentUser.setPhone(requestData.get("phone"));

        boolean passwordChanged = false;
        String newPassword = requestData.get("password");
        if (newPassword != null && !newPassword.isEmpty()) {
            currentUser.setPassword(newPassword);
            passwordChanged = true;
        }

        boolean success = userDAO.updateUser(currentUser);
        if (success) {
            // Update session with new values
            session.setAttribute("first_name", currentUser.getFirst_name());
            session.setAttribute("last_name", currentUser.getLast_name());
            session.setAttribute("phone", currentUser.getPhone());

            Map<String, String> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("message", "Profile updated successfully");

            if (passwordChanged) {
                session.invalidate();
                responseData.put("redirect", request.getContextPath() + "/login.jsp");
            } else {
                responseData.put("redirect", request.getContextPath() + "/dashboard.jsp");
            }

            response.getWriter().write(gson.toJson(responseData));
        } else {
            sendError(response, "Failed to update profile", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleInsert(HttpServletRequest request, HttpServletResponse response,
                              Map<String, String> requestData) throws IOException, SQLException {
        User user = new User(
                requestData.get("nic"),
                requestData.get("first_name"),
                requestData.get("last_name"),
                requestData.get("email"),
                requestData.get("phone"),
                requestData.get("password"),
                null
        );

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

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response,
                              Map<String, String> requestData) throws IOException, SQLException {
        User user = new User(
                requestData.get("nic"),
                requestData.get("first_name"),
                requestData.get("last_name"),
                requestData.get("email"),
                requestData.get("phone"),
                requestData.get("password"),
                null
        );

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

    private Map<String, String> parseJsonRequest(HttpServletRequest request) throws IOException {
        try {
            return gson.fromJson(request.getReader(), Map.class);
        } catch (JsonSyntaxException e) {
            logger.log(Level.WARNING, "Error parsing JSON request", e);
            return new HashMap<>();
        }
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