<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>My Vehicles</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        :root {
            --primary-color: #3498db;
            --success-color: #2ecc71;
            --danger-color: #e74c3c;
            --light-color: #ecf0f1;
            --dark-color: #2c3e50;
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
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }

        header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 30px;
            padding-bottom: 15px;
            border-bottom: 1px solid #ddd;
        }

        h1 {
            color: var(--dark-color);
            font-weight: 600;
        }

        .btn {
            display: inline-block;
            padding: 10px 20px;
            border-radius: 5px;
            text-decoration: none;
            font-weight: 500;
            transition: all 0.3s ease;
            cursor: pointer;
            border: none;
        }

        .btn-primary {
            background-color: var(--primary-color);
            color: white;
        }

        .btn-primary:hover {
            background-color: #2980b9;
        }

        .btn-danger {
            background-color: var(--danger-color);
            color: white;
        }

        .btn-danger:hover {
            background-color: #c0392b;
        }

        .vehicle-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 20px;
            margin-top: 20px;
        }

        .vehicle-card {
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            padding: 20px;
            transition: transform 0.3s ease;
        }

        .vehicle-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
        }

        .vehicle-card h3 {
            color: var(--dark-color);
            margin-bottom: 10px;
            font-size: 1.3rem;
        }

        .vehicle-card p {
            margin-bottom: 8px;
            color: #555;
        }

        .vehicle-card strong {
            color: var(--dark-color);
        }

        .card-actions {
            display: flex;
            gap: 10px;
            margin-top: 15px;
        }

        .empty-state {
            text-align: center;
            padding: 40px;
            grid-column: 1 / -1;
        }

        .empty-state p {
            color: #7f8c8d;
            font-size: 1.1rem;
            margin-bottom: 15px;
        }

        .loading-spinner {
            display: none; /* Hidden by default, shown by JS */
            text-align: center;
            padding: 30px;
            grid-column: 1 / -1;
        }

        .error-message {
            color: var(--danger-color);
            padding: 15px;
            background-color: #fdecea;
            border-radius: 5px;
            margin-bottom: 20px;
            display: none; /* Hidden by default, shown by JS */
        }

        @media (max-width: 768px) {
            .vehicle-grid {
                grid-template-columns: 1fr;
            }

            header {
                flex-direction: column;
                align-items: flex-start;
                gap: 15px;
            }
        }
    </style>
</head>
<body>
<div class="container">
    <header>
        <h1>My Vehicles</h1>
        <a href="registervehicle.jsp" class="btn btn-primary">Add New Vehicle</a>
    </header>

    <div id="error-message" class="error-message"></div>

    <div class="vehicle-grid" id="vehicle-list-container">
        <div id="loading-spinner" class="loading-spinner">
            <p>Loading your vehicles...</p>
            <!-- You can add a CSS spinner here if desired -->
        </div>
        <!-- Vehicle cards will be loaded here by JavaScript -->
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        loadVehicles();
    });

    function loadVehicles() {
        const loadingSpinner = document.getElementById('loading-spinner');
        const vehicleListContainer = document.getElementById('vehicle-list-container');
        const errorMessageDiv = document.getElementById('error-message');

        // Show loading spinner and clear previous content
        loadingSpinner.style.display = 'block';
        vehicleListContainer.innerHTML = ''; // Clear existing content
        vehicleListContainer.appendChild(loadingSpinner); // Re-add spinner if it was removed
        errorMessageDiv.style.display = 'none'; // Hide any previous error messages

        fetch('/vehicle?action=list', {
            method: 'POST', // Using POST as per VehicleServlet's doPost for actions
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        })
            .then(response => {
                if (!response.ok) {
                    // Attempt to read error message from response body
                    return response.json().then(err => { throw new Error(err.message || 'Network response was not ok'); });
                }
                return response.json();
            })
            .then(data => {
                loadingSpinner.style.display = 'none'; // Hide spinner

                if (data.length === 0) {
                    vehicleListContainer.innerHTML = `
                    <div class="empty-state">
                        <p>You haven't registered any vehicles yet.</p>
                        <p>Click "Add New Vehicle" to get started!</p>
                    </div>
                `;
                    return;
                }

                // Generate HTML for each vehicle card
                vehicleListContainer.innerHTML = data.map(vehicle => `
                <div class="vehicle-card">
                    <h3>${vehicle.numberplate}</h3>
                    <p><strong>Type:</strong> ${vehicle.vehicletype}</p>
                    <p><strong>District:</strong> ${vehicle.registereddistrict}</p>
                    <p><strong>Engine:</strong> ${vehicle.enginenumber} (${vehicle.enginecapacity}cc)</p>
                    <div class="card-actions">
                        <button class="btn btn-primary"
                                onclick="bookService('${vehicle.numberplate}')">
                            Book Service
                        </button>
                        <button class="btn btn-danger"
                                onclick="confirmDelete('${vehicle.numberplate}')">
                            Delete
                        </button>
                    </div>
                </div>
            `).join('');
            })
            .catch(error => {
                console.error('Error loading vehicles:', error);
                loadingSpinner.style.display = 'none'; // Hide spinner
                showError('Failed to load vehicles: ' + error.message);
            });
    }

    function bookService(numberplate) {
        console.log('Booking service for:', numberplate);
        // Redirect to a booking page or open a modal for booking
        alert(`Preparing to book service for vehicle: ${numberplate}`);
        // Example: window.location.href = `bookservice.jsp?numberplate=${numberplate}`;
    }

    function confirmDelete(numberplate) {
        if (confirm(`Are you sure you want to delete vehicle ${numberplate}?`)) {
            deleteVehicle(numberplate);
        }
    }

    function deleteVehicle(numberplate) {
        const params = new URLSearchParams();
        params.append('action', 'delete');
        params.append('numberplate', numberplate);

        fetch('/vehicle', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: params
        })
            .then(response => {
                if (!response.ok) {
                    return response.json().then(err => { throw new Error(err.message || 'Failed to delete vehicle'); });
                }
                return response.json();
            })
            .then(data => {
                if (data.status === 'success') {
                    alert('Vehicle deleted successfully!');
                    loadVehicles(); // Refresh the list after deletion
                } else {
                    showError(data.message || 'Failed to delete vehicle');
                }
            })
            .catch(error => {
                console.error('Error deleting vehicle:', error);
                showError('Failed to delete vehicle. Please try again. ' + error.message);
            });
    }

    function showError(message) {
        const errorElement = document.getElementById('error-message');
        errorElement.textContent = message;
        errorElement.style.display = 'block';

        // Hide error after 5 seconds
        setTimeout(() => {
            errorElement.style.display = 'none';
        }, 5000);
    }
</script>
</body>
</html>