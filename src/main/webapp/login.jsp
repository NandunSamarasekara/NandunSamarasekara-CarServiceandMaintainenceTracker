<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Login</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
        }
        .container {
            background-color: #fff;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            width: 400px;
            text-align: center;
        }
        h2 {
            margin-bottom: 20px;
            color: #333;
        }
        .form-group {
            margin-bottom: 15px;
            text-align: left;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            color: #555;
        }
        .form-group input[type="text"],
        .form-group input[type="password"] {
            width: calc(100% - 20px);
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        .btn {
            background-color: #28a745;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            width: 100%;
            box-sizing: border-box;
        }
        .btn:hover {
            background-color: #218838;
        }
        .message {
            margin-top: 15px;
            padding: 10px;
            border-radius: 4px;
            display: none; /* Hidden by default */
        }
        .message.success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .message.error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .register-link {
            margin-top: 20px;
            font-size: 14px;
        }
        .register-link a {
            color: #007bff;
            text-decoration: none;
        }
        .register-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>Login</h2>
    <div id="message" class="message"></div>
    <form id="loginForm">
        <div class="form-group">
            <label for="nic">NIC:</label>
            <input type="text" id="nic" name="nic" required>
        </div>
        <div class="form-group">
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>
        </div>
        <button type="submit" class="btn">Login</button>
    </form>
    <div class="register-link">
        Don't have an account? <a href="register.jsp">Register here</a>
    </div>
</div>

<script>
    document.getElementById('loginForm').addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent default form submission

        const nic = document.getElementById('nic').value;
        const password = document.getElementById('password').value;
        const messageDiv = document.getElementById('message');

        // Clear previous messages
        messageDiv.style.display = 'none';
        messageDiv.className = 'message';
        messageDiv.textContent = '';

        const credentials = {
            nic: nic,
            password: password
        };

        fetch('<%= request.getContextPath() %>/user?action=login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(credentials)
        })
            .then(response => response.json().then(data => ({ status: response.status, body: data })))
            .then(obj => {
                if (obj.status === 200 && obj.body.status === 'success') {
                    messageDiv.classList.add('success');
                    messageDiv.textContent = 'Login successful! Redirecting...';
                    messageDiv.style.display = 'block';
                    // Redirect to dashboard
                    setTimeout(() => {
                        window.location.href = obj.body.redirect;
                    }, 1000);
                } else {
                    messageDiv.classList.add('error');
                    messageDiv.textContent = obj.body.message || 'Login failed. Please check your credentials.';
                    messageDiv.style.display = 'block';
                }
            })
            .catch(error => {
                console.error('Error:', error);
                messageDiv.classList.add('error');
                messageDiv.textContent = 'An unexpected error occurred. Please try again later.';
                messageDiv.style.display = 'block';
            });
    });
</script>
</body>
</html>