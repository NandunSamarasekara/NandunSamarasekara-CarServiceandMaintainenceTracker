<%--
  Created by IntelliJ IDEA.
  User: Nandun Samarasekara
  Date: 6/25/2025
  Time: 3:55 PM
  To change this template use File | Settings | File Templates.
--%>
// Global variables
let users = [];

// DOM Content Loaded
document.addEventListener('DOMContentLoaded', function() {
// Check which page we're on
const path = window.location.pathname;

if (path.endsWith('user-list.html')) {
loadUsers();
} else if (path.endsWith('user-form.html')) {
setupUserForm();
}
});

// Load users for user-list.html
async function loadUsers() {
try {
const response = await fetch('/user?action=list');
if (!response.ok) throw new Error('Network response was not ok');

users = await response.json();
renderUserTable();
} catch (error) {
console.error('Error loading users:', error);
alert('Error loading users. Please try again.');
}
}

// Render user table
function renderUserTable() {
const tableBody = document.getElementById('userTableBody');
tableBody.innerHTML = '';

users.forEach(user => {
const row = document.createElement('tr');

row.innerHTML = `
<td>${user.nic}</td>
<td>${user.first_name}</td>
<td>${user.last_name}</td>
<td>${user.email}</td>
<td>${user.phone}</td>
<td>
    <a href="user-form.html?nic=${user.nic}" class="btn btn-warning">Edit</a>
    <button class="btn btn-danger" onclick="deleteUser('${user.nic}')">Delete</button>
</td>
`;

tableBody.appendChild(row);
});
}

// Setup user form
function setupUserForm() {
const form = document.getElementById('userForm');
const urlParams = new URLSearchParams(window.location.search);
const nic = urlParams.get('nic');

if (nic) {
// Edit mode
document.getElementById('formTitle').textContent = 'Edit User';
document.getElementById('formHeading').textContent = 'Edit User';
document.getElementById('nic').readOnly = true;

// Load user data
loadUserData(nic);
}

form.addEventListener('submit', function(e) {
e.preventDefault();
saveUser();
});
}

// Load user data for editing
async function loadUserData(nic) {
try {
const response = await fetch(`/user?action=edit&nic=${nic}`);
if (!response.ok) throw new Error('Network response was not ok');

const user = await response.json();

document.getElementById('nic').value = user.nic;
document.getElementById('nicHidden').value = user.nic;
document.getElementById('first_name').value = user.first_name;
document.getElementById('last_name').value = user.last_name;
document.getElementById('email').value = user.email;
document.getElementById('phone').value = user.phone;
document.getElementById('password').value = user.password;
} catch (error) {
console.error('Error loading user data:', error);
alert('Error loading user data. Please try again.');
}
}

// Save user (create or update)
async function saveUser() {
const form = document.getElementById('userForm');
const formData = new FormData(form);
const userData = Object.fromEntries(formData.entries());

const urlParams = new URLSearchParams(window.location.search);
const nic = urlParams.get('nic');

try {
const url = nic ? '/user?action=update' : '/user?action=insert';
const method = 'POST';

const response = await fetch(url, {
method: method,
headers: {
'Content-Type': 'application/json',
},
body: JSON.stringify(userData)
});

if (!response.ok) throw new Error('Network response was not ok');

window.location.href = 'user-list.html';
} catch (error) {
console.error('Error saving user:', error);
alert('Error saving user. Please try again.');
}
}

// Delete user
async function deleteUser(nic) {
if (!confirm('Are you sure you want to delete this user?')) return;

try {
const response = await fetch(`/user?action=delete&nic=${nic}`, {
method: 'POST'
});

if (!response.ok) throw new Error('Network response was not ok');

loadUsers(); // Refresh the list
} catch (error) {
console.error('Error deleting user:', error);
alert('Error deleting user. Please try again.');
}
}