// Global state
window.token = localStorage.getItem('token');
window.userRole = localStorage.getItem('role');
window.currentUserId = localStorage.getItem('userId');
window.currentUsername = localStorage.getItem('username');
window.allUsers = [];
window.clientDevices = [];

// Initialize app
if (window.token) {
    showAdminPage();
}
