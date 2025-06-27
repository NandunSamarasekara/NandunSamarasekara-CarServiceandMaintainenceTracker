<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    // Check if the user is logged in (session attribute "nic" exists)
    String nic = (String) session.getAttribute("nic");
    if (nic == null) {
        // If not logged in, redirect to login page
        response.sendRedirect("login.jsp");
        return; // Stop further processing of this JSP
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dashboard</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
            text-align: center;
        }
        .container {
            background-color: #fff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            width: 600px;
        }
        h2 {
            color: #333;
            margin-bottom: 20px;
        }
        p {
            color: #555;
            font-size: 18px;
            margin-bottom: 30px;
        }
        .btn {
            background-color: #dc3545;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            text-decoration: none; /* For anchor tag */
        }
        .btn:hover {
            background-color: #c82333;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>Welcome to Your Dashboard!</h2>
    <p>Hello, <%= nic %>!</p>
    <p>This is your personalized dashboard. You are successfully logged in.</p>
    <a href="login.jsp" class="btn">Logout</a>
</div>
</body>
</html>