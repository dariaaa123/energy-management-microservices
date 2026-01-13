# Device Data Simulator (Producer)

## Overview
This Python script simulates a smart meter device that sends energy consumption measurements to the monitoring system via RabbitMQ.

## Configuration

The simulator uses a configuration file `producer_config.json` to set its parameters.

### Configuration File Format

```json
{
  "rabbitmq_url": "amqps://username:password@host/vhost",
  "exchange": "data_exchange",
  "routing_key": "device.data.key",
  "device_id": "your-device-uuid-here",
  "send_interval_seconds": 600
}
```

### Configuration Parameters

| Parameter | Description | Default |
|-----------|-------------|---------|
| `rabbitmq_url` | CloudAMQP connection URL | Current CloudAMQP instance |
| `exchange` | RabbitMQ exchange name | `data_exchange` |
| `routing_key` | Routing key for messages | `device.data.key` |
| `device_id` | UUID of the device to simulate | `device_001` |
| `send_interval_seconds` | Interval between measurements (seconds) | `600` (10 minutes) |

## Setup

### 1. Install Dependencies

```bash
pip install pika
```

### 2. Configure Device ID

**Option A: Edit the config file directly**
```bash
# Edit producer_config.json
notepad producer_config.json  # Windows
nano producer_config.json     # Linux/Mac
```

Change the `device_id` to match a device in your system:
```json
{
  "device_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

**Option B: Let the script create the default config**
- Run the script once, it will create `producer_config.json`
- Edit the file with your device ID
- Run the script again

### 3. Get Your Device ID

1. Login to the web interface at http://localhost
2. Navigate to Devices section
3. Copy the device ID (UUID) you want to simulate
4. Paste it into `producer_config.json`

## Usage

### Run the Simulator

```bash
python producer.py
```

### Expected Output

```
============================================================
  DEVICE DATA SIMULATOR - Energy Management System
============================================================

[*] Loaded configuration from 'producer_config.json'
    - Device ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890
    - Send Interval: 600 seconds

[*] Connecting to RabbitMQ...
[✓] Connected to RabbitMQ CloudAMQP
[*] Simulating device: a1b2c3d4-e5f6-7890-abcd-ef1234567890
[*] Sending measurements every 600 seconds
[*] Press Ctrl+C to stop

============================================================

[>] Sent: {'timestamp': '2024-11-17T10:30:00', 'deviceId': '...', 'measurementValue': 0.45}
[>] Sent: {'timestamp': '2024-11-17T10:40:00', 'deviceId': '...', 'measurementValue': 0.52}
...
```

### Stop the Simulator

Press `Ctrl+C` to stop the simulator gracefully.

## Message Format

The simulator sends JSON messages with the following structure:

```json
{
  "timestamp": "2024-11-17T10:30:00",
  "deviceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "measurementValue": 0.45
}
```

- `timestamp`: ISO 8601 format datetime (UTC)
- `deviceId`: UUID of the device
- `measurementValue`: Energy consumption in kWh (simulated realistic values)

## Simulation Logic

The simulator generates realistic energy consumption patterns:

| Time Period | Base Load (kWh) | Description |
|-------------|-----------------|-------------|
| 00:00 - 06:00 | 0.1 - 0.3 | Night (low consumption) |
| 06:00 - 12:00 | 0.2 - 0.5 | Morning (moderate) |
| 12:00 - 18:00 | 0.3 - 0.6 | Afternoon (higher) |
| 18:00 - 24:00 | 0.4 - 0.8 | Evening (peak) |

Small random fluctuations (±0.05 kWh) are added to simulate realistic variations.

## Testing Different Intervals

For testing purposes, you can change the interval to send measurements more frequently:

```json
{
  "send_interval_seconds": 60
}
```

This will send a measurement every minute instead of every 10 minutes.

**Recommended intervals:**
- **Production**: 600 seconds (10 minutes) - as per requirements
- **Testing**: 60 seconds (1 minute) - to quickly generate data
- **Demo**: 10 seconds - for live demonstrations

## Troubleshooting

### Error: "Config file not found"
- The script will automatically create `producer_config.json` with default values
- Edit the file and run the script again

### Error: "Connection refused"
- Check that RabbitMQ credentials in `producer_config.json` are correct
- Verify CloudAMQP instance is active

### Error: "Authentication failed"
- Update the `rabbitmq_url` with current credentials
- Check CloudAMQP dashboard for correct password

### No data appearing in monitoring service
- Verify the `device_id` exists in the system
- Check that the device is assigned to a user
- Check monitoring service logs: `docker logs ds2025-monitoring-service`

## Multiple Devices

To simulate multiple devices simultaneously:

1. Create multiple config files:
   ```
   producer_config_device1.json
   producer_config_device2.json
   producer_config_device3.json
   ```

2. Modify `producer.py` to accept config file as argument, or

3. Run multiple instances in different terminals:
   ```bash
   # Terminal 1
   python producer.py  # Uses producer_config.json
   
   # Terminal 2
   # Edit producer_config.json with different device_id
   python producer.py
   ```

## Integration with Monitoring Service

The messages sent by this simulator are:
1. Published to RabbitMQ exchange `data_exchange`
2. Routed to queue `device_data_queue`
3. Consumed by Monitoring Service
4. Stored in `device_measurements` table
5. Aggregated into `hourly_energy_consumption` table
6. Displayed in the web interface charts

## Requirements Met

✅ Standalone desktop application (Python script)
✅ Generates measurements every 10 minutes (configurable)
✅ Sends JSON data to RabbitMQ
✅ Configuration file for device ID (not hardcoded)
✅ Realistic energy consumption patterns

---

**Note**: This simulator is for development and testing purposes. In a production environment, actual smart meter devices would send real measurements.
