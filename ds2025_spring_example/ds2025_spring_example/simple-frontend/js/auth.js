// Authentication functions
async function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorDiv = document.getElementById('loginError');

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) throw new Error('Login failed');

        const data = await response.json();
        window.token = data.token;
        window.userRole = data.role;
        
        localStorage.setItem('token', window.token);
        localStorage.setItem('role', window.userRole);
        localStorage.setItem('username', data.username);
        localStorage.setItem('userId', data.userId);
        
        window.currentUserId = data.userId;
        window.currentUsername = data.username;

        errorDiv.classList.add('hidden');
        showAdminPage();
    } catch (error) {
        errorDiv.textContent = 'Invalid username or password';
        errorDiv.classList.remove('hidden');
    }
}

function logout() {
    localStorage.clear();
    if (notificationWs) notificationWs.close();
    if (chatWs) chatWs.close();
    window.token = null;
    window.userRole = null;
    document.getElementById('adminPage').classList.add('hidden');
    document.getElementById('loginPage').classList.remove('hidden');
    location.reload();
}

async function showAdminPage() {
    document.getElementById('loginPage').classList.add('hidden');
    document.getElementById('adminPage').classList.remove('hidden');
    
    window.userRole = localStorage.getItem('role');
    window.currentUsername = localStorage.getItem('username');
    window.currentUserId = localStorage.getItem('userId');
    
    await loadUsers();
    
    const currentUser = window.allUsers.find(u => u.username === window.currentUsername);
    if (currentUser) {
        localStorage.setItem('userServiceId', currentUser.id);
    }
    
    connectWebSockets();
    
    if (window.userRole === 'CLIENT') {
        const usersCard = document.getElementById('usersCard');
        if (usersCard) usersCard.style.display = 'none';
        
        document.querySelector('h1').textContent = 'My Devices - ' + window.currentUsername;
        document.getElementById('devicesTitle').textContent = 'My Assigned Devices';
        document.getElementById('assignedUserHeader').style.display = 'none';
        document.getElementById('energyVisualizationCard').style.display = 'block';
        document.getElementById('chatWidget').style.display = 'block';
        document.getElementById('adminChatPanel').style.display = 'none';
        document.getElementById('dateSelect').valueAsDate = new Date();
        
        await loadDevices();
    } else {
        document.querySelector('h1').textContent = 'Energy Management System - Admin';
        document.getElementById('devicesTitle').textContent = 'Devices Management';
        document.getElementById('assignedUserHeader').style.display = 'table-cell';
        document.getElementById('energyVisualizationCard').style.display = 'none';
        document.getElementById('chatWidget').style.display = 'none';
        document.getElementById('adminChatPanel').style.display = 'block';
        
        await loadDevices();
    }
}
