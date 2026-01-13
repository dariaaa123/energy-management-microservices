# Energy Management System - Deployment Diagram

## System Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           DOCKER HOST ENVIRONMENT                                │
│                                                                                   │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │                     Docker Network: ds2025_demo_net                         │ │
│  │                                                                              │ │
│  │                        ┌──────────────────────┐                             │ │
│  │                        │  Traefik Container   │                             │ │
│  │                        │  ds2025-traefik      │                             │ │
│  │                        │                      │                             │ │
│  │                        │  Reverse Proxy       │                             │ │
│  │                        │  Port: 80, 443, 8080 │                             │ │
│  │                        │  (API Gateway)       │                             │ │
│  │                        └──────────┬───────────┘                             │ │
│  │                                   │                                          │ │
│  │          ┌────────────────────────┼────────────────────────┐                │ │
│  │          │                        │                        │                │ │
│  │  ┌───────▼────────┐      ┌───────▼────────┐      ┌───────▼────────┐       │ │
│  │  │  Spring Boot   │      │  Spring Boot   │      │  Spring Boot   │       │ │
│  │  │  Container     │      │  Container     │      │  Container     │       │ │
│  │  │                │      │                │      │                │       │ │
│  │  │ Authorization  │      │ User Service   │      │ Device Service │       │ │
│  │  │   Service      │      │                │      │                │       │ │
│  │  │  Port: 8083    │      │  Port: 8081    │      │  Port: 8082    │       │ │
│  │  │  Java 17       │      │  Java 17       │      │  Java 17       │       │ │
│  │  │                │      │                │      │                │       │ │
│  │  │ Route:         │      │ Route:         │      │ Route:         │       │ │
│  │  │ /api/auth      │      │ /api/users     │      │ /api/devices   │       │ │
│  │  └───────┬────────┘      └───────┬────────┘      └───────┬────────┘       │ │
│  │          │                       │                       │                 │ │
│  │          │                       │                       │                 │ │
│  │          │                       │                       │                 │ │
│  │          │         ┌─────────────▼───────────────────────▼─────┐           │ │
│  │          │         │      PostgreSQL Container                 │           │ │
│  │          │         │      ds2025-postgres                      │           │ │
│  │          │         │      PostgreSQL 17                        │           │ │
│  │          │         │      Port: 5433 (external)                │           │ │
│  │          │         │                                            │           │ │
│  │          │         │  ┌──────────────────────────────────────┐ │           │ │
│  │          │         │  │  Database: auth-db                   │ │           │ │
│  │          └─────────┼──►  Table: users                        │ │           │ │
│  │                    │  │  (id, username, password, role)      │ │           │ │
│  │                    │  └──────────────────────────────────────┘ │           │ │
│  │                    │                                            │           │ │
│  │                    │  ┌──────────────────────────────────────┐ │           │ │
│  │                    └──►  Database: example-db                │ │           │ │
│  │                       │  Table: users                        │ │           │ │
│  │                       │  (id, username, password, role,      │ │           │ │
│  │                       │   name, address, age)                │ │           │ │
│  │                       └──────────────────────────────────────┘ │           │ │
│  │                       │                                        │           │ │
│  │                       │  ┌──────────────────────────────────┐ │           │ │
│  │                       │  │  Database: device-db             │ │           │ │
│  │                       │  │  Table: devices                  │ │           │ │
│  │                       │  │  (id, name,                      │ │           │ │
│  │                       │  │   maximum_consumption_value,     │ │           │ │
│  │                       │  │   user_id)                       │ │           │ │
│  │                       │  └──────────────────────────────────┘ │           │ │
│  │                       └────────────────────────────────────────┘           │ │
│  │                                                                             │ │
│  │                        ┌──────────────────────┐                            │ │
│  │                        │  Nginx Container     │                            │ │
│  │                        │  ds2025-frontend     │                            │ │
│  │                        │                      │                            │ │
│  │                        │  simple-frontend     │                            │ │
│  │                        │  Port: 80            │                            │ │
│  │                        │  (HTML/CSS/JS)       │                            │ │
│  │                        │                      │                            │ │
│  │                        │  Route: /            │                            │ │
│  │                        └──────────▲───────────┘                            │ │
│  │                                   │                                         │ │
│  │                                   │                                         │ │
│  └───────────────────────────────────┼─────────────────────────────────────────┘ │
│                                      │                                           │
│  ┌──────────────────────────────────────────────────────────────────────┐      │
│  │                         Docker Volumes                                │      │
│  │                                                                        │      │
│  │  • ds2025_postgres_data → /var/lib/postgresql/data (ds2025-postgres) │      │
│  │                                                                        │      │
│  └──────────────────────────────────────────────────────────────────────┘      │
│                                                                                  │
└──────────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      │
                          ┌───────────▼────────────┐
                          │   External Users       │
                          │                        │
                          │  • Admin Users         │
                          │  • Client Users        │
                          │                        │
                          │  Access via:           │
                          │  http://localhost:80   │
                          │                        │
                          │  Traefik Dashboard:    │
                          │  http://localhost:8080 │
                          └────────────────────────┘
```

## Component Details

### 0. Reverse Proxy Layer

#### Traefik
- **Container Name**: `ds2025-traefik`
- **Image**: `traefik:v2.10`
- **Ports**: 
  - `80:80` (HTTP)
  - `443:443` (HTTPS)
  - `8080:8080` (Dashboard)
- **Responsibilities**:
  - API Gateway and reverse proxy
  - Routes requests to appropriate services
  - Service discovery via Docker labels
  - Load balancing
- **Routes**:
  - `/` → frontend
  - `/api/auth` → authorization-service
  - `/api/users` → user-service
  - `/api/devices` → device-service

### 1. Database Layer

#### PostgreSQL - Single Instance with Multiple Databases
- **Container Name**: `ds2025-postgres`
- **Image**: `postgres:17`
- **Port**: `5433:5432` (external:internal)
- **Volume**: `ds2025_postgres_data`
- **Environment**:
  - POSTGRES_USER: postgres
  - POSTGRES_PASSWORD: root
  - POSTGRES_DB: postgres

##### Database: auth-db
- **Purpose**: Authentication and authorization
- **Tables**:
  - `users` (id, username, password, role)
- **Used by**: Authorization Service

##### Database: example-db
- **Purpose**: User management
- **Tables**:
  - `users` (id, username, password, role, name, address, age)
- **Used by**: User Service

##### Database: device-db
- **Purpose**: Device and energy management
- **Tables**:
  - `devices` (id, name, maximum_consumption_value, user_id)
- **Used by**: Device Service

### 2. Backend Services Layer

#### Authorization Service
- **Container Name**: `ds2025-authorization-service`
- **Build Context**: `./AuthorizationService`
- **Port**: `8083:8083`
- **Technology**: Spring Boot 3.x, Java 17
- **Database**: auth-db
- **Traefik Route**: `/api/auth`
- **Dependencies**: ds2025-postgres, traefik
- **Responsibilities**:
  - JWT token generation (HS256, 1 hour expiry)
  - User authentication (BCrypt password hashing)
  - Token validation
  - User registration
  - Role-based access control (ADMIN/CLIENT)

#### User Microservice
- **Container Name**: `ds2025-user-service`
- **Build Context**: `./demo`
- **Port**: `8081:8081`
- **Technology**: Spring Boot 3.x, Java 17
- **Database**: example-db
- **Traefik Route**: `/api/users`
- **Dependencies**: ds2025-postgres, traefik
- **Responsibilities**:
  - User CRUD operations
  - User profile management
  - Syncs credentials with Authorization Service
  - Role management (Admin/Client)

#### Device Microservice
- **Container Name**: `ds2025-device-service`
- **Build Context**: `./DeviceMicroService`
- **Port**: `8082:8082`
- **Technology**: Spring Boot 3.x, Java 17
- **Database**: device-db
- **Traefik Route**: `/api/devices`
- **Dependencies**: ds2025-postgres, traefik
- **Responsibilities**:
  - Device CRUD operations
  - Device-User associations
  - Energy consumption tracking
  - Maximum consumption monitoring

### 3. Frontend Layer

#### Web Dashboard
- **Container Name**: `ds2025-frontend`
- **Build Context**: `./simple-frontend`
- **Port**: `80:80`
- **Technology**: HTML, CSS, JavaScript, Nginx
- **Traefik Route**: `/` (Priority: 1)
- **Dependencies**: traefik
- **Purpose**: 
  - Admin dashboard for user and device management
  - Client interface for viewing devices and consumption
  - Login and authentication UI
  - Responsive web interface

## Network Configuration

### Docker Network: ds2025_demo_net
- **Name**: `ds2025_demo_net`
- **Type**: Bridge network
- **Purpose**: Internal communication between containers
- **DNS Resolution**: Containers can communicate using service names

### Port Mappings (Host:Container)
```
80:80       → traefik (HTTP entry point)
443:443     → traefik (HTTPS entry point)
8080:8080   → traefik (Dashboard)
8081:8081   → user-service (Internal, accessed via Traefik)
8082:8082   → device-service (Internal, accessed via Traefik)
8083:8083   → authorization-service (Internal, accessed via Traefik)
5433:5432   → postgres (Database)
```

### Traefik Routing Configuration
All external requests go through Traefik on port 80:
- `http://localhost/` → Frontend
- `http://localhost/api/auth/*` → Authorization Service
- `http://localhost/api/users/*` → User Service
- `http://localhost/api/devices/*` → Device Service
- `http://localhost:8080` → Traefik Dashboard

## Data Flow

### 1. User Authentication Flow
```
Client → Traefik (port 80) → Frontend
           ↓
Frontend → Traefik → Authorization Service (POST /api/auth/login)
                         ↓
                    Query auth-db
                         ↓
                    Generate JWT Token (HS256)
                         ↓
                    Return token + user info
                         ↓
                    Client (stored in localStorage)
```

### 2. User Registration Flow
```
Admin → Traefik → Frontend → Traefik → User Service (POST /api/users)
                                            ↓
                                       Save to example-db
                                            ↓
                                       Sync credentials
                                            ↓
                                       Authorization Service (POST /api/auth/register)
                                            ↓
                                       Save to auth-db
```

### 3. Device Management Flow
```
User → Traefik → Frontend → Traefik → Device Service (GET/POST/PUT/DELETE /api/devices)
                                            ↓
                                       Validate JWT token
                                            ↓
                                       Query device-db
                                            ↓
                                       Return device data
```

### 4. Cross-Service Communication
```
User Service ──────────────► Authorization Service
(Sync user credentials)      (Register in auth-db)

Device Service ─────────────► User Service
(Verify ownership)            (Get user info from example-db)
```

## Deployment Commands

### Start All Services
```bash
docker-compose up -d
```

### Stop All Services
```bash
docker-compose down
```

### View Logs
```bash
docker-compose logs -f [service-name]
```

### Rebuild Services
```bash
docker-compose up -d --build
```

## Health Checks

### Service Endpoints
- Traefik Dashboard: `http://localhost:8080`
- Frontend: `http://localhost:80`
- Authorization Service: `http://localhost/api/auth/validate?token=<token>`
- User Service: `http://localhost/api/users`
- Device Service: `http://localhost/api/devices`

### Database Connections
```bash
# Connect to PostgreSQL container
docker exec -it ds2025-postgres psql -U postgres

# Connect to specific databases
docker exec -it ds2025-postgres psql -U postgres -d auth-db
docker exec -it ds2025-postgres psql -U postgres -d example-db
docker exec -it ds2025-postgres psql -U postgres -d device-db

# List all databases
docker exec -it ds2025-postgres psql -U postgres -c "\l"
```

## Security Considerations

1. **Reverse Proxy**: Traefik acts as single entry point, hiding internal services
2. **Network Isolation**: All services communicate within Docker network (ds2025_demo_net)
3. **JWT Authentication**: 
   - Stateless authentication using JWT tokens
   - HS256 algorithm with Base64-encoded secret key
   - 1-hour token expiration
   - BCrypt password hashing
4. **Database Credentials**: Stored in environment variables (should use Docker secrets in production)
5. **CORS Configuration**: Configured to allow frontend-backend communication
6. **Role-Based Access**: Admin and Client roles with different permissions
7. **Database Separation**: Three separate databases for different concerns (auth, users, devices)
8. **Service Discovery**: Automatic via Docker labels, no hardcoded IPs

## Scalability Notes

- Each microservice can be scaled independently using Docker Compose scale
- Traefik automatically load balances between multiple instances
- Single PostgreSQL instance with multiple databases (can be split for better scaling)
- Persistent volumes ensure data retention across container restarts
- Stateless JWT authentication enables horizontal scaling
- Consider using Docker Swarm or Kubernetes for production deployment
- Database connection pooling configured in Spring Boot applications

## Monitoring & Logging

- **Traefik Dashboard**: `http://localhost:8080` - View routes, services, and health
- **Container Logs**: `docker-compose logs -f [service-name]`
- **Database Logs**: `docker logs ds2025-postgres`
- **Application Logs**: Spring Boot logging to console (stdout/stderr)
- **Nginx Logs**: Access and error logs in frontend container

### View Logs by Service
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f authorization-service
docker-compose logs -f user-service
docker-compose logs -f device-service
docker-compose logs -f frontend
docker-compose logs -f postgres
docker-compose logs -f traefik
```

---

**Note**: This deployment is configured for development/testing. For production:
- Use Docker secrets for sensitive data (passwords, JWT keys)
- Implement SSL/TLS certificates via Traefik Let's Encrypt
- Add monitoring tools (Prometheus, Grafana)
- Configure automated backup strategies for PostgreSQL
- Implement CI/CD pipeline (GitLab CI, GitHub Actions)
- Use container orchestration (Kubernetes, Docker Swarm)
- Set up centralized logging (ELK Stack, Loki)
- Configure resource limits and health checks
- Implement rate limiting and DDoS protection

## Monitoring & Logging

- **Container Logs**: `docker-compose logs`
- **Database Logs**: Available in container logs
- **Application Logs**: Spring Boot logging to console
- **Nginx Logs**: Access and error logs in container

---

**Note**: This deployment is configured for development/testing. For production:
- Use proper secrets management
- Implement SSL/TLS certificates
- Add monitoring tools (Prometheus, Grafana)
- Configure backup strategies for databases
- Implement CI/CD pipeline
- Use container orchestration (Kubernetes)
