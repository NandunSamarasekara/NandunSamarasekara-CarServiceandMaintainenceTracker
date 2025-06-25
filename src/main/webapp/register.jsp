<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Register</title>
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
    .form-group input[type="email"],
    .form-group input[type="password"] {
      width: calc(100% - 20px);
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
      box-sizing: border-box;
    }
    .btn {
      background-color: #007bff;
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
      background-color: #0056b3;
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
    .login-link {
      margin-top: 20px;
      font-size: 14px;
    }
    .login-link a {
      color: #007bff;
      text-decoration: none;
    }
    .login-link a:hover {
      text-decoration: underline;
    }
  </style>
</head>
<body>
<div class="container">
  <h2>Register</h2>
  <div id="message" class="message"></div>
  <form id="registerForm">
    <div class="form-group">
      <label for="nic">NIC:</label>
      <input type="text" id="nic" name="nic" required>
    </div>
    <div class="form-group">
      <label for="first_name">First Name:</label>
      <input type="text" id="first_name" name="first_name" required>
    </div>
    <div class="form-group">
      <label for="last_name">Last Name:</label>
      <input type="text" id="last_name" name="last_name" required>
    </div>
    <div class="form-group">
      <label for="email">Email:</label>
      <input type="email" id="email" name="email" required>
    </div>
    <div class="form-group">
      <label for="phone">Phone:</label>
      <input type="text" id="phone" name="phone" required>
    </div>
    <div class="form-group">
      <label for="password">Password:</label>
      <input type="password" id="password" name="password" required>
    </div>
    <div class="form-group">
      <label for="confpassword">Confirm Password:</label>
      <input type="password" id="confpassword" name="confpassword" required>
    </div>
    <button type="submit" class="btn">Register</button>
  </form>
  <div class="login-link">
    Already have an account? <a href="login.jsp">Login here</a>
  </div>
</div>

<script>
  document.getElementById('registerForm').addEventListener('submit', function(event) {
    event.preventDefault(); // Prevent default form submission

    const nic = document.getElementById('nic').value;
    const first_name = document.getElementById('first_name').value;
    const last_name = document.getElementById('last_name').value;
    const email = document.getElementById('email').value;
    const phone = document.getElementById('phone').value;
    const password = document.getElementById('password').value;
    const confpassword = document.getElementById('confpassword').value;
    const messageDiv = document.getElementById('message');

    // Clear previous messages
    messageDiv.style.display = 'none';
    messageDiv.className = 'message';
    messageDiv.textContent = '';

    if (password !== confpassword) {
      messageDiv.classList.add('error');
      messageDiv.textContent = 'Passwords do not match!';
      messageDiv.style.display = 'block';
      return;
    }

    const userData = {
      nic: nic,
      first_name: first_name,
      last_name: last_name,
      email: email,
      phone: phone,
      password: password,
      confpassword: confpassword // This field is not stored but used for client-side validation
    };

    fetch('<%= request.getContextPath() %>/user?action=register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(userData)
    })
            .then(response => response.json().then(data => ({ status: response.status, body: data })))
            .then(obj => {
              if (obj.status === 200 && obj.body.status === 'success') {
                messageDiv.classList.add('success');
                messageDiv.textContent = obj.body.message;
                messageDiv.style.display = 'block';
                // Redirect to login page after a short delay
                setTimeout(() => {
                  window.location.href = obj.body.redirect;
                }, 2000);
              } else {
                messageDiv.classList.add('error');
                messageDiv.textContent = obj.body.message || 'Registration failed. Please try again.';
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