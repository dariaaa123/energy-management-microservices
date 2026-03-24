// WebSocket connections
let notificationWs = null;
let chatWs = null;

function connectWebSockets() {
    const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsHost = window.location.host;
    
    connectNotificationWs(wsProtocol, wsHost);
    connectChatWs(wsProtocol, wsHost);
}

function connectNotificationWs(protocol, host) {
    const userId = localStorage.getItem('userServiceId') || window.currentUserId || localStorage.getItem('userId');
    if (!userId) return;
    
    notificationWs = new WebSocket(`${protocol}//${host}/ws/notifications?userId=${userId}`);
    
    notificationWs.onopen = () => {
        console.log('Notification WebSocket connected with userId:', userId);
        notificationWs.send(JSON.stringify({ type: 'REGISTER', userId: userId }));
    };
    
    notificationWs.onmessage = (event) => {
        const notification = JSON.parse(event.data);
        console.log('Received notification:', notification);
        showNotification(notification);
    };
    
    notificationWs.onclose = () => {
        console.log('Notification WebSocket disconnected, reconnecting...');
        setTimeout(() => connectNotificationWs(protocol, host), 3000);
    };
    
    notificationWs.onerror = (error) => {
        console.error('Notification WebSocket error:', error);
    };
}

function connectChatWs(protocol, host) {
    chatWs = new WebSocket(`${protocol}//${host}/ws/chat`);
    
    chatWs.onopen = () => {
        console.log('Chat WebSocket connected');
        const isAdmin = window.userRole === 'ADMIN';
        chatWs.send(JSON.stringify({
            type: 'REGISTER',
            senderId: window.currentUserId,
            senderName: window.currentUsername,
            isAdmin: isAdmin
        }));
    };
    
    chatWs.onmessage = (event) => {
        const message = JSON.parse(event.data);
        console.log('Received chat message:', message);
        handleChatMessage(message);
    };
    
    chatWs.onclose = () => {
        console.log('Chat WebSocket disconnected, reconnecting...');
        setTimeout(() => connectChatWs(protocol, host), 3000);
    };
}

function showNotification(notification) {
    const container = document.getElementById('notificationContainer');
    const notifDiv = document.createElement('div');
    notifDiv.className = 'notification';
    
    if (notification.type === 'OVERCONSUMPTION_ALERT') {
        notifDiv.className += ' warning';
    }
    
    notifDiv.innerHTML = `
        <div>
            <strong>${notification.type === 'OVERCONSUMPTION_ALERT' ? '⚠️ Overconsumption Alert' : 'Notification'}</strong>
            <p style="margin-top: 5px;">${notification.message}</p>
            <small style="opacity: 0.8;">${notification.deviceName || ''}</small>
        </div>
        <span class="notification-close" onclick="this.parentElement.remove()">×</span>
    `;
    
    container.appendChild(notifDiv);
    
    setTimeout(() => {
        if (notifDiv.parentElement) {
            notifDiv.remove();
        }
    }, 10000);
    
    try {
        const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2teleQAA');
        audio.volume = 0.3;
        audio.play().catch(() => {});
    } catch(e) {}
}

function handleChatMessage(message) {
    if (window.userRole === 'ADMIN') {
        handleAdminChatMessage(message);
    } else {
        handleUserChatMessage(message);
    }
}
