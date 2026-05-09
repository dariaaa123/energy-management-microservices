#  Energy Management System - Distributed System

Microservices-based energy management system for real-time monitoring and management of energy consumption.

##  About the Project

Distributed system for electrical energy management that enables:
- Real-time energy consumption monitoring
- User and device management
- Automatic notifications for overconsumption
- Intelligent chatbot for customer support
- Real-time communication via WebSocket


##  Architecture

The system is built on a microservices architecture with the following components:

```
┌─────────────┐
│  Frontend   │ ← Nginx (React-style SPA)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Traefik   │ ← API Gateway & Reverse Proxy
└──────┬──────┘
       │
       ├─────────────────────────────────────────────┐
       │           │           │           │         │
       ▼           ▼           ▼           ▼         ▼
   ┌──────┐   ┌──────┐   ┌────────┐  ┌──────────┐ ┌──────────┐
   │ Auth │   │ User │   │ Device │  │Monitoring│ │WebSocket │
   │      │   │      │   │        │  │          │ │          │
   └───┬──┘   └───┬──┘   └────┬───┘  └────┬─────┘ └────┬─────┘
       │          │           │           │            │
       ▼          ▼           ▼           ▼            ▼
   ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐  ┌────────┐
   │auth-db │ │example-│ │device- │ │monitor-│  │websock-│
   │        │ │  db    │ │  db    │ │  db    │  │  db    │
   └────────┘ └────────┘ └────────┘ └────────┘  └────────┘
        └──────────────┬──────────────────┘
                       │
              ┌────────▼────────┐
              │   PostgreSQL    │ ← Database Server
              │    Instance     │
              └─────────────────┘
                       │
              ┌────────▼────────┐
              │    RabbitMQ     │ ← Message Broker (CloudAMQP)
              └─────────────────┘

```

### Data Flow

**1. Consumption Monitoring:**
```
Producer (Simulator) → RabbitMQ → Monitoring Service → PostgreSQL
                                         ↓
                                   (if > max)
                                         ↓
                                   RabbitMQ → WebSocket → Frontend
```

**2. Data Synchronization:**
```
User/Device Service → RabbitMQ → Monitoring Service → PostgreSQL
```

**3. Chat Support:**
```
User → WebSocket → Chat Service (Rules/AI) → WebSocket → User
                         ↓
                   (if needed)
                         ↓
                      Admin
```

##  Features

###  Foundation
-  Authentication and authorization (JWT)
-  User management (Admin/Client)
-  Device management
-  Complete CRUD for all entities
-  Data validation and error handling

###  Message-Oriented Middleware
-  Monitoring microservice
-  RabbitMQ integration (CloudAMQP)
-  Asynchronous data processing
-  Automatic hourly consumption calculation
-  Event synchronization between services
-  Device simulator (Python)

###  Real-Time Communication
-  WebSocket for real-time notifications
-  Automatic overconsumption alerts
-  Chatbot with 12 predefined rules
-  AI integration (Hugging Face) for complex responses
-  Bidirectional client-admin chat
-  Modern interface with notifications

##  Technologies

### Backend
- **Java 21**
- **Spring Boot 3.2.0 / 4.0.0-SNAPSHOT**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA** (Hibernate)
- **Spring AMQP** (RabbitMQ)
- **Spring WebSocket**
- **PostgreSQL 17**

### Message Broker
- **RabbitMQ** (CloudAMQP hosted)
- **AMQP Protocol** over TLS

### Frontend
- **HTML5, CSS3, JavaScript**
- **WebSocket API**
- **Fetch API**
- **Nginx Alpine**

### Infrastructure
- **Docker & Docker Compose**
- **Traefik v2.10** (Reverse Proxy)
- **Python 3.8+** (Device Simulator)

### AI Integration
- **Hugging Face Inference API**
- Model: `mistralai/Mistral-7B-Instruct-v0.2`

##  Installation and Running

### Prerequisites

- Docker Desktop installed and running
- Python 3.8+ (for simulator)
- Available ports: 80, 443, 5433, 8080-8086

### Quick Start

```powershell
# Clone the repository
git clone <repository-url>
cd ds2025_spring_example

# Build and start all services
docker-compose up --build -d

# Check container status
docker ps

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Application Access

- **Frontend**: http://localhost
- **Traefik Dashboard**: http://localhost:8080
- **PostgreSQL**: localhost:5433

### Default Credentials

**Admin:**
- Username: `admin`
- Password: `admin123`

**Test User:**
- Username: `maria`
- Password: `maria123`


### RabbitMQ

**Exchanges:**
- `data_exchange` - Device data
- `sync_exchange` - Entity synchronization
- `notification_exchange` - Notifications

**Queues:**
- `device_data_queue` - Device measurements
- `synchronization_queue` - Synchronization events
- `notification_queue` - Overconsumption alerts

##  Databases

The system uses PostgreSQL with 4 separate databases:

| Database | Service | Main Tables |
|----------|---------|-------------|
| auth-db | Authorization | users, roles |
| example-db | User Service | users, user_details |
| device-db | Device Service | devices |
| monitoring-db | Monitoring | device_measurements, hourly_energy_consumption, devices (sync), users (sync) |

##  Security

- **JWT Authentication** for all endpoints
- **Role-Based Access Control** (Admin/Client)
- **HTTPS ready** (Traefik configurable)
- **RabbitMQ over TLS** (amqps://)
- **CORS** configured for frontend
- **Password hashing** (BCrypt)




