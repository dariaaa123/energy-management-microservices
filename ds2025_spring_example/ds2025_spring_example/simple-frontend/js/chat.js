// Chat functionality
let selectedChatUser = null;
let chatHistory = {};

function handleUserChatMessage(message) {
    const messagesDiv = document.getElementById('chatMessages');
    const typingIndicator = document.getElementById('typingIndicator');
    
    if (message.type === 'TYPING') {
        typingIndicator.classList.remove('hidden');
        setTimeout(() => typingIndicator.classList.add('hidden'), 3000);
        return;
    }
    
    if (message.type === 'BOT_RESPONSE' || message.type === 'ADMIN_MESSAGE') {
        const msgDiv = document.createElement('div');
        msgDiv.className = `chat-message ${message.type === 'BOT_RESPONSE' ? 'bot' : 'admin'}`;
        msgDiv.innerHTML = `
            <div class="sender">${message.senderName || (message.type === 'BOT_RESPONSE' ? 'Support Bot' : 'Admin')}</div>
            <div class="bubble">${message.content}</div>
            <div class="time">${new Date().toLocaleTimeString()}</div>
        `;
        messagesDiv.appendChild(msgDiv);
        messagesDiv.scrollTop = messagesDiv.scrollHeight;
    }
}

function handleAdminChatMessage(message) {
    if (message.type === 'USER_ONLINE' || message.type === 'ACTIVE_USERS') {
        updateUserList(message);
        return;
    }
    
    if (message.type === 'USER_MESSAGE' || message.type === 'HELP_NEEDED') {
        const userId = message.senderId;
        if (!chatHistory[userId]) {
            chatHistory[userId] = [];
        }
        chatHistory[userId].push(message);
        
        updateUserList();
        
        if (selectedChatUser === userId) {
            displayAdminChatMessages(userId);
        }
    }
}

function updateUserList(message) {
    const userListDiv = document.getElementById('userList');
    
    if (message && message.type === 'USER_ONLINE') {
        if (!chatHistory[message.senderId]) {
            chatHistory[message.senderId] = [];
        }
    }
    
    const users = Object.keys(chatHistory);
    if (users.length === 0) {
        userListDiv.innerHTML = '<span style="color: #95a5a6;">No active users</span>';
        return;
    }
    
    userListDiv.innerHTML = users.map(userId => {
        const hasUnread = chatHistory[userId].some(m => !m.read);
        const userName = chatHistory[userId][0]?.senderName || userId.substring(0, 8);
        return `<div class="user-chip ${selectedChatUser === userId ? 'active' : ''} ${hasUnread ? 'has-message' : ''}" 
                    onclick="selectChatUser('${userId}')">${userName}</div>`;
    }).join('');
}

function selectChatUser(userId) {
    if (selectedChatUser === userId) {
        selectedChatUser = null;
        updateUserList();
        displayAdminChatMessages(null);
        return;
    }
    
    selectedChatUser = userId;
    if (chatHistory[userId]) {
        chatHistory[userId].forEach(m => m.read = true);
    }
    updateUserList();
    displayAdminChatMessages(userId);
}

function displayAdminChatMessages(userId) {
    const messagesDiv = document.getElementById('adminChatMessages');
    
    if (!userId) {
        messagesDiv.innerHTML = '<p style="color: #95a5a6; text-align: center; margin-top: 100px;">Select a user to view messages</p>';
        return;
    }
    
    const messages = chatHistory[userId] || [];
    
    messagesDiv.innerHTML = messages.map(msg => `
        <div class="chat-message ${msg.type === 'ADMIN_MESSAGE' ? 'user' : 'bot'}">
            <div class="sender">${msg.senderName || 'User'}</div>
            <div class="bubble">${msg.content}</div>
            <div class="time">${msg.timestamp ? new Date(msg.timestamp).toLocaleTimeString() : ''}</div>
        </div>
    `).join('');
    
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function sendChatMessage() {
    const input = document.getElementById('chatInput');
    const content = input.value.trim();
    if (!content || !chatWs) return;
    
    const message = {
        type: 'USER_MESSAGE',
        senderId: window.currentUserId,
        senderName: window.currentUsername,
        content: content,
        isAdmin: false
    };
    
    chatWs.send(JSON.stringify(message));
    
    const messagesDiv = document.getElementById('chatMessages');
    const msgDiv = document.createElement('div');
    msgDiv.className = 'chat-message user';
    msgDiv.innerHTML = `
        <div class="sender">You</div>
        <div class="bubble">${content}</div>
        <div class="time">${new Date().toLocaleTimeString()}</div>
    `;
    messagesDiv.appendChild(msgDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
    
    input.value = '';
}

function sendAdminMessage() {
    const input = document.getElementById('adminChatInput');
    const content = input.value.trim();
    if (!content || !chatWs || !selectedChatUser) {
        alert('Please select a user first');
        return;
    }
    
    const message = {
        type: 'ADMIN_MESSAGE',
        senderId: window.currentUserId,
        senderName: window.currentUsername,
        recipientId: selectedChatUser,
        content: content,
        isAdmin: true
    };
    
    chatWs.send(JSON.stringify(message));
    
    if (!chatHistory[selectedChatUser]) {
        chatHistory[selectedChatUser] = [];
    }
    chatHistory[selectedChatUser].push(message);
    displayAdminChatMessages(selectedChatUser);
    
    input.value = '';
}

function toggleChat() {
    const chatBox = document.getElementById('chatBox');
    chatBox.classList.toggle('open');
}
