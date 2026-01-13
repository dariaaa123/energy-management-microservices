# Assignment 2: Monitoring Microservice with RabbitMQ

## Overview

This assignment extends the Energy Management System from Assignment 1 by introducing:
- **Monitoring Microservice**: Processes device data and computes hourly energy consumption
- **RabbitMQ Message Broker**: Enables asynchronous communication between services
- **Device Data Simulator**: Python application that simulates smart meter readings
- **Event-Based Synchronization**: Automatic sync of users and devices across microservices

---

## Architecture Components

### 1. Monitoring Microservice (Port 8084)
- **Purpose**: Consumes device measurements, calculates hourly consumption, stores results
- **Database**: `monitoring-db` (PostgreSQL)
- **Queues Consumed**:
  - `device_data_queue`: Receives smart meter readings
  - `synchronization_queue`: Receives user/device sync events
- **REST Endpoints**:
  - `GET /api/monitoring/devices/{deviceId}/hourly` - Get hourly consumption data
  - `GET /api/monitoring/devices/{deviceId}/latest` - Get latest consumption
  - `GET /api/monitoring/health` - Health check

### 2. Device Data Simulator (producer.py)
- **Purpose**: Simulates smart meter readings every 10 minutes
- **Message Format**:
```json
{
  "timestamp": "2024-11-17T10:30:00",
  "deviceId": "device_001",
  "measurementValue": 0.45
}
```
- **Exchange**: `data_exchange`
- **Routing Key**: `device.data.key`

### 3. RabbitMQ Message Broker (CloudAMQP)
- **Service**: CloudAMQP hosted RabbitMQ
- **Exchanges**:
  - `data_exchange`: Routes device measurements
  - `sync_exchange`: Routes synchronization events
- **Queues**:
  - `device_data_queue`: Stores device measurements
  - `synchronization_queue`: Stores sync events

### 4. Event-Based Synchronization

All microservices publish synchronization events when data changes:

**User Sync Message** (from User Service):
```json
{
  "type": "USER",
  "action": "CREATE|UPDATE|DELETE",
  "userId": "uuid",
  "username": "john",
  "name": "John Doe"
}
```

**Device Sync Message** (from Device Service):
```json
{
  "type": "DEVICE",
  "action": "CREATE|UPDATE|DELETE",
  "deviceId": "uuid",
  "deviceName": "Smart Meter 001",
  "maximumConsumptionValue": 5000.0,
  "assignedUserId": "uuid"
}
```

---

## Database Schema

### Monitoring Database (`monitoring-db`)

**Table: device_measurements**
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-generated ID |
| device_id | VARCHAR | Device identifier |
| timestamp | TIMESTAMP | Measurement timestamp |
| measurement_value | DOUBLE | Energy consumption value (kWh) |
| received_at | TIMESTAMP | When message was received |

**Table: hourly_energy_consumption**
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT (PK) | Auto-generated ID |
| device_id | VARCHAR | Device identifier |
| hour_timestamp | TIMESTAMP | Start of the hour |
| total_consumption | DOUBLE | Sum of measurements in that hour |
| measurement_count | INTEGER | Number of measurements |
| calculated_at | TIMESTAMP | When calculation was performed |

**Table: devices** (synced from Device Service)
| Column | Type | Description |
|--------|------|-------------|
| device_id | VARCHAR (PK) | Device identifier |
| name | VARCHAR | Device name |
| maximum_consumption_value | DOUBLE | Max consumption threshold |
| user_id | VARCHAR | Assigned user ID |

**Table: users** (synced from User Service)
| Column | Type | Description |
|--------|------|-------------|
| user_id | VARCHAR (PK) | User identifier |
| username | VARCHAR | Username |
| name | VARCHAR | Full name |

---

## Setup Instructions

### Prerequisites
- Docker Desktop installed and running
- Python 3.8+ (for running the simulator locally)
- All ports available: 80, 443, 5433, 8080, 8081, 8082, 8083, 8084

### Step 1: Build and Start All Services

```bash
# Build all Docker images
docker-compose build

# Start all services
docker-compose up -d

# Verify all containers are running
docker-compose ps
```

You should see 7 containers:
- `ds2025-traefik`
- `ds2025-postgres`
- `ds2025-authorization-service`
- `ds2025-user-service`
- `ds2025-device-service`
- `ds2025-monitoring-service`
- `ds2025-frontend`

### Step 2: Verify Services

```bash
# Check Monitoring Service health
curl http://localhost/api/monitoring/health

# Check logs
docker logs ds2025-monitoring-service
```

### Step 3: Run Device Data Simulator

The simulator is a Python script that sends device measurements to RabbitMQ.

**Option A: Run locally**
```bash
# Install dependencies
pip install pika

# Run the simulator
python producer.py
```

**Option B: Modify device ID and interval**
Edit `producer.py`:
```python
DEVICE_ID = "device_001"  # Change to match your device
SEND_INTERVAL_SECONDS = 600  # 10 minutes (600 seconds)
```

---

## Testing the System

### Test 1: Create a Device and User

1. **Login as admin** at `http://localhost`
   - Username: `admin`
   - Password: `admin123`

2. **Create a new device**:
   - Name: "Smart Meter Test"
   - Max Consumption: 5000

3. **Create a new user**:
   - Username: "testuser"
   - Password: "test123"
   - Role: CLIENT
   - Name: "Test User"

4. **Assign device to user**

5. **Verify synchronization**:
```bash
# Check Monitoring Service logs
docker logs ds2025-monitoring-service | grep "Synced"

# You should see:
# Synced user: User(userId=..., username=testuser, name=Test User)
# Synced device: Device(deviceId=..., name=Smart Meter Test, ...)
```

### Test 2: Send Device Measurements

1. **Update producer.py** with your device ID:
```python
DEVICE_ID = "your-device-uuid-here"
```

2. **Run the simulator**:
```bash
python producer.py
```

3. **Monitor the logs**:
```bash
docker logs -f ds2025-monitoring-service
```

You should see:
```
Received device data message: DeviceDataMessage(timestamp=..., deviceId=..., measurementValue=...)
Saved measurement: DeviceMeasurement(...)
Calculating hourly consumption for device ...
Updated hourly consumption: HourlyEnergyConsumption(...)
```

### Test 3: Query Hourly Consumption

```bash
# Get all hourly consumption for a device
curl "http://localhost/api/monitoring/devices/{deviceId}/hourly"

# Get latest consumption
curl "http://localhost/api/monitoring/devices/{deviceId}/latest"

# Get consumption for a date range
curl "http://localhost/api/monitoring/devices/{deviceId}/hourly?start=2024-11-17T00:00:00&end=2024-11-17T23:59:59"
```

**Example Response**:
```json
[
  {
    "id": 1,
    "deviceId": "device_001",
    "hourTimestamp": "2024-11-17T10:00:00",
    "totalConsumption": 2.45,
    "measurementCount": 6,
    "calculatedAt": "2024-11-17T10:50:00"
  }
]
```

---

## Message Flow Diagrams

### Device Data Flow
```
Producer (producer.py)
    ↓ (publish)
RabbitMQ (data_exchange → device_data_queue)
    ↓ (consume)
Monitoring Service
    ↓ (save)
PostgreSQL (monitoring-db)
    - device_measurements table
    - hourly_energy_consumption table
```

### Synchronization Flow
```
User Service (CREATE/UPDATE/DELETE user)
    ↓ (publish)
RabbitMQ (sync_exchange → synchronization_queue)
    ↓ (consume)
Monitoring Service
    ↓ (save)
PostgreSQL (monitoring-db.users)

Device Service (CREATE/UPDATE/DELETE device)
    ↓ (publish)
RabbitMQ (sync_exchange → synchronization_queue)
    ↓ (consume)
Monitoring Service
    ↓ (save)
PostgreSQL (monitoring-db.devices)
```

---

## Configuration Details

### RabbitMQ Configuration

All services use CloudAMQP:
```
URL: amqps://jjzdoqha:Albina_16@cow.rmq2.cloudamqp.com/jjzdoqha
```

**Exchanges**:
- `data_exchange` (Topic)
- `sync_exchange` (Topic)

**Queues**:
- `device_data_queue` (Durable)
- `synchronization_queue` (Durable)

**Routing Keys**:
- `device.data.key` - Device measurements
- `sync.key` - Synchronization events

### Environment Variables

**Monitoring Service**:
```yaml
DB_IP: postgres
DB_PORT: 5432
DB_USER: postgres
DB_PASSWORD: root
DB_DBNAME: monitoring-db
PORT: 8084
RABBITMQ_URL: amqps://jjzdoqha:Albina_16@cow.rmq2.cloudamqp.com/jjzdoqha
```

**User Service** (updated):
```yaml
RABBITMQ_URL: amqps://jjzdoqha:Albina_16@cow.rmq2.cloudamqp.com/jjzdoqha
```

**Device Service** (updated):
```yaml
RABBITMQ_URL: amqps://jjzdoqha:Albina_16@cow.rmq2.cloudamqp.com/jjzdoqha
```

---

## Troubleshooting

### Issue: Monitoring Service not receiving messages

**Solution**:
```bash
# Check RabbitMQ connection
docker logs ds2025-monitoring-service | grep "RabbitMQ"

# Verify queues exist (check CloudAMQP dashboard)
# URL: https://customer.cloudamqp.com/

# Restart the service
docker-compose restart monitoring-service
```

### Issue: Producer cannot connect to RabbitMQ

**Solution**:
```bash
# Test connection
python -c "import pika; pika.URLParameters('amqps://jjzdoqha:Albina_16@cow.rmq2.cloudamqp.com/jjzdoqha')"

# Check firewall settings
# Ensure port 5671 (AMQPS) is not blocked
```

### Issue: Hourly consumption not calculating

**Solution**:
```bash
# Check if measurements are being saved
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db -c "SELECT * FROM device_measurements LIMIT 10;"

# Check for errors in logs
docker logs ds2025-monitoring-service | grep "ERROR"

# Verify device ID matches
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db -c "SELECT DISTINCT device_id FROM device_measurements;"
```

### Issue: Sync messages not working

**Solution**:
```bash
# Check User Service logs
docker logs ds2025-user-service | grep "sync"

# Check Device Service logs
docker logs ds2025-device-service | grep "sync"

# Verify RabbitMQ configuration
docker logs ds2025-monitoring-service | grep "Received sync message"
```

---

## API Documentation

### Monitoring Service Endpoints

#### Get Hourly Consumption
```http
GET /api/monitoring/devices/{deviceId}/hourly
```

**Query Parameters**:
- `start` (optional): Start datetime (ISO 8601 format)
- `end` (optional): End datetime (ISO 8601 format)

**Response**:
```json
[
  {
    "id": 1,
    "deviceId": "uuid",
    "hourTimestamp": "2024-11-17T10:00:00",
    "totalConsumption": 2.45,
    "measurementCount": 6,
    "calculatedAt": "2024-11-17T10:50:00"
  }
]
```

#### Get Latest Consumption
```http
GET /api/monitoring/devices/{deviceId}/latest
```

**Response**:
```json
{
  "id": 1,
  "deviceId": "uuid",
  "hourTimestamp": "2024-11-17T10:00:00",
  "totalConsumption": 2.45,
  "measurementCount": 6,
  "calculatedAt": "2024-11-17T10:50:00"
}
```

#### Health Check
```http
GET /api/monitoring/health
```

**Response**:
```
Monitoring Service is running
```

---

## Project Structure

```
.
├── MonitoringMicroservice/
│   ├── src/main/java/com/energy/monitoring/
│   │   ├── config/
│   │   │   └── RabbitMQConfig.java
│   │   ├── consumer/
│   │   │   ├── DeviceDataConsumer.java
│   │   │   └── SyncConsumer.java
│   │   ├── controller/
│   │   │   └── MonitoringController.java
│   │   ├── dto/
│   │   │   ├── DeviceDataMessage.java
│   │   │   ├── HourlyConsumptionDTO.java
│   │   │   └── SyncMessage.java
│   │   ├── entity/
│   │   │   ├── Device.java
│   │   │   ├── DeviceMeasurement.java
│   │   │   ├── HourlyEnergyConsumption.java
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   ├── DeviceMeasurementRepository.java
│   │   │   ├── DeviceRepository.java
│   │   │   ├── HourlyEnergyConsumptionRepository.java
│   │   │   └── UserRepository.java
│   │   ├── service/
│   │   │   ├── MonitoringService.java
│   │   │   └── SyncService.java
│   │   └── MonitoringApplication.java
│   ├── Dockerfile
│   └── pom.xml
├── demo/ (User Service - updated)
│   └── src/main/java/com/example/demo/
│       ├── config/
│       │   └── RabbitMQConfig.java (NEW)
│       └── services/
│           └── SyncPublisher.java (NEW)
├── DeviceMicroservice/ (Device Service - updated)
│   └── src/main/java/org/example/
│       ├── config/
│       │   └── RabbitMQConfig.java (NEW)
│       └── services/
│           └── SyncPublisher.java (NEW)
├── producer.py (Device Data Simulator)
├── docker-compose.yml (updated)
└── ASSIGNMENT2_README.md (this file)
```

---

## Key Features Implemented

✅ Monitoring Microservice with RabbitMQ consumer  
✅ Device data processing and storage  
✅ Hourly energy consumption calculation  
✅ Event-based synchronization (users and devices)  
✅ Device data simulator (Python)  
✅ REST API for querying consumption data  
✅ Separate database for monitoring service  
✅ Asynchronous message processing  
✅ Durable queues and persistent messages  
✅ Error handling and logging  

---

## Performance Considerations

1. **Message Processing**: Asynchronous processing prevents blocking
2. **Database Indexing**: Indexes on `device_id` and `hour_timestamp` for fast queries
3. **Hourly Aggregation**: Incremental updates as new measurements arrive
4. **Connection Pooling**: RabbitMQ connection reuse across messages
5. **Transaction Management**: Atomic operations for data consistency

---

## Future Enhancements

- WebSocket notifications for real-time consumption updates
- Alert system when consumption exceeds device maximum
- Data visualization dashboard
- Historical data analytics
- Message retry mechanism with dead letter queue
- Rate limiting for device data ingestion
- Data archival for old measurements

---

## Authors

**Student Name**: Daria Fangli  
**Academic Year**: 2024-2025  
**Course**: Distributed Systems  
**Assignment**: #2 - Message-Oriented Middleware

---

**Last Updated**: November 2024
