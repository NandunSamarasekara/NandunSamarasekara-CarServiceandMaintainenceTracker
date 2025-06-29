<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Register New Vehicle</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        :root {
            --primary-color: #3498db;
            --success-color: #2ecc71;
            --danger-color: #e74c3c;
            --light-color: #ecf0f1;
            --dark-color: #2c3e50;
            --border-color: #ddd;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        body {
            background-color: #f5f7fa;
            color: #333;
            line-height: 1.6;
            padding: 20px;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        h1 {
            color: var(--dark-color);
            text-align: center;
            margin-bottom: 30px;
            font-weight: 600;
        }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: var(--dark-color);
        }

        label:after {
            content: "*";
            color: var(--danger-color);
            margin-left: 4px;
        }

        input, select {
            width: 100%;
            padding: 12px;
            border: 1px solid var(--border-color);
            border-radius: 5px;
            font-size: 16px;
            transition: border-color 0.3s ease;
        }

        input:invalid, select:invalid {
            border-color: var(--danger-color);
        }

        input:focus, select:focus {
            outline: none;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
        }

        .error-message {
            color: var(--danger-color);
            font-size: 14px;
            margin-top: 5px;
            display: none;
        }

        .btn {
            display: inline-block;
            padding: 12px 24px;
            border-radius: 5px;
            text-decoration: none;
            font-weight: 500;
            transition: all 0.3s ease;
            cursor: pointer;
            border: none;
            font-size: 16px;
            width: 100%;
        }

        .btn-primary {
            background-color: var(--primary-color);
            color: white;
        }

        .btn-primary:hover {
            background-color: #2980b9;
        }

        .btn-primary:disabled {
            background-color: #95a5a6;
            cursor: not-allowed;
        }

        .btn-secondary {
            background-color: #95a5a6;
            color: white;
            margin-top: 15px;
        }

        .btn-secondary:hover {
            background-color: #7f8c8d;
        }

        .form-footer {
            margin-top: 30px;
            text-align: center;
        }

        .server-error {
            color: var(--danger-color);
            background-color: #fdecea;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
            display: none;
            border-left: 4px solid var(--danger-color);
        }

        .success-message {
            color: var(--success-color);
            background-color: #eafaf1;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
            display: none;
            border-left: 4px solid var(--success-color);
        }

        .error-message {
            color: var(--danger-color);
            font-size: 14px;
            margin-top: 5px;
            display: none;
            animation: fadeIn 0.3s ease-in;
        }

        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        @media (max-width: 768px) {
            .container {
                padding: 20px;
            }
        }
    </style>
</head>
<body>
<div class="container">
    <h1>Register New Vehicle</h1>

    <div id="server-error" class="server-error"></div>
    <div id="success-message" class="success-message"></div>

    <form id="vehicleForm" novalidate>
        <div class="form-group">
            <label for="numberplate">Number Plate</label>
            <input type="text" id="numberplate" name="numberplate" required
                   placeholder="E.g., ABC-1234" pattern="[A-Za-z0-9-]{3,15}"
                   title="3-15 alphanumeric characters with optional hyphens">
            <div id="numberplate-error" class="error-message"></div>
        </div>

        <div class="form-group">
            <label for="registereddistrict">Registered District</label>
            <input type="text" id="registereddistrict" name="registereddistrict" required
                   placeholder="E.g., Colombo">
            <div id="district-error" class="error-message"></div>
        </div>

        <div class="form-group">
            <label for="enginenumber">Engine Number</label>
            <input type="text" id="enginenumber" name="enginenumber" required
                   placeholder="Engine identification number">
            <div id="engine-error" class="error-message"></div>
        </div>

        <div class="form-group">
            <label for="enginecapacity">Engine Capacity (cc)</label>
            <input type="number" id="enginecapacity" name="enginecapacity" required
                   min="1" placeholder="1500" value="1500">
            <div id="capacity-error" class="error-message"></div>
        </div>

        <div class="form-group">
            <label for="vehicletype">Vehicle Type</label>
            <select id="vehicletype" name="vehicletype" required>
                <option value="">Select Vehicle Type</option>
                <option value="Car">Car</option>
                <option value="Motorcycle">Motorcycle</option>
                <option value="Van">Van</option>
                <option value="Bus">Bus</option>
                <option value="Lorry">Lorry</option>
            </select>
            <div id="type-error" class="error-message"></div>
        </div>

        <button type="submit" class="btn btn-primary" id="submitBtn">Register Vehicle</button>
    </form>

    <div class="form-footer">
        <a href="myvehicles.jsp" class="btn btn-secondary">Back to My Vehicles</a>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const form = document.getElementById('vehicleForm');
        const submitBtn = document.getElementById('submitBtn');

// Real-time validation as user types
        form.addEventListener('input', function(e) {
            validateField(e.target);
        });

// Form submission handling
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            clearMessages();

            if (validateForm()) {
                registerVehicle();
            }
        });
    });

    function validateField(field) {
        const errorElement = document.getElementById(`${field.id}-error`);

        if (field.validity.valid) {
            errorElement.style.display = 'none';
            field.style.borderColor = '';
        } else {
            showFieldError(field);
        }
    }

    function validateForm() {
        let isValid = true;
        const fields = [
            'numberplate',
            'registereddistrict',
            'enginenumber',
            'enginecapacity',
            'vehicletype'
        ];

        fields.forEach(fieldId => {
            const field = document.getElementById(fieldId);
            if (!field.validity.valid) {
                showFieldError(field);
                isValid = false;
            } else if (fieldId === 'numberplate' && !/^[A-Za-z0-9-]{3,15}$/.test(field.value)) {
                showFieldError(field);
                isValid = false;
            } else if (fieldId === 'enginecapacity' && parseInt(field.value) <= 0) {
                showFieldError(field);
                isValid = false;
            }
        });

        return isValid;
    }

    function showFieldError(field) {
        const errorElement = document.getElementById(`${field.id}-error`);

        if (field.validity.valueMissing) {
            errorElement.textContent = 'This field is required';
        } else if (field.validity.patternMismatch) {
            errorElement.textContent = 'Invalid format for number plate (use letters, numbers and hyphens)';
        } else if (field.validity.rangeUnderflow || parseInt(field.value) <= 0) {
            errorElement.textContent = 'Engine capacity must be greater than 0';
        } else if (field.validity.badInput) {
            errorElement.textContent = 'Please enter a valid number';
        } else {
            errorElement.textContent = 'Invalid value';
        }

        errorElement.style.display = 'block';
        field.style.borderColor = 'var(--danger-color)';
    }

    function registerVehicle() {
        const submitBtn = document.getElementById('submitBtn');
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span class="spinner">⌛</span> Registering...';

        const formData = new URLSearchParams();
        formData.append('numberplate', document.getElementById('numberplate').value.trim());
        formData.append('registereddistrict', document.getElementById('registereddistrict').value.trim());
        formData.append('enginenumber', document.getElementById('enginenumber').value.trim());
        formData.append('enginecapacity', document.getElementById('enginecapacity').value.trim());
        formData.append('vehicletype', document.getElementById('vehicletype').value);
        formData.append('action', 'add');

        fetch('/vehicle', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: formData
        })
            .then(response => {
                // Check if response is JSON before parsing
                const contentType = response.headers.get("content-type");
                if (contentType && contentType.indexOf("application/json") !== -1) {
                    return response.json().then(data => {
                        if (!response.ok) {
                            throw new Error(data.message || `Server error: ${response.status}`);
                        }
                        return data;
                    });
                } else {
                    // If not JSON, read as text and throw error
                    return response.text().then(text => {
                        throw new Error(text || `Server error: ${response.status}`);
                    });
                }
            })
            .then(data => {
                if (data && data.status === 'success') {
                    showSuccess(data.message || 'Vehicle registered successfully!');
                    document.getElementById('vehicleForm').reset();
                } else {
                    throw new Error(data?.message || 'Registration failed');
                }
            })
            .catch(error => {
                console.error('Registration error:', error);
                let errorMessage = error.message;

// Handle common error cases
                if (errorMessage.includes('<!DOCTYPE html>')) {
                    errorMessage = 'Server error occurred. Please try again.';
                } else if (errorMessage.length > 100) {
                    errorMessage = errorMessage.substring(0, 100) + '...';
                }

                showServerError(errorMessage);
            })
            .finally(() => {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Register Vehicle';
            });
    }

    function showServerError(message) {
        const element = document.getElementById('server-error');
        element.textContent = message;
        element.style.display = 'block';
        setTimeout(() => element.style.display = 'none', 5000);
    }

    function showSuccess(message) {
        const element = document.getElementById('success-message');
        element.textContent = message;
        element.style.display = 'block';
        setTimeout(() => element.style.display = 'none', 5000);
    }

    function clearMessages() {
        document.getElementById('server-error').style.display = 'none';
        document.getElementById('success-message').style.display = 'none';
        document.querySelectorAll('.error-message').forEach(el => {
            el.style.display = 'none';
        });
    }
</script>
</body>
</html>