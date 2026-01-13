# Monitoring Microservice

## Overview
The Monitoring Microservice is responsible for processing smart meter readings, calculating hourly energy consumption, and maintaining synchronized copies of user and device data from other microservices.

## Features
- ✅ Consumes device measurements from RabbitMQ
- ✅ Calculates hourly energy consumption automatically
- ✅ Synchronizes user and device data via event-driven architecture
- ✅ Provides REST API for querying consumption data
- ✅ Stores raw measurements and aggregated hourly data
- ✅ Handles message processing asynchronously

## Technology Stack
- **Java 21**
- **Spring Boot 3.2.0**
- **Spring AMQP** (RabbitMQ)
- **Spring Data JPA**
- **PostgreSQL**
- **Lombok**
- **Maven**

## Project Structure
```
src/main/java/com/energy/monitoring/
├── config/
│   └── RabbitMQConfig.java          # RabbitMQ configuration
├── consumer/
│   ├── DeviceDataConsumer.java      # Consumes device measurements
│   └── SyncConsumer.java            # Consumes sync events
├── controller/
│   └── MonitoringController.java    # REST API endpoints
├── dto/
│   ├── DeviceDataMessage.java       # Device measurement DTO
│   ├── HourlyConsumptionDTO.java    # API response DTO
│   └── SyncMessage.java             # Sync event DTO
├── entity/
│   ├── Device.java                  # Device entity (synced)
│   ├── DeviceMeasurement.java       # Raw measurement entity
│   ├── HourlyEnergyConsumption.java # Aggregated consumption entity
│   └── User.java                    # User entity (synced)
├── repository/
│   ├── DeviceMeasurementRepository.java
│   ├── DeviceRepository.java
│   ├── HourlyEnergyConsumptionRepository.java
│   └── UserRepository.java
├── service/
│   ├── MonitoringService.java       # Business logic for measurements
│   └── SyncService.java             # Business logic for sync
└── MonitoringApplication.java       # Main application class
```

## Database Schema

### device_measurements
Stores raw measurements from smart meters.
- `id` (BIGINT, PK): Auto-generated ID
- `device_id` (VARCHAR): Device identifier
- `timestamp` (TIMESTAMP): When measurement was taken
- `measurement_value` (DOUBLE): Energy consumption value (kWh)
- `received_at` (TIMESTAMP): When message was received

### hourly_energy_consumption
Stores aggregated hourly consumption data.
- `id` (BIGINT, PK): Auto-generated ID
- `device_id` (VARCHAR): Device identifier
- `hour_timestamp` (TIMESTAMP): Start of the hour
- `total_consumption` (DOUBLE): Sum of measurements in that hour
- `measurement_count` (INTEGER): Number of measurements
- `calculated_at` (TIMESTAMP): When calculation was performed

### devices
Synchronized copy of devices from Device Service.
- `device_id` (VARCHAR, PK): Device identifier
- `name` (VARCHAR): Device name
- `maximum_consumption_value` (DOUBLE): Max consumption threshold
- `user_id` (VARCHAR): Assigned user ID

### users
Synchronized copy of users from User Service.
- `user_id` (VARCHAR, PK): User identifier
- `username` (VARCHAR): Username
- `name` (VARCHAR): Full name

## RabbitMQ Configuration

### Queues Consumed
1. **device_data_queue**
   - Exchange: `data_exchange`
   - Routing Key: `device.data.key`
   - Purpose: Receives device measurements

2. **synchronization_queue**
   - Exchange: `sync_exchange`
   - Routing Key: `sync.key`
   - Purpose: Receives user/device sync events

### Message Formats

**Device Data Message:**
```json
{
  "timestamp": "2024-11-17T10:30:00",
  "deviceId": "device-uuid",
  "measurementValue": 0.45
}
```

**Sync Message (User):**
```json
{
  "type": "USER",
  "action": "CREATE",
  "userId": "user-uuid",
  "username": "john",
  "name": "John Doe"
}
```

**Sync Message (Device):**
```json
{
  "type": "DEVICE",
  "action": "CREATE",
  "deviceId": "device-uuid",
  "deviceName": "Smart Meter 001",
  "maximumConsumptionValue": 5000.0,
  "assignedUserId": "user-uuid"
}
```

## REST API Endpoints

### Get Hourly Consumption
```http
GET /api/monitoring/devices/{deviceId}/hourly
```
Query Parameters:
- `start` (optional): Start datetime (ISO 8601)
- `end` (optional): End datetime (ISO 8601)

Response:
```json
[
  {
    "id": 1,
    "deviceId": "device-uuid",
    "hourTimestamp": "2024-11-17T10:00:00",
    "totalConsumption": 2.45,
    "measurementCount": 6,
    "calculatedAt": "2024-11-17T10:50:00"
  }
]
```

### Get Latest Consumption
```http
GET /api/monitoring/devices/{deviceId}/latest
```

Response:
```json
{
  "id": 1,
  "deviceId": "device-uuid",
  "hourTimestamp": "2024-11-17T10:00:00",
  "totalConsumption": 2.45,
  "measurementCount": 6,
  "calculatedAt": "2024-11-17T10:50:00"
}
```

### Health Check
```http
GET /api/monitoring/health
```

Response:
```
Monitoring Service is running
```

## Configuration

### application.properties
```properties
# Server
server.port=8084

# Database
spring.datasource.url=jdbc:postgresql://postgres:5432/monitoring-db
spring.datasource.username=postgres
spring.datasource.password=root

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# RabbitMQ
spring.rabbitmq.addresses=amqps://jjzdoqha:Albina_16@cow.rmq2.cloudamqp.com/jjzdoqha
rabbitmq.queue.device-data=device_data_queue
rabbitmq.queue.sync=synchronization_queue
rabbitmq.exchange.data=data_exchange
rabbitmq.exchange.sync=sync_exchange
rabbitmq.routing-key.device-data=device.data.key
rabbitmq.routing-key.sync=sync.key
```

## Building and Running

### With Docker
```bash
# Build image
docker build -t monitoring-microservice .

# Run container
docker run -p 8084:8084 \
  -e DB_IP=postgres \
  -e DB_PORT=5432 \
  -e DB_USER=postgres \
  -e DB_PASSWORD=root \
  -e DB_DBNAME=monitoring-db \
  -e RABBITMQ_URL=amqps://... \
  monitoring-microservice
```

### With Maven
```bash
# Build
mvn clean package

# Run
java -jar target/monitoring-microservice-1.0.0.jar
```

## How It Works

### Device Data Processing Flow
1. Producer sends measurement to RabbitMQ
2. `DeviceDataConsumer` receives message
3. `MonitoringService.processDeviceData()` is called
4. Raw measurement is saved to `device_measurements` table
5. `calculateHourlyConsumption()` is triggered
6. Hourly consumption is calculated and updated in `hourly_energy_consumption` table

### Synchronization Flow
1. User/Device Service publishes sync event to RabbitMQ
2. `SyncConsumer` receives message
3. `SyncService.processSyncMessage()` is called
4. Based on type (USER/DEVICE) and action (CREATE/UPDATE/DELETE):
   - User data is synced to `users` table
   - Device data is synced to `devices` table

## Logging

The service logs important events:
- Message reception
- Data processing
- Hourly calculation
- Synchronization events
- Errors and exceptions

View logs:
```bash
docker logs -f ds2025-monitoring-service
```

## Error Handling

- Invalid timestamps: Falls back to current time
- Missing device: Still processes measurement (device sync happens separately)
- Database errors: Logged and transaction rolled back
- RabbitMQ connection issues: Automatic reconnection

## Performance Considerations

- **Asynchronous Processing**: Non-blocking message consumption
- **Database Indexing**: Indexes on `device_id` and `hour_timestamp`
- **Incremental Updates**: Hourly consumption updated incrementally
- **Connection Pooling**: Efficient database connection management
- **Transaction Management**: Atomic operations for data consistency

## Testing

See `TESTING_GUIDE.md` for comprehensive testing scenarios.

Quick test:
```bash
# Health check
curl http://localhost/api/monitoring/health

# Query consumption
curl http://localhost/api/monitoring/devices/{deviceId}/hourly
```

## Troubleshooting

### Service won't start
```bash
# Check logs
docker logs ds2025-monitoring-service

# Common issues:
# - Database not ready: Wait for PostgreSQL to start
# - RabbitMQ connection: Check URL and credentials
# - Port conflict: Ensure 8084 is available
```

### Not receiving messages
```bash
# Check RabbitMQ connection
docker logs ds2025-monitoring-service | grep "RabbitMQ"

# Verify queues exist (CloudAMQP dashboard)
# Check producer is sending to correct exchange/routing key
```

### Hourly consumption not calculating
```bash
# Check if measurements are being saved
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM device_measurements LIMIT 10;"

# Check for errors
docker logs ds2025-monitoring-service | grep "ERROR"
```

## Dependencies

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

## License
This project is part of the Distributed Systems course assignment.

## Author
Daria Fangli - 2024/2025
