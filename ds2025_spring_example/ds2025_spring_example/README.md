# Energy Management System - Distributed Systems Assignment

## Project Overview

This is a distributed Energy Management System built with microservices architecture that allows authenticated users to access, monitor, and manage smart energy metering devices. The system implements role-based access control with two user types: Administrators and Clients.

### Key Features
- **Role-Based Access Control**: Admin and Client roles with different permissions
- **Microservices Architecture**: Loosely coupled services for User Management, Device Management, and Authentication
- **Reverse Proxy**: Traefik API Gateway for request routing and load balancing
- **Containerized Deployment**: Full Docker-based deployment with docker-compose
- **RESTful APIs**: All microservices expose REST endpoints
- **JWT Authentication**: Secure token-based authentication
- **Database Persistence**: PostgreSQL databases for each service

---

## System Architecture

### Components

1. **Frontend Application** (Nginx + HTML/JavaScript)
   - Browser-based interface for user interactions
   - Admin dashboard for CRUD operations
   - Client dashboard for viewing assigned devices

2. **Traefik Reverse Proxy & API Gateway**
   - Entry point for all HTTP requests
   - Routes requests to appropriate microservices
   - Provides load balancing and service discovery

3. **Authorization Service** (Spring Boot - Port 8083)
   - Handles user login and registration
   - Generates and validates JWT tokens
   - Manages authentication credentials

4. **User Management Microservice** (Spring Boot - Port 8081)
   - CRUD operations for user accounts
   - Stores user profile data (name, address, age, username, role)
   - Syncs credentials with Authorization Service

5. **Device Management Microservice** (Spring Boot - Port 8082)
   - CRUD operations for devices
   - Device-to-user assignment
   - Stores device data (name, maximum consumption value)

6. **PostgreSQL Database**
   - Shared database server with separate databases:
     - `auth-db`: Authentication credentials
     - `example-db`: Users and devices data

---

## Technologies Used

### Backend
- **Java 21** with **Spring Boot 4.0.0-SNAPSHOT**
- **Spring Data JPA** for ORM
- **Spring Security** for authentication
- **JWT (JSON Web Tokens)** for stateless authentication
- **PostgreSQL 17** for data persistence
- **Maven** for dependency management

### Frontend
- **HTML5/CSS3/JavaScript** (Vanilla JS)
- **Nginx Alpine** for serving static files and API proxying

### Infrastructure
- **Docker** & **Docker Compose** for containerization
- **Traefik v2.10** as reverse proxy and API gateway
- **Docker Networks** for service isolation

---

## Prerequisites

Before running this project, ensure you have the following installed:

- **Docker Desktop** (version 20.10 or higher)
  - Download from: https://www.docker.com/products/docker-desktop
- **Docker Compose** (version 2.0 or higher)
  - Included with Docker Desktop
- **Git** (for cloning the repository)
- **8GB RAM minimum** (recommended for running all containers)
- **Ports Available**: 80, 443, 5433, 8080

### Verify Installation

```bash
docker --version
docker-compose --version
```

---

## Build and Deployment Instructions

### Step 1: Clone the Repository

```bash
git clone <repository-url>
cd ds2025_spring_example/ds2025_spring_example
```

### Step 2: Build All Services

Build all Docker images (this may take 5-10 minutes on first run):

```bash
docker-compose build
```

### Step 3: Start the System

Start all services in detached mode:

```bash
docker-compose up -d
```

### Step 4: Verify Services are Running

Check that all containers are running:

```bash
docker-compose ps
```

You should see 6 containers running:
- `ds2025-traefik`
- `ds2025-postgres`
- `ds2025-authorization-service`
- `ds2025-user-service`
- `ds2025-device-service`
- `ds2025-frontend`

### Step 5: Access the Application

Open your browser and navigate to:

```
http://localhost
```

---

## Default User Accounts

### Administrator Account
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: ADMIN
- **Permissions**: Full CRUD on users and devices, device assignment

### Client Account (Example)
You can create client accounts through the admin interface.

---

## User Guide

### For Administrators

1. **Login** with admin credentials
2. **User Management**:
   - Click "+ Create User" to add new users
   - Fill in: Username, Password, Role (ADMIN/CLIENT), Name, Address, Age
   - Edit or Delete existing users
3. **Device Management**:
   - Click "+ Create Device" to add new devices
   - Fill in: Device Name, Maximum Consumption Value
   - Edit, Delete, or Assign devices to users
4. **Device Assignment**:
   - Click "Assign" button on any device
   - Select a user from the dropdown
   - Click "Assign" to link the device to the user

### For Clients

1. **Login** with client credentials
2. **View Devices**:
   - See only devices assigned to your account
   - View device name and maximum consumption value
   - No edit/delete permissions (read-only access)

---

## API Endpoints

### Authorization Service (Port 8083)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login and get JWT token | No |
| GET | `/api/auth/validate` | Validate JWT token | No |

**Login Request Example:**
```json
POST /api/auth/login
{
  "username": "admin",
  "password": "admin123"
}
```

**Login Response Example:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ADMIN",
  "userId": 1
}
```

### User Management Service (Port 8081)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users` | Get all users | Yes |
| GET | `/api/users/{id}` | Get user by ID | Yes |
| POST | `/api/users` | Create new user | Yes |
| PUT | `/api/users/{id}` | Update user | Yes |
| DELETE | `/api/users/{id}` | Delete user | Yes |

**Create User Request Example:**
```json
POST /api/users
Headers: Authorization: Bearer <token>
{
  "username": "john",
  "password": "pass123",
  "role": "CLIENT",
  "name": "John Doe",
  "address": "123 Main St",
  "age": 25
}
```

### Device Management Service (Port 8082)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/api/devices` | Get all devices | Yes | Any |
| GET | `/api/devices/{id}` | Get device by ID | Yes | Any |
| POST | `/api/devices` | Create new device | Yes | ADMIN |
| PUT | `/api/devices/{id}` | Update device | Yes | ADMIN |
| DELETE | `/api/devices/{id}` | Delete device | Yes | ADMIN |
| PUT | `/api/devices/{deviceId}/assign/{userId}` | Assign device to user | Yes | ADMIN |

**Create Device Request Example:**
```json
POST /api/devices
Headers: 
  Authorization: Bearer <token>
  X-User-Role: ADMIN
{
  "name": "Smart Meter 001",
  "maximumConsumptionValue": 5000
}
```

---

## Database Schema

### Authorization Database (`auth-db`)

**Table: users**
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-generated user ID |
| username | VARCHAR(255) UNIQUE | Login username |
| password | VARCHAR(255) | Encrypted password |
| role | VARCHAR(50) | User role (ADMIN/CLIENT) |

### User Management Database (`example-db`)

**Table: users**
| Column | Type | Description |
|--------|------|-------------|
| id | UUID (PK) | User unique identifier |
| username | VARCHAR(255) | Username |
| password | VARCHAR(255) | Password (synced with auth-db) |
| role | VARCHAR(50) | User role |
| name | VARCHAR(255) | Full name |
| address | VARCHAR(255) | Physical address |
| age | INTEGER | User age |

**Table: devices**
| Column | Type | Description |
|--------|------|-------------|
| id | UUID (PK) | Device unique identifier |
| name | VARCHAR(255) | Device name |
| maximum_consumption_value | INTEGER | Max energy consumption (Wh) |
| user_id | VARCHAR(255) | Assigned user UUID (nullable) |

---

## Docker Configuration

### Services Overview

```yaml
services:
  traefik:          # Reverse Proxy (Port 80, 443, 8080)
  postgres:         # Database Server (Port 5433)
  authorization-service:  # Auth Service (Internal: 8083)
  user-service:     # User Management (Internal: 8081)
  device-service:   # Device Management (Internal: 8082)
  frontend:         # Web UI (Internal: 80)
```

### Network Configuration

All services communicate through the `demo_net` Docker network:
- Services can reach each other using service names (e.g., `http://user-service:8081`)
- External access is only through Traefik on port 80

### Volume Configuration

- `postgres_data`: Persistent storage for PostgreSQL database

---

## Configuration Files

### Environment Variables

**Authorization Service:**
- `DB_IP=postgres`
- `DB_PORT=5432`
- `DB_USER=postgres`
- `DB_PASSWORD=root`
- `DB_DBNAME=auth-db`
- `PORT=8083`

**User Service:**
- `DB_IP=postgres`
- `DB_PORT=5432`
- `DB_USER=postgres`
- `DB_PASSWORD=root`
- `DB_DBNAME=example-db`
- `PORT=8081`
- `AUTH_SERVICE_URL=http://authorization-service:8083`

**Device Service:**
- `DB_IP=postgres`
- `DB_PORT=5432`
- `DB_USER=postgres`
- `DB_PASSWORD=root`
- `DB_DBNAME=example-db`
- `PORT=8082`

---

## Testing the System

### Test User Creation and Authentication

1. **Create a new client user** (as admin):
   ```
   Username: testclient
   Password: test123
   Role: CLIENT
   Name: Test Client
   Address: Test Address
   Age: 30
   ```

2. **Logout and login as testclient**
3. **Verify** that the client sees only their assigned devices

### Test Device Assignment

1. **Login as admin**
2. **Create a device**: "Smart Meter A", Max Consumption: 3000
3. **Assign device** to "testclient"
4. **Logout and login as testclient**
5. **Verify** the device appears in the client's dashboard

---

## Stopping the System

### Stop all services:

```bash
docker-compose down
```

### Stop and remove all data (including database):

```bash
docker-compose down -v
```

---

## Troubleshooting

### Issue: Containers won't start

**Solution:**
```bash
# Check Docker is running
docker ps

# View logs for specific service
docker logs ds2025-user-service
docker logs ds2025-authorization-service
docker logs ds2025-device-service
```

### Issue: Port already in use

**Solution:**
- Check if port 80 is available
- Stop any other web servers (IIS, Apache, etc.)
- Or modify `docker-compose.yml` to use different ports

### Issue: Database connection errors

**Solution:**
```bash
# Restart PostgreSQL container
docker-compose restart postgres

# Check PostgreSQL logs
docker logs ds2025-postgres
```

### Issue: Frontend shows blank page

**Solution:**
```bash
# Rebuild and restart frontend
docker-compose build frontend
docker-compose up -d frontend

# Clear browser cache (Ctrl+Shift+R)
```

### Issue: 403 Forbidden on device operations

**Solution:**
- Ensure you're logged in as ADMIN
- Check browser console for role information
- Logout and login again to refresh token

### Issue: User creation doesn't sync with auth service

**Solution:**
```bash
# Check if authorization service is running
docker ps | grep authorization

# Check logs
docker logs ds2025-authorization-service
docker logs ds2025-user-service
```

---

## Monitoring and Logs

### View logs for all services:

```bash
docker-compose logs -f
```

### View logs for specific service:

```bash
docker logs -f ds2025-user-service
docker logs -f ds2025-device-service
docker logs -f ds2025-authorization-service
```

### Access Traefik Dashboard:

```
http://localhost:8080
```

---

## Development Notes

### Project Structure

```
ds2025_spring_example/
├── AuthorizationService/       # JWT authentication service
│   ├── src/main/java/org/example/
│   │   ├── controller/         # REST controllers
│   │   ├── service/            # Business logic
│   │   ├── model/              # JPA entities
│   │   ├── repository/         # Data access
│   │   └── dto/                # Data transfer objects
│   ├── Dockerfile
│   └── pom.xml
├── demo/                       # User management service
│   ├── src/main/java/com/example/demo/
│   │   ├── controllers/        # REST controllers
│   │   ├── services/           # Business logic
│   │   ├── entities/           # JPA entities
│   │   ├── repositories/       # Data access
│   │   └── dtos/               # Data transfer objects
│   ├── Dockerfile
│   └── pom.xml
├── DeviceMicroservice/         # Device management service
│   ├── src/main/java/org/example/
│   │   ├── controllers/        # REST controllers
│   │   ├── services/           # Business logic
│   │   ├── entities/           # JPA entities
│   │   ├── repositories/       # Data access
│   │   └── dtos/               # Data transfer objects
│   ├── Dockerfile
│   └── pom.xml
├── simple-frontend/            # Web UI
│   ├── index.html              # Main application
│   ├── nginx.conf              # Nginx configuration
│   └── Dockerfile
├── init/                       # Database initialization scripts
│   └── init.sql
├── docker-compose.yml          # Docker orchestration
└── README.md                   # This file
```

### Key Design Decisions

1. **Microservices Communication**: Services communicate via HTTP REST APIs
2. **Authentication Flow**: JWT tokens generated by Authorization Service, validated by API Gateway
3. **Database Strategy**: Shared PostgreSQL server with separate databases for isolation
4. **User Sync**: User service automatically syncs credentials with Authorization Service on creation
5. **Role-Based UI**: Frontend dynamically adjusts based on user role (CLIENT vs ADMIN)

---

## Security Considerations

1. **JWT Tokens**: Stateless authentication with expiration
2. **Password Encryption**: BCrypt hashing for stored passwords
3. **Role-Based Access Control**: Endpoints protected by role checks
4. **CORS Configuration**: Configured to allow frontend access
5. **Network Isolation**: Services communicate through internal Docker network

---

## Assignment Requirements Checklist

-  User Management Microservice with CRUD operations
-  Device Management Microservice with CRUD operations
-  Authentication Microservice with JWT
-  Frontend with Admin and Client roles
-  Device-to-user associations
-  Reverse Proxy (Traefik)
-  Docker deployment with docker-compose
-  PostgreSQL database
-  REST APIs for all services
-  Role-based access control
-  README file with build and execution instructions

---

## Authors

**Student Name**: Daria Fangli  
**Academic Year**: 2024-2025  
**Course**: Distributed Systems  
**Assignment**: #1 - Request-Reply Communication Paradigm

---

## Support

For issues or questions:
1. Check the Troubleshooting section above
2. Review Docker logs for error messages
3. Verify all prerequisites are installed
4. Ensure ports 80, 443, 5433, 8080 are available

---

**Last Updated**: November 2024
