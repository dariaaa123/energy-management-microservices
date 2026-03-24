// Device management functions
async function loadDevices() {
    try {
        const response = await fetch('/api/devices');
        if (!response.ok) throw new Error('Failed to load devices');
        
        let devices = await response.json();
        
        let clientUserUUID = null;
        if (window.userRole === 'CLIENT' && window.allUsers.length > 0) {
            const currentUser = window.allUsers.find(u => u.username === window.currentUsername);
            if (currentUser) {
                clientUserUUID = currentUser.id;
            }
        }
        
        if (window.userRole === 'CLIENT') {
            if (clientUserUUID) {
                devices = devices.filter(device => device.userId === clientUserUUID);
                window.clientDevices = devices;
                populateDeviceDropdown(devices);
            } else {
                devices = [];
                window.clientDevices = [];
            }
            const createBtn = document.querySelector('#devicesCard .btn-success');
            if (createBtn) createBtn.style.display = 'none';
        } else {
            const createBtn = document.querySelector('#devicesCard .btn-success');
            if (createBtn) createBtn.style.display = 'inline-block';
        }
        
        const tbody = document.getElementById('devicesBody');
        tbody.innerHTML = devices.map(device => {
            const assignedUser = device.userId ? window.allUsers.find(u => u.id === device.userId) : null;
            const userName = assignedUser ? assignedUser.name : (device.userId ? 'Unknown User' : 'Unassigned');
            
            const actionButtons = window.userRole === 'ADMIN' ? `
                <button class="btn-small btn-warning" onclick='editDevice(${JSON.stringify(device)})'>Edit</button>
                <button class="btn-small btn-success" onclick="showAssignModal('${device.id}')">Assign</button>
                <button class="btn-small btn-danger" onclick="deleteDevice('${device.id}')">Delete</button>
            ` : '-';
            
            const assignedUserCell = window.userRole === 'ADMIN' ? `<td>${userName}</td>` : '';
            
            return `
            <tr>
                <td>${device.id.substring(0, 8)}...</td>
                <td>${device.name || 'N/A'}</td>
                <td>${device.maximumConsumptionValue || 'N/A'}</td>
                ${assignedUserCell}
                <td>${actionButtons}</td>
            </tr>
        `}).join('');
    } catch (error) {
        document.getElementById('devicesError').textContent = 'Failed to load devices: ' + error.message;
        document.getElementById('devicesError').classList.remove('hidden');
    }
}

function showCreateDeviceModal() {
    document.getElementById('deviceModalTitle').textContent = 'Create Device';
    document.getElementById('deviceId').value = '';
    document.getElementById('deviceDescription').value = '';
    document.getElementById('deviceMaxConsumption').value = '';
    document.getElementById('deviceModal').style.display = 'block';
}

function editDevice(device) {
    document.getElementById('deviceModalTitle').textContent = 'Edit Device';
    document.getElementById('deviceId').value = device.id;
    document.getElementById('deviceDescription').value = device.name || '';
    document.getElementById('deviceMaxConsumption').value = device.maximumConsumptionValue || '';
    document.getElementById('deviceModal').style.display = 'block';
}

function closeDeviceModal() { 
    document.getElementById('deviceModal').style.display = 'none'; 
}

async function saveDevice() {
    const id = document.getElementById('deviceId').value;
    const deviceData = {
        name: document.getElementById('deviceDescription').value,
        maximumConsumptionValue: parseInt(document.getElementById('deviceMaxConsumption').value)
    };

    try {
        const url = id ? `/api/devices/${id}` : '/api/devices';
        const method = id ? 'PUT' : 'POST';
        const role = window.userRole || localStorage.getItem('role') || 'USER';
        
        const response = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${window.token}`,
                'X-User-Role': role
            },
            body: JSON.stringify(deviceData)
        });

        if (!response.ok) throw new Error('Failed to save device');
        closeDeviceModal();
        loadDevices();
    } catch (error) {
        alert('Failed to save device: ' + error.message);
    }
}

async function deleteDevice(id) {
    if (!confirm('Delete this device?')) return;
    try {
        const response = await fetch(`/api/devices/${id}`, {
            method: 'DELETE',
            headers: { 
                'Authorization': `Bearer ${window.token}`,
                'X-User-Role': window.userRole || localStorage.getItem('role') || 'USER'
            }
        });
        if (!response.ok) throw new Error('Failed to delete');
        loadDevices();
    } catch (error) {
        alert('Failed to delete device: ' + error.message);
    }
}

function showAssignModal(deviceId) {
    document.getElementById('assignDeviceId').value = deviceId;
    const select = document.getElementById('assignUserId');
    select.innerHTML = '<option value="">Select User</option>' + 
        window.allUsers.map(user => `<option value="${user.id}">${user.name} (Age: ${user.age})</option>`).join('');
    document.getElementById('assignModal').style.display = 'block';
}

function closeAssignModal() { 
    document.getElementById('assignModal').style.display = 'none'; 
}

async function assignDevice() {
    const deviceId = document.getElementById('assignDeviceId').value;
    const userId = document.getElementById('assignUserId').value;

    if (!userId) { 
        alert('Please select a user'); 
        return; 
    }

    try {
        const response = await fetch(`/api/devices/${deviceId}/assign/${userId}`, {
            method: 'PUT',
            headers: { 
                'Authorization': `Bearer ${window.token}`,
                'X-User-Role': window.userRole || localStorage.getItem('role') || 'USER'
            }
        });

        if (!response.ok) throw new Error('Failed to assign device');
        closeAssignModal();
        loadDevices();
    } catch (error) {
        alert('Failed to assign device: ' + error.message);
    }
}
