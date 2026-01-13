# Dockerized Device Data Producer

## Overview
The producer is now available as a Docker container for easy deployment on any machine.

## Quick Start

### Option 1: Run as Standalone (Original)
```bash
# Install dependencies
pip install pika

# Edit config
notepad producer_config.json

# Run directly
python producer.py
```

### Option 2: Run in Docker (Portable)
```bash
# Edit config file
notepad producer_config.json

# Build and run
docker-compose --profile producer up -d

# View logs
docker logs -f ds2025-producer

# Stop
docker-compose --profile producer down
```

## Configuration

Edit `producer_config.json` in the project root:

```json
{
  "rabbitmq_url": "amqps://username:password@host/vhost",
  "exchange": "data_exchange",
  "routing_key": "device.data.key",
  "device_id": "your-device-uuid-here",
  "send_interval_seconds": 600
}
```

**Important:** The config file is mounted as a volume, so:
- ✅ Changes take effect after container restart
- ✅ Config persists outside container
- ✅ Easy to edit without rebuilding

## Usage

### Start the Producer

```bash
# Start with all other services
docker-compose --profile producer up -d

# Or start just the producer
docker-compose up -d producer
```

### View Producer Logs

```bash
# Follow logs in real-time
docker logs -f ds2025-producer

# View last 50 lines
docker logs --tail 50 ds2025-producer
```

### Stop the Producer

```bash
# Stop producer only
docker-compose stop producer

# Stop and remove
docker-compose down producer

# Stop all services including producer
docker-compose --profile producer down
```

### Restart After Config Change

```bash
# Edit config
notepad producer_config.json

# Restart producer to apply changes
docker-compose restart producer
```

## Why Use Docker Profile?

The producer uses a Docker **profile** called `producer`. This means:

- ✅ **Optional** - Won't start automatically with `docker-compose up`
- ✅ **On-demand** - Start only when you need it
- ✅ **Flexible** - Can run standalone or in Docker

### Start All Services WITHOUT Producer
```bash
docker-compose up -d
```

### Start All Services WITH Producer
```bash
docker-compose --profile producer up -d
```

## Advantages of Dockerized Producer

1. **Portability** - Runs on any machine with Docker
2. **No Python Installation** - Everything in container
3. **Consistent Environment** - Same behavior everywhere
4. **Easy Deployment** - One command to start
5. **Automatic Restart** - Restarts if it crashes
6. **Isolated** - Doesn't affect host system

## Troubleshooting

### Producer Not Starting

```bash
# Check if container exists
docker ps -a | findstr producer

# Check logs for errors
docker logs ds2025-producer

# Rebuild if needed
docker-compose build producer
docker-compose up -d producer
```

### Config Changes Not Applied

```bash
# Restart the container
docker-compose restart producer

# Or rebuild (if you changed Dockerfile)
docker-compose build producer
docker-compose up -d producer
```

### Connection Issues

```bash
# Check if RabbitMQ URL is correct in config
notepad producer_config.json

# Verify network connectivity
docker exec ds2025-producer ping -c 3 cow.rmq2.cloudamqp.com

# Check monitoring service is running
docker ps | findstr monitoring
```

## Multi-Device Setup

For multiple devices, you have two options:

### Option A: Multiple Config Files + Multiple Containers

1. Create `producer_config_device1.json`, `producer_config_device2.json`, etc.
2. Modify docker-compose.yml to add multiple producer services
3. Each uses a different config file

### Option B: Use Multi-Producer Script

1. Use `multi_producer.py` instead
2. Edit `multi_producer_config.json` with all device IDs
3. One container simulates all devices

## Deployment on Another Machine

### Step 1: Copy Project
```bash
# Copy entire project folder to new machine
# Or clone from git repository
```

### Step 2: Edit Config
```bash
# Edit producer_config.json with device IDs
notepad producer_config.json
```

### Step 3: Start Services
```bash
# Start all services including producer
docker-compose --profile producer up -d
```

That's it! The producer will start sending data automatically.

## Comparison: Standalone vs Docker

| Feature | Standalone | Docker |
|---------|-----------|--------|
| Python Required | ✅ Yes | ❌ No |
| Pip Install | ✅ Yes | ❌ No |
| Portable | ⚠️ Needs Python | ✅ Runs anywhere |
| Easy Deployment | ⚠️ Manual setup | ✅ One command |
| Auto-restart | ❌ No | ✅ Yes |
| Config Changes | ✅ Just restart script | ✅ Restart container |
| Logs | Terminal only | Docker logs |
| Resource Isolation | ❌ No | ✅ Yes |

## Best Practices

1. **Always edit config before starting** - Set correct device ID
2. **Use meaningful device IDs** - Get from your system
3. **Monitor logs initially** - Ensure messages are being sent
4. **Adjust interval for testing** - Use 60 seconds instead of 600
5. **Stop when not needed** - Saves CloudAMQP quota

## Integration with CI/CD

The dockerized producer can be easily integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Start Producer
  run: docker-compose --profile producer up -d

- name: Wait for data
  run: sleep 120

- name: Run tests
  run: pytest tests/

- name: Stop Producer
  run: docker-compose --profile producer down
```

---

**Note:** The producer is still a "standalone desktop application" as per requirements - it just happens to run in a container for portability! 🚀
