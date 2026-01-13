# System Architecture - Assignment 2

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Frontend (Nginx)                             │
│                      http://localhost                                │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    Traefik Reverse Proxy                             │
│                    (API Gateway - Port 80)                           │
└─┬──────────┬──────────┬──────────┬──────────────────────────────────┘
  │          │          │          │
  │          │          │          │
  ▼          ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐ ┌──────────────┐
│  Auth  │ │  User  │ │ Device │ │  Monitoring  │
│Service │ │Service │ │Service │ │   Service    │
│ :8083  │ │ :8081  │ │ :8082  │ │    :8084     │
└───┬────┘ └───┬────┘ └───┬────┘ └──────┬───────┘
    │          │          │              │
    │          │          │              │
    ▼          ▼          ▼              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    PostgreSQL Database Server                        │
│                         (Port 5433)                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐       │
│  │ auth-db  │  │example-db│  │device-db │  │monitoring-db │       │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────────────┘

                             ▲
                             │
                             │ RabbitMQ Messages
                             │
┌─────────────────────────────────────────────────────────────────────┐
│                    RabbitMQ (CloudAMQP)                              │
│                                                                       │
│  ┌─────────────────────┐         ┌──────────────────────┐          │
│  │   data_exchange     │         │    sync_exchange     │          │
│  │      (Topic)        │         │       (Topic)        │          │
│  └──────────┬──────────┘         └──────────┬───────────┘          │
│             │                               │                       │
│             ▼                               ▼                       │
│  ┌──────────────────────┐       ┌──────────────────────┐          │
│  │ device_data_queue    │       │synchronization_queue │          │
│  │     (Durable)        │       │      (Durable)       │          │
│  └──────────┬───────────┘       └──────────┬───────────┘          │
└─────────────┼──────────────────────────────┼──────────────────────┘
              │                              │
              │ Consume                      │ Consume
              ▼                              ▼
    ┌──────────────────┐          ┌──────────────────┐
    │DeviceDataConsumer│          │   SyncConsumer   │
    │  (Monitoring)    │          │   (Monitoring)   │
    └──────────────────┘          └──────────────────┘
              ▲
              │ Publish
              │
    ┌──────────────────┐
    │   producer.py    │
    │ (Device Simulator)│
    └──────────────────┘
```

---

## Message Flow Details

### 1. Device Data Flow

```
┌──────────────┐
│ producer.py  │ Simulates smart meter readings
└──────┬───────┘
       │ Publish every 10 minutes
       │ {timestamp, deviceId, measurementValue}
       ▼
┌─────────────────────────┐
│ RabbitMQ CloudAMQP      │
│ Exchange: data_exchange │
│ Queue: device_data_queue│
└──────┬──────────────────┘
       │ Consume
       ▼
┌──────────────────────────┐
│ Monitoring Service       │
│ DeviceDataConsumer       │
└──────┬───────────────────┘
       │ Process & Calculate
       ▼
┌──────────────────────────┐
│ PostgreSQL               │
│ monitoring-db            │
│ - device_measurements    │ ← Raw measurements
│ - hourly_energy_consumption│ ← Aggregated data
└──────────────────────────┘
```

### 2. User Synchronization Flow

```
┌──────────────┐
│ User Service │ Admin creates/updates/deletes user
└──────┬───────┘
       │ Publish sync event
       │ {type: "USER", action: "CREATE", userId, username, name}
       ▼
┌─────────────────────────────┐
│ RabbitMQ CloudAMQP          │
│ Exchange: sync_exchange     │
│ Queue: synchronization_queue│
└──────┬──────────────────────┘
       │ Consume
       ▼
┌──────────────────────────┐
│ Monitoring Service       │
│ SyncConsumer             │
└──────┬───────────────────┘
       │ Save/Update/Delete
       ▼
┌──────────────────────────┐
│ PostgreSQL               │
│ monitoring-db.users      │ ← Synced user data
└──────────────────────────┘
```

### 3. Device Synchronization Flow

```
┌──────────────┐
│Device Service│ Admin creates/updates/deletes device
└──────┬───────┘
       │ Publish sync event
       │ {type: "DEVICE", action: "CREATE", deviceId, deviceName, ...}
       ▼
┌─────────────────────────────┐
│ RabbitMQ CloudAMQP          │
│ Exchange: sync_exchange     │
│ Queue: synchronization_queue│
└──────┬──────────────────────┘
       │ Consume
       ▼
┌──────────────────────────┐
│ Monitoring Service       │
│ SyncConsumer             │
└──────┬───────────────────┘
       │ Save/Update/Delete
       ▼
┌──────────────────────────┐
│ PostgreSQL               │
│ monitoring-db.devices    │ ← Synced device data
└──────────────────────────┘
```

---

## Component Interactions

### Monitoring Service Internal Flow

```
┌─────────────────────────────────────────────────────────────┐
│              Monitoring Microservice                         │
│                                                              │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │DeviceDataConsumer│         │   SyncConsumer   │         │
│  └────────┬─────────┘         └────────┬─────────┘         │
│           │                            │                    │
│           ▼                            ▼                    │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │MonitoringService │         │   SyncService    │         │
│  │                  │         │                  │         │
│  │- processDeviceData│        │- processSyncMsg  │         │
│  │- calculateHourly │         │- handleUserSync  │         │
│  └────────┬─────────┘         │- handleDeviceSync│         │
│           │                   └────────┬─────────┘         │
│           │                            │                    │
│           ▼                            ▼                    │
│  ┌─────────────────────────────────────────────┐          │
│  │            Repositories                      │          │
│  │  - DeviceMeasurementRepository              │          │
│  │  - HourlyEnergyConsumptionRepository        │          │
│  │  - UserRepository                           │          │
│  │  - DeviceRepository                         │          │
│  └────────┬────────────────────────────────────┘          │
│           │                                                 │
└───────────┼─────────────────────────────────────────────────┘
            │
            ▼
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL                                │
│                   monitoring-db                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Data Models

### Device Measurement
```
┌─────────────────────────┐
│  device_measurements    │
├─────────────────────────┤
│ id (PK)                 │
│ device_id               │
│ timestamp               │
│ measurement_value       │
│ received_at             │
└─────────────────────────┘
```

### Hourly Energy Consumption
```
┌──────────────────────────────┐
│ hourly_energy_consumption    │
├──────────────────────────────┤
│ id (PK)                      │
│ device_id                    │
│ hour_timestamp               │
│ total_consumption            │
│ measurement_count            │
│ calculated_at                │
└──────────────────────────────┘
```

### Synced Device
```
┌─────────────────────────┐
│       devices           │
├─────────────────────────┤
│ device_id (PK)          │
│ name                    │
│ maximum_consumption_value│
│ user_id                 │
└─────────────────────────┘
```

### Synced User
```
┌─────────────────────────┐
│        users            │
├─────────────────────────┤
│ user_id (PK)            │
│ username                │
│ name                    │
└─────────────────────────┘
```

---

## Technology Stack

### Backend Services
- **Java 21** with **Spring Boot 3.2.0** (Monitoring)
- **Java 21** with **Spring Boot 4.0.0-SNAPSHOT** (User, Device, Auth)
- **Spring AMQP** for RabbitMQ integration
- **Spring Data JPA** for database access
- **PostgreSQL 17** for persistence

### Message Broker
- **RabbitMQ** (CloudAMQP hosted)
- **AMQP Protocol** over TLS (amqps://)
- **Topic Exchanges** for routing
- **Durable Queues** for reliability

### Infrastructure
- **Docker** & **Docker Compose** for containerization
- **Traefik v2.10** as API Gateway
- **Nginx Alpine** for frontend

### Simulator
- **Python 3.8+**
- **Pika** library for RabbitMQ

---

## Network Configuration

```
Docker Network: ds2025_demo_net (Bridge)

Services:
├── traefik           → Ports: 80, 443, 8080
├── postgres          → Port: 5433 (external), 5432 (internal)
├── authorization     → Internal: 8083
├── user-service      → Internal: 8081
├── device-service    → Internal: 8082
├── monitoring-service→ Internal: 8084
└── frontend          → Internal: 80

External Access:
- All HTTP traffic → Port 80 → Traefik → Services
- Database → Port 5433 → PostgreSQL
- Traefik Dashboard → Port 8080
```

---

## Security Considerations

1. **RabbitMQ**: TLS encrypted connection (amqps://)
2. **Database**: Internal Docker network only
3. **API Gateway**: Single entry point (Traefik)
4. **Authentication**: JWT tokens (existing from Assignment 1)
5. **Message Durability**: Persistent queues and messages

---

## Scalability Considerations

1. **Horizontal Scaling**: Multiple monitoring service instances can consume from same queue
2. **Load Balancing**: Traefik distributes requests
3. **Database Connection Pooling**: Efficient resource usage
4. **Asynchronous Processing**: Non-blocking message consumption
5. **Queue Persistence**: Messages survive service restarts

---

## Monitoring & Observability

### Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker logs -f ds2025-monitoring-service

# Filter by keyword
docker logs ds2025-monitoring-service | grep "ERROR"
```

### Metrics
- RabbitMQ Dashboard: CloudAMQP web interface
- Traefik Dashboard: http://localhost:8080
- Database queries: Direct PostgreSQL access

### Health Checks
- Monitoring Service: `GET /api/monitoring/health`
- PostgreSQL: `pg_isready` command
- RabbitMQ: CloudAMQP dashboard

---

## Deployment Workflow

```
1. Code Changes
   ↓
2. Build Docker Images
   docker-compose build
   ↓
3. Start Services
   docker-compose up -d
   ↓
4. Verify Health
   curl http://localhost/api/monitoring/health
   ↓
5. Run Simulator
   python producer.py
   ↓
6. Monitor Logs
   docker-compose logs -f
```

---

## Future Architecture Enhancements

1. **WebSocket Layer**: Real-time consumption updates to frontend
2. **Redis Cache**: Cache frequently accessed consumption data
3. **Elasticsearch**: Full-text search and analytics
4. **Grafana**: Visualization dashboards
5. **Prometheus**: Metrics collection
6. **Kubernetes**: Container orchestration for production
7. **API Rate Limiting**: Prevent abuse
8. **Circuit Breaker**: Resilience patterns
9. **Dead Letter Queue**: Handle failed messages
10. **Multi-region**: Geographic distribution

---

This architecture provides a solid foundation for a scalable, maintainable, and observable energy management system with asynchronous communication capabilities.
