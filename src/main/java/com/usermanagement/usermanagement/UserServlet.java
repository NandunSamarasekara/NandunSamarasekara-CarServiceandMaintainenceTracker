package com.usermanagement.usermanagement;

import com.google.gson.Gson;
import com.usermanagement.usermanagement.UserDAO;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "UserServlet", value = "/user")
public class UserServlet extends HttpServlet {
    private UserDAO userDAO;

    public void init() {
        userDAO = new UserDAO();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            switch (action) {
                case "list":
                    listUsers(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "insert":
                    insertUser(request, response);
                    break;
                case "update":
                    updateUser(request, response);
                    break;
                case "delete":
                    deleteUser(request, response);
                    break;
                default:
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
                    break;
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    private void listUsers(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        List<User> listUsers = userDAO.selectAllUsers();
        String json = new Gson().toJson(listUsers);
        response.getWriter().write(json);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String nic = request.getParameter("nic");
        User user = userDAO.selectUser(nic);
        String json = new Gson().toJson(user);
        response.getWriter().write(json);
    }

    private void insertUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        // Read JSON from request
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }
        User user = new Gson().fromJson(sb.toString(), User.class);

        userDAO.insertUser(user);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        // Read JSON from request
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }
        User user = new Gson().fromJson(sb.toString(), User.class);

        userDAO.updateUser(user);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        String nic = request.getParameter("nic");
        userDAO.deleteUser(nic);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}