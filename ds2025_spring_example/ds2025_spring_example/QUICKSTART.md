# Quick Start Guide - Assignment 2

## 🚀 Get Started in 5 Minutes

### Step 1: Start All Services (2 minutes)

```bash
# Build and start everything
docker-compose build
docker-compose up -d

# Wait for services to be ready (check logs)
docker-compose logs -f monitoring-service
```

Wait until you see: `Started MonitoringApplication`

### Step 2: Create Test Data (1 minute)

1. Open browser: `http://localhost`
2. Login: `admin` / `admin123`
3. Create a device:
   - Name: "Test Meter"
   - Max Consumption: 5000
4. **Copy the device ID** from the URL or response

### Step 3: Run Simulator (1 minute)

```bash
# Edit producer.py - change DEVICE_ID to your device UUID
# Line 13: DEVICE_ID = "your-device-uuid-here"

# Run simulator
python producer.py
```

### Step 4: Verify It Works (1 minute)

```bash
# Check if messages are being processed
docker logs -f ds2025-monitoring-service

# You should see:
# "Received device data message"
# "Saved measurement"
# "Updated hourly consumption"

# Query the API (replace {deviceId} with your device UUID)
curl http://localhost/api/monitoring/devices/{deviceId}/latest
```

---

## 🎯 What You Should See

### In Monitoring Service Logs:
```
Received device data message: DeviceDataMessage(timestamp=2024-11-17T10:30:00, deviceId=abc-123, measurementValue=0.45)
Saved measurement: DeviceMeasurement(id=1, deviceId=abc-123, ...)
Calculating hourly consumption for device abc-123 at hour 2024-11-17T10:00:00
Updated hourly consumption: HourlyEnergyConsumption(totalConsumption=2.45, measurementCount=6)
```

### API Response:
```json
{
  "id": 1,
  "deviceId": "abc-123",
  "hourTimestamp": "2024-11-17T10:00:00",
  "totalConsumption": 2.45,
  "measurementCount": 6,
  "calculatedAt": "2024-11-17T10:50:00"
}
```

---

## 🧪 Test Synchronization

### Test User Sync:
```bash
# Create a user via API or frontend
# Check monitoring service logs:
docker logs ds2025-monitoring-service | grep "Synced user"

# Output: Synced user: User(userId=..., username=testuser, name=Test User)
```

### Test Device Sync:
```bash
# Create a device via API or frontend
# Check monitoring service logs:
docker logs ds2025-monitoring-service | grep "Synced device"

# Output: Synced device: Device(deviceId=..., name=Smart Meter, ...)
```

---

## 🔧 Common Issues

### Producer can't connect?
```bash
# Test RabbitMQ connection
pip install pika
python -c "import pika; print('OK')"
```

### No messages in monitoring service?
```bash
# Check if queues exist (CloudAMQP dashboard)
# Restart monitoring service
docker-compose restart monitoring-service
```

### Device ID not found?
```bash
# List all devices
curl http://localhost/api/devices

# Use the "id" field from the response
```

---

## 📊 Useful Commands

```bash
# View all logs
docker-compose logs -f

# View specific service
docker logs -f ds2025-monitoring-service

# Check database
docker exec -it ds2025-postgres psql -U postgres -d monitoring-db

# List measurements
SELECT * FROM device_measurements ORDER BY timestamp DESC LIMIT 10;

# List hourly consumption
SELECT * FROM hourly_energy_consumption ORDER BY hour_timestamp DESC;

# Stop everything
docker-compose down

# Clean restart
docker-compose down -v
docker-compose build
docker-compose up -d
```

---

## 🎓 Assignment Checklist

- ✅ Monitoring Microservice running on port 8084
- ✅ RabbitMQ integration (CloudAMQP)
- ✅ Device data consumer processing measurements
- ✅ Hourly consumption calculation working
- ✅ Synchronization consumer receiving user/device events
- ✅ REST API endpoints responding
- ✅ Python simulator sending data every 10 minutes
- ✅ Separate monitoring-db database
- ✅ All services in docker-compose
- ✅ Documentation complete

---

## 📚 Next Steps

1. Read `ASSIGNMENT2_README.md` for detailed documentation
2. Test all API endpoints
3. Verify synchronization with multiple users/devices
4. Run simulator for extended period (1+ hour)
5. Query historical consumption data
6. Check CloudAMQP dashboard for message statistics

---

**Need Help?** Check the troubleshooting section in ASSIGNMENT2_README.md
