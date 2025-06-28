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
    String phone = (String) userSession.getAttribute("phone");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Settings</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
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

        .settings-card {
            background-color: white;
            border-radius: 8px;
            padding: 25px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
            max-width: 600px;
            margin: 0 auto;
        }

        .settings-card h2 {
            margin-top: 0;
            color: var(--primary-color);
            border-bottom: 1px solid #eee;
            padding-bottom: 10px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: 500;
        }

        .form-group input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 16px;
        }

        .btn-save {
            background-color: var(--success-color);
            color: white;
            border: none;
            padding: 10px 20px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            transition: background-color 0.3s;
        }

        .btn-save:hover {
            background-color: #27ae60;
        }

        .message {
            padding: 10px;
            margin-bottom: 20px;
            border-radius: 4px;
            display: none;
        }

        .success {
            background-color: #d4edda;
            color: #155724;
            display: block;
        }

        .error {
            background-color: #f8d7da;
            color: #721c24;
            display: block;
        }

        @media (max-width: 768px) {
            .dashboard-container {
                flex-direction: column;
            }
            .sidebar {
                width: 100%;
                height: auto;
            }
        }
    </style>
</head>
<body>
<div class="dashboard-container">
    <!-- Sidebar Navigation -->
    <div class="sidebar">
        <div class="sidebar-header">
            <h2>Car Service Tracker</h2>
            <p>User Settings</p>
        </div>
        <ul class="nav-menu">
            <li><a href="dashboard.jsp"><i class="fas fa-tachometer-alt"></i> Dashboard</a></li>
            <li><a href="#"><i class="fas fa-car"></i> My Vehicles</a></li>
            <li><a href="#"><i class="fas fa-calendar-alt"></i> Appointments</a></li>
            <li><a href="#"><i class="fas fa-history"></i> Service History</a></li>
            <li><a href="Settings.jsp"><i class="fas fa-cog"></i> Settings</a></li>
        </ul>
    </div>

    <!-- Main Content Area -->
    <div class="main-content">
        <div class="header">
            <h1>Account Settings</h1>
            <form action="logout.jsp" method="post">
                <button type="submit" class="btn-logout">
                    <i class="fas fa-sign-out-alt"></i> Logout
                </button>
            </form>
        </div>

        <div class="settings-card">
            <h2>Profile Information</h2>
            <div id="message" class="message"></div>

            <form id="profileForm">
                <input type="hidden" id="nic" name="nic" value="<%= nic %>">

                <div class="form-group">
                    <label for="first_name">First Name</label>
                    <input type="text" id="first_name" name="first_name" value="<%= firstName %>" required>
                </div>

                <div class="form-group">
                    <label for="last_name">Last Name</label>
                    <input type="text" id="last_name" name="last_name" value="<%= lastName %>" required>
                </div>

                <div class="form-group">
                    <label for="email">Email</label>
                    <input type="email" id="email" name="email" value="<%= email %>" readonly>
                </div>

                <div class="form-group">
                    <label for="phone">Phone Number</label>
                    <input type="tel" id="phone" name="phone" value="<%= phone != null ? phone : "" %>" required>
                </div>

                <div class="form-group">
                    <label for="password">New Password (leave blank to keep current)</label>
                    <input type="password" id="password" name="password">
                </div>

                <button type="submit" class="btn-save">Save Changes</button>
            </form>
        </div>
    </div>
</div>

<script>
    document.getElementById('profileForm').addEventListener('submit', function(e) {
        e.preventDefault();

        // Create form data object
        const formData = {
            action: 'updateProfile',
            nic: document.getElementById('nic').value,
            first_name: document.getElementById('first_name').value,
            last_name: document.getElementById('last_name').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value,
            password: document.getElementById('password').value
        };

        console.log('Submitting:', formData);

        fetch('<%= request.getContextPath() %>/user', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(formData)
        })
            .then(response => {
                console.log('Response status:', response.status);
                if (!response.ok) {
                    return response.json().then(err => {
                        throw new Error(err.message || 'Server error');
                    });
                }
                return response.json();
            })
            .then(data => {
                console.log('Response data:', data);
                const messageDiv = document.getElementById('message');
                messageDiv.className = 'message';

                if (data.status === 'success') {
                    messageDiv.classList.add('success');
                    messageDiv.textContent = data.message;

                    if (data.redirect) {
                        setTimeout(() => {
                            window.location.href = data.redirect;
                        }, 1500);
                    }
                } else {
                    messageDiv.classList.add('error');
                    messageDiv.textContent = data.message || 'An unknown error occurred';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                const messageDiv = document.getElementById('message');
                messageDiv.className = 'message error';
                messageDiv.textContent = error.message || 'An error occurred. Please try again.';
            });
    });
</script>
</body>
</html>