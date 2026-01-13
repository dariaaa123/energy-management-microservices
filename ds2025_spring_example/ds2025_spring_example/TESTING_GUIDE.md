# Testing Guide - Monitoring Microservice

## Complete Testing Scenarios

### Scenario 1: End-to-End Device Monitoring

#### Step 1: Setup
```bash
# Start all services
docker-compose up -d

# Wait for services to be ready
docker-compose logs -f monitoring-service | grep "Started"
```

#### Step 2: Create Test User
```bash
# Login and get token
curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Save the token from response
TOKEN="your-jwt-token-here"

# Create user
curl -X POST http://localhost/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "testuser",
    "password": "test123",
    "role": "CLIENT",
    "name": "Test User",
    "address": "123 Test St",
    "age": 30
  }'

# Verify sync in monitoring service
docker logs ds2025-monitoring-service | grep "Synced user"
```

#### Step 3: Create Test Device
```bash
# Create device
curl -X POST http://localhost/api/devices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Role: ADMIN" \
  -d '{
    "name": "Smart Meter Test 001",
    "maximumConsumptionValue": 5000
  }'

# Save device ID from response
DEVICE_ID="device-uuid-here"

# Verify sync in monitoring service
docker logs ds2025-monitoring-service | grep "Synced device"

# Verify in database
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM devices WHERE device_id='$DEVICE_ID';"
```

#### Step 4: Send Device Measurements
```bash
# Update producer.py with your device ID
# Edit line 13: DEVICE_ID = "$DEVICE_ID"

# Run simulator
python producer.py

# In another terminal, monitor logs
docker logs -f ds2025-monitoring-service
```

#### Step 5: Verify Data Processing
```bash
# Check raw measurements
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM device_measurements WHERE device_id='$DEVICE_ID' ORDER BY timestamp DESC LIMIT 5;"

# Check hourly consumption
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM hourly_energy_consumption WHERE device_id='$DEVICE_ID' ORDER BY hour_timestamp DESC;"
```

#### Step 6: Query via API
```bash
# Get all hourly consumption
curl "http://localhost/api/monitoring/devices/$DEVICE_ID/hourly"

# Get latest consumption
curl "http://localhost/api/monitoring/devices/$DEVICE_ID/latest"

# Get consumption for specific date range
curl "http://localhost/api/monitoring/devices/$DEVICE_ID/hourly?start=2024-11-17T00:00:00&end=2024-11-17T23:59:59"
```

---

### Scenario 2: Test Synchronization

#### Test User Synchronization

```bash
# Create user
curl -X POST http://localhost/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "synctest",
    "password": "sync123",
    "role": "CLIENT",
    "name": "Sync Test User",
    "address": "456 Sync Ave",
    "age": 25
  }'

# Check monitoring service received sync message
docker logs ds2025-monitoring-service | grep "Received sync message" | tail -1

# Verify in monitoring database
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM users WHERE username='synctest';"

# Update user
USER_ID="user-uuid-from-create"
curl -X PUT "http://localhost/api/users/$USER_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "username": "synctest",
    "password": "sync123",
    "role": "CLIENT",
    "name": "Updated Sync User",
    "address": "789 Updated St",
    "age": 26
  }'

# Verify update in monitoring database
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM users WHERE user_id='$USER_ID';"

# Delete user
curl -X DELETE "http://localhost/api/users/$USER_ID" \
  -H "Authorization: Bearer $TOKEN"

# Verify deletion in monitoring database
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM users WHERE user_id='$USER_ID';"
```

#### Test Device Synchronization

```bash
# Create device
curl -X POST http://localhost/api/devices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Role: ADMIN" \
  -d '{
    "name": "Sync Test Device",
    "maximumConsumptionValue": 3000
  }'

DEVICE_ID="device-uuid-from-create"

# Check monitoring service received sync message
docker logs ds2025-monitoring-service | grep "Synced device" | tail -1

# Verify in monitoring database
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM devices WHERE device_id='$DEVICE_ID';"

# Assign device to user
curl -X PUT "http://localhost/api/devices/$DEVICE_ID/assign/$USER_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Role: ADMIN"

# Verify assignment in monitoring database
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM devices WHERE device_id='$DEVICE_ID';"

# Delete device
curl -X DELETE "http://localhost/api/devices/$DEVICE_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Role: ADMIN"

# Verify deletion
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM devices WHERE device_id='$DEVICE_ID';"
```

---

### Scenario 3: Test Hourly Consumption Calculation

#### Send Multiple Measurements in Same Hour

```python
# Create test_hourly.py
import pika
import json
from datetime import datetime, timedelta

RABBIT_URL = "amqps://jjzdoqha:Albina_16@cow.rmq2.cloudamqp.com/jjzdoqha"
EXCHANGE = "data_exchange"
ROUTING_KEY = "device.data.key"
DEVICE_ID = "your-device-id"

params = pika.URLParameters(RABBIT_URL)
connection = pika.BlockingConnection(params)
channel = connection.channel()

# Send 6 measurements for the same hour (10-minute intervals)
base_time = datetime.now().replace(minute=0, second=0, microsecond=0)

for i in range(6):
    timestamp = base_time + timedelta(minutes=i*10)
    value = 0.4 + (i * 0.05)  # Increasing values
    
    msg = {
        "timestamp": timestamp.isoformat(),
        "deviceId": DEVICE_ID,
        "measurementValue": value
    }
    
    channel.basic_publish(
        exchange=EXCHANGE,
        routing_key=ROUTING_KEY,
        body=json.dumps(msg),
        properties=pika.BasicProperties(delivery_mode=2)
    )
    
    print(f"Sent: {msg}")

connection.close()
print("Done! Check monitoring service logs.")
```

```bash
# Run the test
python test_hourly.py

# Verify hourly calculation
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT device_id, hour_timestamp, total_consumption, measurement_count 
      FROM hourly_energy_consumption 
      WHERE device_id='$DEVICE_ID' 
      ORDER BY hour_timestamp DESC 
      LIMIT 1;"

# Expected: total_consumption ≈ 2.625, measurement_count = 6
```

---

### Scenario 4: Load Testing

#### Send Burst of Messages

```python
# Create load_test.py
import pika
import json
from datetime import datetime
import random

RABBIT_URL = "amqps://jjzdoqha:Albina_16@cow.rmq2.cloudamqp.com/jjzdoqha"
EXCHANGE = "data_exchange"
ROUTING_KEY = "device.data.key"
DEVICE_ID = "your-device-id"

params = pika.URLParameters(RABBIT_URL)
connection = pika.BlockingConnection(params)
channel = connection.channel()

# Send 100 messages
for i in range(100):
    msg = {
        "timestamp": datetime.now().isoformat(),
        "deviceId": DEVICE_ID,
        "measurementValue": random.uniform(0.1, 1.0)
    }
    
    channel.basic_publish(
        exchange=EXCHANGE,
        routing_key=ROUTING_KEY,
        body=json.dumps(msg),
        properties=pika.BasicProperties(delivery_mode=2)
    )
    
    if (i + 1) % 10 == 0:
        print(f"Sent {i + 1} messages")

connection.close()
print("Load test complete!")
```

```bash
# Run load test
python load_test.py

# Monitor processing
docker logs -f ds2025-monitoring-service | grep "Saved measurement"

# Check database
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT COUNT(*) FROM device_measurements WHERE device_id='$DEVICE_ID';"
```

---

### Scenario 5: Error Handling Tests

#### Test Invalid Device ID

```python
# Send message with non-existent device
import pika
import json
from datetime import datetime

RABBIT_URL = "amqps://jjzdoqha:Albina_16@cow.rmq2.cloudamqp.com/jjzdoqha"
EXCHANGE = "data_exchange"
ROUTING_KEY = "device.data.key"

params = pika.URLParameters(RABBIT_URL)
connection = pika.BlockingConnection(params)
channel = connection.channel()

msg = {
    "timestamp": datetime.now().isoformat(),
    "deviceId": "non-existent-device-id",
    "measurementValue": 0.5
}

channel.basic_publish(
    exchange=EXCHANGE,
    routing_key=ROUTING_KEY,
    body=json.dumps(msg),
    properties=pika.BasicProperties(delivery_mode=2)
)

connection.close()
print("Sent message with invalid device ID")
```

```bash
# Check logs for error handling
docker logs ds2025-monitoring-service | grep "non-existent-device-id"

# Should still save measurement (device sync happens separately)
```

#### Test Invalid Timestamp Format

```python
# Send message with invalid timestamp
msg = {
    "timestamp": "invalid-timestamp",
    "deviceId": "your-device-id",
    "measurementValue": 0.5
}

# Send via RabbitMQ
# Check logs - should use current time as fallback
docker logs ds2025-monitoring-service | grep "Failed to parse timestamp"
```

---

### Scenario 6: Data Verification

#### Verify Data Consistency

```bash
# Count measurements
MEASUREMENT_COUNT=$(docker exec -it ds2025-postgres psql -U postgres -d monitoring-db -t \
  -c "SELECT COUNT(*) FROM device_measurements WHERE device_id='$DEVICE_ID';")

echo "Total measurements: $MEASUREMENT_COUNT"

# Verify hourly aggregation
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT 
        hour_timestamp,
        total_consumption,
        measurement_count,
        (SELECT SUM(measurement_value) 
         FROM device_measurements 
         WHERE device_id='$DEVICE_ID' 
         AND timestamp >= hour_timestamp 
         AND timestamp < hour_timestamp + INTERVAL '1 hour') as calculated_sum
      FROM hourly_energy_consumption 
      WHERE device_id='$DEVICE_ID'
      ORDER BY hour_timestamp DESC
      LIMIT 5;"

# total_consumption should match calculated_sum
```

---

### Scenario 7: API Testing with Different Parameters

```bash
# Test 1: Get all consumption data
curl -v "http://localhost/api/monitoring/devices/$DEVICE_ID/hourly"

# Test 2: Get data for specific date range
START_DATE="2024-11-17T00:00:00"
END_DATE="2024-11-17T23:59:59"
curl -v "http://localhost/api/monitoring/devices/$DEVICE_ID/hourly?start=$START_DATE&end=$END_DATE"

# Test 3: Get latest consumption
curl -v "http://localhost/api/monitoring/devices/$DEVICE_ID/latest"

# Test 4: Non-existent device (should return empty array or 404)
curl -v "http://localhost/api/monitoring/devices/non-existent-id/hourly"

# Test 5: Health check
curl -v "http://localhost/api/monitoring/health"
```

---

### Scenario 8: Performance Testing

```bash
# Test response time
time curl "http://localhost/api/monitoring/devices/$DEVICE_ID/hourly"

# Test with large date range
START_DATE="2024-01-01T00:00:00"
END_DATE="2024-12-31T23:59:59"
time curl "http://localhost/api/monitoring/devices/$DEVICE_ID/hourly?start=$START_DATE&end=$END_DATE"

# Monitor database performance
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db \
  -c "EXPLAIN ANALYZE 
      SELECT * FROM hourly_energy_consumption 
      WHERE device_id='$DEVICE_ID' 
      ORDER BY hour_timestamp DESC;"
```

---

## Automated Test Script

```bash
#!/bin/bash
# test_all.sh - Complete automated test

set -e

echo "=== Starting Automated Tests ==="

# 1. Check services are running
echo "1. Checking services..."
docker-compose ps | grep "Up" || exit 1

# 2. Health check
echo "2. Health check..."
curl -f http://localhost/api/monitoring/health || exit 1

# 3. Login
echo "3. Logging in..."
TOKEN=$(curl -s -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

echo "Token: $TOKEN"

# 4. Create device
echo "4. Creating device..."
DEVICE_RESPONSE=$(curl -s -X POST http://localhost/api/devices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Role: ADMIN" \
  -d '{"name":"Test Device","maximumConsumptionValue":5000}')

DEVICE_ID=$(echo $DEVICE_RESPONSE | jq -r '.id')
echo "Device ID: $DEVICE_ID"

# 5. Wait for sync
echo "5. Waiting for sync..."
sleep 5

# 6. Verify device in monitoring DB
echo "6. Verifying device sync..."
docker exec ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT * FROM devices WHERE device_id='$DEVICE_ID';" || exit 1

# 7. Send test measurement
echo "7. Sending test measurement..."
python3 << EOF
import pika, json
from datetime import datetime

params = pika.URLParameters("amqps://jjzdoqha:Albina_16@cow.rmq2.cloudamqp.com/jjzdoqha")
connection = pika.BlockingConnection(params)
channel = connection.channel()

msg = {
    "timestamp": datetime.now().isoformat(),
    "deviceId": "$DEVICE_ID",
    "measurementValue": 0.5
}

channel.basic_publish(
    exchange="data_exchange",
    routing_key="device.data.key",
    body=json.dumps(msg),
    properties=pika.BasicProperties(delivery_mode=2)
)

connection.close()
print("Message sent!")
EOF

# 8. Wait for processing
echo "8. Waiting for processing..."
sleep 10

# 9. Query API
echo "9. Querying API..."
curl -f "http://localhost/api/monitoring/devices/$DEVICE_ID/hourly" || exit 1

# 10. Verify in database
echo "10. Verifying in database..."
docker exec ds2025-postgres psql -U postgres -d monitoring-db \
  -c "SELECT COUNT(*) FROM device_measurements WHERE device_id='$DEVICE_ID';" || exit 1

echo "=== All Tests Passed! ==="
```

```bash
# Make executable and run
chmod +x test_all.sh
./test_all.sh
```

---

## Expected Results Summary

| Test | Expected Result |
|------|----------------|
| Service Health | HTTP 200, "Monitoring Service is running" |
| User Sync | User appears in monitoring-db.users |
| Device Sync | Device appears in monitoring-db.devices |
| Measurement Processing | Entry in device_measurements table |
| Hourly Calculation | Entry in hourly_energy_consumption table |
| API Query | JSON array with consumption data |
| Load Test (100 msgs) | All 100 measurements saved |
| Invalid Device ID | Message processed, no error |
| Invalid Timestamp | Fallback to current time |

---

## Troubleshooting Tests

If tests fail, check:

```bash
# 1. Service logs
docker logs ds2025-monitoring-service

# 2. Database connection
docker exec -it ds2025-postgres psql -U postgres -l

# 3. RabbitMQ connection
docker logs ds2025-monitoring-service | grep "RabbitMQ"

# 4. Queue status (CloudAMQP dashboard)
# Visit: https://customer.cloudamqp.com/

# 5. Network connectivity
docker network inspect ds2025_demo_net
```

---

**Happy Testing!** 🧪
