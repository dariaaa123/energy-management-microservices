# Assignment 3 - Real-time Communication & Customer Support

## Overview

This assignment extends the Energy Management System with real-time communication capabilities:

1. **WebSocket Microservice** - Real-time notifications and chat transport
2. **Customer Support (Chat) Microservice** - Rule-based chatbot with AI fallback
3. **Overconsumption Alerts** - Real-time notifications when devices exceed limits
4. **Client-to-Admin Chat** - Bidirectional real-time messaging

## Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Frontend (Nginx)                             │
│                      http://localhost                                │
│   - WebSocket client for notifications (/ws/notifications)          │
│   - Chat widget for customer support (/ws/chat)                     │
│   - Real-time notification display                                  │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Traefik Reverse Proxy                             │
│                    (API Gateway - Port 80)                           │
│         Routes: /api/*, /ws/* to appropriate services               │
└─┬──────────┬──────────┬──────────┬──────────┬──────────┬───────────┘
  │          │          │          │          │          │
  ▼          ▼          ▼          ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐ ┌────────┐ ┌────────┐
│  Auth  │ │  User  │ │ Device │ │Monitoring│ │WebSocket│ │  Chat  │
│Service │ │Service │ │Service │ │ Service  │ │Service │ │Service │
│ :8083  │ │ :8081  │ │ :8082  │ │  :8084   │ │ :8085  │ │ :8086  │
└────────┘ └────────┘ └────────┘ └────┬─────┘ └───┬────┘ └────┬───┘
                                      │           │           │
                                      │    RabbitMQ (CloudAMQP)
                                      │           │           │
                                      ▼           ▼           │
                              ┌───────────────────────┐       │
                              │  notification_exchange │       │
                              │  notification_queue    │◄──────┘
                              └───────────────────────┘
```

## Message Flow

### Overconsumption Notification Flow
```
Producer → RabbitMQ(data_exchange) → MonitoringService
                                          │
                                          ▼ (if consumption > max)
                                    NotificationPublisher
                                          │
                                          ▼
                              RabbitMQ(notification_exchange)
                                          │
                                          ▼
                              WebSocketService(NotificationConsumer)
                                          │
                                          ▼
                              WebSocket → Frontend (User sees alert)
```

### Chat Message Flow
```
User types message → WebSocket → WebSocketService
                                      │
                                      ▼
                                 ChatService
                                      │
                    ┌─────────────────┼─────────────────┐
                    ▼                 ▼                 ▼
              Rule Match?        AI Response?      Fallback
                    │                 │                 │
                    └─────────────────┼─────────────────┘
                                      ▼
                              WebSocketService
                                      │
                                      ▼
                              WebSocket → User sees response
```

## New Services

### 1. WebSocket Service (Port 8085)

Handles real-time WebSocket connections for:
- **Overconsumption Notifications**: Receives alerts from RabbitMQ and pushes to connected clients
- **Chat Message Transport**: Routes messages between users, bot, and admins

**WebSocket Endpoints:**
- `ws://localhost/ws/notifications?userId={userServiceId}` - Notification WebSocket
- `ws://localhost/ws/chat` - Chat WebSocket

**REST Endpoints:**
- `POST /api/ws/send-to-user` - Send message to specific user
- `POST /api/ws/send-to-admins` - Broadcast to all admins
- `GET /api/ws/health` - Health check

**Key Files:**
- `NotificationWebSocketHandler.java` - Manages notification connections
- `ChatWebSocketHandler.java` - Manages chat connections
- `NotificationConsumer.java` - RabbitMQ consumer for alerts

### 2. Chat Service (Port 8086)

Provides customer support functionality:
- **Rule-based Chatbot**: 12 predefined rules for common questions
- **AI Fallback**: Hugging Face API integration for complex queries
- **Admin Notification**: Forwards unresolved queries to human admins

**Endpoints:**
- `POST /api/chat/process` - Process user message (main endpoint)
- `POST /api/chat/test-rules` - Test rule matching
- `POST /api/chat/test-ai` - Test AI response
- `GET /api/chat/health` - Health check with AI status

**Key Files:**
- `RuleBasedChatService.java` - 12 keyword-based rules
- `HuggingFaceAIService.java` - AI response generation
- `ChatService.java` - Orchestrates rule → AI → fallback flow

### 3. Monitoring Service Updates

Added overconsumption detection:
- Calculates hourly consumption per device
- Compares against device's `maximum_consumption_value`
- Publishes alerts to RabbitMQ when limit exceeded
- Uses `user_id` from device to route notifications

**Key Files:**
- `MonitoringService.java` - `checkOverconsumption()` method
- `NotificationPublisher.java` - Publishes to RabbitMQ

## Chatbot Rules (12 Rules)

| # | Keywords | Response Topic |
|---|----------|----------------|
| 1 | hello, hi, hey, good morning | Greeting |
| 2 | login, password, account, sign in | Login/Account issues |
| 3 | device, add device, sensor | Device management |
| 4 | consumption, energy, usage, power | Energy consumption |
| 5 | alert, notification, warning, overconsumption | Alerts explanation |
| 6 | bill, billing, payment, cost, price | Billing inquiries |
| 7 | error, bug, problem, issue, not working | Technical support |
| 8 | human, agent, admin, operator, speak | Connect to admin |
| 9 | hours, time, clock, available, schedule, when | Support hours |
| 10 | thank, thanks, bye, goodbye | Goodbye |
| 11 | how to, help, guide, tutorial | Usage guide |
| 12 | security, privacy, data, safe | Security info |

## AI-Driven Customer Support

When no rule matches, the system uses AI to generate contextual responses:

**AI Response Topics:**
- Energy saving tips
- Bill reduction advice
- Peak hours information
- Smart home automation
- Solar panel information
- Winter/Summer energy tips

**Configuration:**
```yaml
# docker-compose.yml
environment:
  HUGGINGFACE_TOKEN: your_token_here
  AI_ENABLED: "true"
```

## Build & Run

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local development)
- Python 3.x (for testing scripts)
- CloudAMQP account (RabbitMQ)

### Quick Start

```powershell
# Navigate to project directory
cd ds2025_spring_example

# Build and start all services
docker-compose up --build -d

# Wait for services to start (30-60 seconds)
# Check status
docker ps

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Service Startup Order
1. PostgreSQL (database)
2. Traefik (reverse proxy)
3. Auth, User, Device Services
4. Monitoring Service
5. WebSocket Service
6. Chat Service
7. Frontend

### Enable AI Support

1. Get a Hugging Face token from https://huggingface.co/settings/tokens
2. Edit `docker-compose.yml`:
```yaml
chat-service:
  environment:
    HUGGINGFACE_TOKEN: hf_your_token_here
    AI_ENABLED: "true"
```
3. Restart chat service:
```powershell
docker-compose up -d --force-recreate chat-service
```

## Testing

### 1. Test Overconsumption Notifications

**Option A: Using test script**
```powershell
# Run the test script (sends high consumption value)
python test_overconsumption.py
```

**Option B: Manual testing**
1. Login as a user (e.g., maria) in the browser
2. The user must have devices with `maximum_consumption_value` set
3. Run producer with high values or use test script
4. Watch for notification popup in top-right corner

**Verify in logs:**
```powershell
# Check monitoring service detected overconsumption
docker logs ds2025-monitoring-service | Select-String "OVERCONSUMPTION"

# Check websocket service received and sent notification
docker logs ds2025-websocket-service | Select-String "notification"
```

### 2. Test Rule-Based Chat

1. Login as any user
2. Click the chat bubble (💬) in bottom-right corner
3. Test these messages:

| Message | Expected Response Type |
|---------|----------------------|
| "Hello" | Greeting |
| "How do I add a device?" | Device management |
| "What time is it?" | Support hours |
| "I can't login" | Login help |
| "Thanks!" | Goodbye |

**Test via API:**
```powershell
Invoke-RestMethod -Uri "http://localhost/api/chat/test-rules" `
  -Method POST -ContentType "application/json" `
  -Body '{"message": "hello"}'
```

### 3. Test AI Chat

Send a message that doesn't match any rule:
- "What are the best solar panels for my house?"
- "How does electricity pricing work?"
- "Tell me about renewable energy"

**Test via API:**
```powershell
Invoke-RestMethod -Uri "http://localhost/api/chat/test-ai" `
  -Method POST -ContentType "application/json" `
  -Body '{"message": "What are the best solar panels?"}'
```

### 4. Test Client-to-Admin Chat

1. Open two browser windows/tabs
2. Login as admin in one, as client in another
3. Client sends message → appears in admin panel
4. Admin clicks on client name → sees messages
5. Admin responds → client receives response

## Service Ports

| Service | Internal Port | External Access |
|---------|--------------|-----------------|
| Traefik Dashboard | 8080 | http://localhost:8080 |
| Frontend | 80 | http://localhost |
| Auth Service | 8083 | /api/auth/* |
| User Service | 8081 | /api/users/* |
| Device Service | 8082 | /api/devices/* |
| Monitoring Service | 8084 | /api/monitoring/* |
| WebSocket Service | 8085 | /ws/*, /api/ws/* |
| Chat Service | 8086 | /api/chat/* |
| PostgreSQL | 5432 | localhost:5433 |

## RabbitMQ Configuration

**CloudAMQP URL:** `amqps://jjzdoqha:***@cow.rmq2.cloudamqp.com/jjzdoqha`

| Exchange | Queue | Purpose |
|----------|-------|---------|
| data_exchange | device_data_queue | Device measurements |
| synchronization_exchange | synchronization_queue | User/Device sync |
| notification_exchange | notification_queue | Overconsumption alerts |

## Database Schema Updates

### monitoring-db.devices
```sql
CREATE TABLE devices (
    device_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    maximum_consumption_value DOUBLE,
    user_id VARCHAR(255)  -- Links to user-service UUID
);
```

## Troubleshooting

### WebSocket Connection Issues
```powershell
# Check WebSocket service logs
docker logs ds2025-websocket-service --tail 50

# Verify service is running
docker ps | Select-String "websocket"

# Test WebSocket endpoint
# Open browser console and check for connection messages
```

### Chat Not Responding
```powershell
# Check Chat service logs
docker logs ds2025-chat-service --tail 50

# Test health endpoint
Invoke-RestMethod http://localhost/api/chat/health

# Test rules directly
Invoke-RestMethod -Uri "http://localhost/api/chat/test-rules" `
  -Method POST -ContentType "application/json" `
  -Body '{"message":"hello"}'
```

### No Notifications Received
1. Verify user is logged in and WebSocket connected (check browser console)
2. Check device has `maximum_consumption_value` set
3. Check device has correct `user_id` in monitoring-db
4. Verify RabbitMQ connection:
```powershell
docker logs ds2025-monitoring-service | Select-String "RabbitMQ"
docker logs ds2025-websocket-service | Select-String "RabbitMQ"
```

### AI Not Working
```powershell
# Check if AI is enabled
Invoke-RestMethod http://localhost/api/chat/health

# Check environment variables
docker exec ds2025-chat-service printenv | Select-String "AI_ENABLED|HUGGING"

# Check logs for API errors
docker logs ds2025-chat-service | Select-String "Hugging"
```

## Files Structure

```
ds2025_spring_example/
├── WebSocketService/           # NEW - WebSocket microservice
│   └── src/main/java/com/energy/websocket/
│       ├── handler/
│       │   ├── NotificationWebSocketHandler.java
│       │   ├── ChatWebSocketHandler.java
│       │   └── ChatServiceClient.java
│       ├── consumer/
│       │   └── NotificationConsumer.java
│       ├── config/
│       │   ├── WebSocketConfig.java
│       │   └── RabbitMQConfig.java
│       └── dto/
│           ├── NotificationMessage.java
│           └── ChatMessage.java
│
├── ChatService/                # NEW - Chat microservice
│   └── src/main/java/com/energy/chat/
│       ├── service/
│       │   ├── ChatService.java
│       │   ├── RuleBasedChatService.java
│       │   └── HuggingFaceAIService.java
│       ├── controller/
│       │   └── ChatController.java
│       └── dto/
│           └── ChatMessage.java
│
├── MonitoringMicroservice/     # MODIFIED
│   └── src/main/java/com/energy/monitoring/
│       └── service/
│           ├── MonitoringService.java      # Added checkOverconsumption()
│           └── NotificationPublisher.java  # NEW
│
├── simple-frontend/            # MODIFIED
│   └── index.html              # Added WebSocket client, chat UI
│
├── docker-compose.yml          # MODIFIED - Added new services
├── test_overconsumption.py     # NEW - Test script
└── ASSIGNMENT3_README.md       # This file
```

## Technology Stack

| Component | Technology |
|-----------|------------|
| Backend | Java 21, Spring Boot 3.2.0 |
| WebSocket | Spring WebSocket |
| Message Broker | RabbitMQ (CloudAMQP) |
| AI | Hugging Face Inference API |
| Database | PostgreSQL 17 |
| Frontend | Vanilla JavaScript, WebSocket API |
| Reverse Proxy | Traefik v2.10 |
| Containerization | Docker, Docker Compose |

## Security Considerations

- JWT authentication for REST endpoints
- WebSocket connections validated with userId
- CORS configured for frontend access
- API tokens stored as environment variables (not in code)
- RabbitMQ uses SSL (amqps://)

## Author

DS2025 - Distributed Systems Assignment 3
