// User management functions
async function loadUsers() {
    try {
        const response = await fetch('/api/users', {
            headers: { 'Authorization': `Bearer ${window.token}` }
        });
        
        if (!response.ok) throw new Error('Failed to load users');
        
        window.allUsers = await response.json();
        
        const tbody = document.getElementById('usersBody');
        if (tbody && window.userRole === 'ADMIN') {
            tbody.innerHTML = window.allUsers.map(user => `
                <tr>
                    <td>${user.id.substring(0, 8)}...</td>
                    <td>${user.username || 'N/A'}</td>
                    <td>${user.name || 'N/A'}</td>
                    <td>${user.role || 'N/A'}</td>
                    <td>${user.address || 'N/A'}</td>
                    <td>${user.age || 'N/A'}</td>
                    <td>
                        <button class="btn-small btn-warning" onclick='editUser(${JSON.stringify(user)})'>Edit</button>
                        <button class="btn-small btn-danger" onclick="deleteUser('${user.id}')">Delete</button>
                    </td>
                </tr>
            `).join('');
        }
    } catch (error) {
        const usersError = document.getElementById('usersError');
        if (usersError) {
            usersError.textContent = 'Failed to load users: ' + error.message;
            usersError.classList.remove('hidden');
        }
    }
}

function showCreateUserModal() {
    document.getElementById('userModalTitle').textContent = 'Create User';
    document.getElementById('userId').value = '';
    document.getElementById('userName').value = '';
    document.getElementById('userUsername').value = '';
    document.getElementById('userPassword').value = '';
    document.getElementById('userRole').value = '';
    document.getElementById('userAge').value = '';
    document.getElementById('userAddress').value = '';
    document.getElementById('userModal').style.display = 'block';
}

function editUser(user) {
    document.getElementById('userModalTitle').textContent = 'Edit User';
    document.getElementById('userId').value = user.id;
    document.getElementById('userName').value = user.name || '';
    document.getElementById('userUsername').value = user.username || '';
    document.getElementById('userPassword').value = '';
    document.getElementById('userRole').value = user.role || '';
    document.getElementById('userAge').value = user.age || '';
    document.getElementById('userAddress').value = user.address || '';
    document.getElementById('userModal').style.display = 'block';
}

function closeUserModal() { 
    document.getElementById('userModal').style.display = 'none'; 
}

async function saveUser() {
    const id = document.getElementById('userId').value;
    const userData = {
        name: document.getElementById('userName').value,
        username: document.getElementById('userUsername').value,
        password: document.getElementById('userPassword').value,
        role: document.getElementById('userRole').value,
        age: parseInt(document.getElementById('userAge').value),
        address: document.getElementById('userAddress').value
    };

    try {
        const url = id ? `/api/users/${id}` : '/api/users';
        const method = id ? 'PUT' : 'POST';
        
        const response = await fetch(url, {
            method,
            headers: { 
                'Content-Type': 'application/json', 
                'Authorization': `Bearer ${window.token}` 
            },
            body: JSON.stringify(userData)
        });

        if (!response.ok) throw new Error('Failed to save user');
        closeUserModal();
        loadUsers();
    } catch (error) {
        alert('Failed to save user: ' + error.message);
    }
}

async function deleteUser(id) {
    if (!confirm('Delete this user?')) return;
    try {
        const response = await fetch(`/api/users/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${window.token}` }
        });
        if (!response.ok) throw new Error('Failed to delete');
        loadUsers();
    } catch (error) {
        alert('Failed to delete user: ' + error.message);
    }
}
