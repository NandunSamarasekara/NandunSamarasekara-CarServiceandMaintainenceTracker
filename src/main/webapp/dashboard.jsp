<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Check session and redirect if not logged in
    HttpSession userSession = request.getSession(false);
    if (userSession == null || userSession.getAttribute("email") == null) {
        response.sendRedirect("login.jsp");
        return;
    }

    // Get user details from session
    String email = (String) userSession.getAttribute("email");
    String nic = (String) userSession.getAttribute("nic");
    String firstName = (String) userSession.getAttribute("first_name");
    String lastName = (String) userSession.getAttribute("last_name");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Dashboard</title>
    <style>
        :root {
            --primary-color: #3498db;
            --secondary-color: #2980b9;
            --success-color: #2ecc71;
            --danger-color: #e74c3c;
            --light-color: #ecf0f1;
            --dark-color: #2c3e50;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f5f7fa;
            color: #333;
        }

        .dashboard-container {
            display: flex;
            min-height: 100vh;
        }

        .sidebar {
            width: 250px;
            background-color: var(--dark-color);
            color: white;
            padding: 20px 0;
            box-shadow: 2px 0 5px rgba(0,0,0,0.1);
        }

        .sidebar-header {
            padding: 0 20px 20px;
            border-bottom: 1px solid rgba(255,255,255,0.1);
        }

        .sidebar-header h2 {
            margin: 0;
            font-size: 1.3rem;
        }

        .sidebar-header p {
            margin: 5px 0 0;
            font-size: 0.9rem;
            color: #bdc3c7;
        }

        .nav-menu {
            list-style: none;
            padding: 0;
            margin: 20px 0;
        }

        .nav-menu li a {
            display: block;
            padding: 12px 20px;
            color: white;
            text-decoration: none;
            transition: all 0.3s;
        }

        .nav-menu li a:hover {
            background-color: rgba(255,255,255,0.1);
            padding-left: 25px;
        }

        .nav-menu li a i {
            margin-right: 10px;
        }

        .main-content {
            flex: 1;
            padding: 30px;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
        }

        .header h1 {
            margin: 0;
            color: var(--dark-color);
        }

        .btn-logout {
            background-color: var(--danger-color);
            color: white;
            border: none;
            padding: 8px 15px;
            border-radius: 4px;
            cursor: pointer;
            transition: background-color 0.3s;
        }

        .btn-logout:hover {
            background-color: #c0392b;
        }

        .welcome-card {
            background-color: white;
            border-radius: 8px;
            padding: 25px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
            margin-bottom: 30px;
        }

        .welcome-card h2 {
            margin-top: 0;
            color: var(--primary-color);
        }

        .user-info {
            display: flex;
            flex-wrap: wrap;
            gap: 20px;
            margin-top: 20px;
        }

        .info-card {
            background-color: var(--light-color);
            padding: 15px;
            border-radius: 6px;
            flex: 1;
            min-width: 200px;
        }

        .info-card h3 {
            margin-top: 0;
            font-size: 1rem;
            color: #7f8c8d;
        }

        .info-card p {
            margin-bottom: 0;
            font-size: 1.1rem;
            font-weight: 500;
        }

        @media (max-width: 768px) {
            .dashboard-container {
                flex-direction: column;
            }

            .sidebar {
                width: 100%;
                height: auto;
            }

            .user-info {
                flex-direction: column;
            }
        }
    </style>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
</head>
<body>
<div class="dashboard-container">
    <!-- Sidebar Navigation -->
    <div class="sidebar">
        <div class="sidebar-header">
            <h2>Car Service Tracker</h2>
            <p>User Dashboard</p>
        </div>
        <ul class="nav-menu">
            <li><a href="dashboard.jsp"><i class="fas fa-tachometer-alt"></i> Dashboard</a></li>
            <li><a href="#"><i class="fas fa-car"></i> My Vehicles</a></li>
            <li><a href="#"><i class="fas fa-calendar-alt"></i> Appointments</a></li>
            <li><a href="#"><i class="fas fa-history"></i> Service History</a></li>
            <li><a href="#"><i class="fas fa-cog"></i> Settings</a></li>
        </ul>
    </div>

    <!-- Main Content Area -->
    <div class="main-content">
        <div class="header">
            <h1>Dashboard</h1>
            <form action="logout.jsp" method="post">
                <button type="submit" class="btn-logout">
                    <i class="fas fa-sign-out-alt"></i> Logout
                </button>
            </form>
        </div>

        <div class="welcome-card">
            <h2>Welcome, <%= firstName != null ? firstName + " " + (lastName != null ? lastName : "") : "User" %>!</h2>
            <p>You're now logged in to your Car Service Tracker account.</p>

            <div class="user-info">
                <div class="info-card">
                    <h3>Email Address</h3>
                    <p><%= email %></p>
                </div>
                <% if (nic != null) { %>
                <div class="info-card">
                    <h3>NIC Number</h3>
                    <p><%= nic %></p>
                </div>
                <% } %>
                <div class="info-card">
                    <h3>Account Status</h3>
                    <p>Active <i class="fas fa-check-circle" style="color: var(--success-color);"></i></p>
                </div>
            </div>
        </div>

        <div class="quick-actions">
            <h2>Quick Actions</h2>
            <!-- Add your quick action buttons or cards here -->
        </div>
    </div>
</div>
</body>
</html>