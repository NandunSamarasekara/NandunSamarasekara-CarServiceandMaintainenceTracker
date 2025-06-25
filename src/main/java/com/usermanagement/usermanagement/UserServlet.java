package com.usermanagement.usermanagement;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "UserServlet", value = "/user")
public class UserServlet extends HttpServlet {
    private UserDAO userDAO;
    private final Gson gson = new Gson();

    public void init() {
        userDAO = new UserDAO();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null || action.isEmpty()) {
            sendError(response, "Action parameter is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            switch (action) {
                case "register":
                    handleRegistration(request, response);
                    break;
                case "login":
                    handleLogin(request, response);
                    break;
                default:
                    sendError(response, "Invalid action", HttpServletResponse.SC_BAD_REQUEST);
                    break;
            }
        } catch (JsonSyntaxException e) {
            sendError(response, "Invalid JSON format: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException ex) {
            sendError(response, "Database error: " + ex.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            sendError(response, "Unexpected error: " + ex.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleRegistration(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        User user = parseUserFromRequest(request);

        // Validate required fields
        if (user.getNIC() == null || user.getNIC().isEmpty() ||
                user.getFist_name() == null || user.getFist_name().isEmpty() ||
                user.getLast_name() == null || user.getLast_name().isEmpty() ||
                user.getEmail() == null || user.getEmail().isEmpty() ||
                user.getPhone() == null || user.getPhone().isEmpty() ||
                user.getPassword() == null || user.getPassword().isEmpty()) {
            sendError(response, "All fields are required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Check if NIC or email already exists
        if (userDAO.isNicExists(user.getNIC())) {
            sendError(response, "NIC already exists", HttpServletResponse.SC_CONFLICT);
            return;
        }

        if (userDAO.isEmailExists(user.getEmail())) {
            sendError(response, "Email already exists", HttpServletResponse.SC_CONFLICT);
            return;
        }

        // Insert user into database
        boolean success = userDAO.insertUser(user);
        if (success) {
            Map<String, String> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("redirect", "login.html");
            responseData.put("message", "Account created successfully! Login using your username: " +
                    user.getFist_name() + user.getLast_name());
            response.getWriter().write(gson.toJson(responseData));
        } else {
            sendError(response, "Registration failed. Please try again.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        Map<String, String> credentials = parseJsonRequest(request);

        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            sendError(response, "Username and password are required",
                    HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean isValid = userDAO.validateUser(username, password);
        if (isValid) {
            HttpSession session = request.getSession();
            session.setAttribute("username", username);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("status", "success");
            responseData.put("redirect", "dashboard.html");
            response.getWriter().write(gson.toJson(responseData));
        } else {
            sendError(response, "Invalid username or password", HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private User parseUserFromRequest(HttpServletRequest request) throws IOException {
        return gson.fromJson(request.getReader(), User.class);
    }

    private Map<String, String> parseJsonRequest(HttpServletRequest request) throws IOException {
        return gson.fromJson(request.getReader(), Map.class);
    }

    private void sendError(HttpServletResponse response, String message, int status)
            throws IOException {
        response.setStatus(status);
        Map<String, String> error = new HashMap<>();
        error.put("status", "error");
        error.put("error", message);
        response.getWriter().write(gson.toJson(error));
    }

    // Add this to handle potential GET requests (for debugging)
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        sendError(response, "GET method not supported. Use POST.",
                HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}