# Synchronization Architecture

## Overview
The system uses RabbitMQ to synchronize user and device data across all microservices in real-time.

## Queue Architecture

### Exchange
- **Name**: `sync_exchange`
- **Type**: Topic Exchange
- **Routing Key**: `sync.key`

### Queues (One per service)
Each service has its own dedicated queue bound to the same exchange, creating a **fanout pattern** where every message is delivered to all services:

1. **auth_sync_queue** → AuthorizationService
2. **device_sync_queue** → DeviceMicroservice  
3. **monitoring_sync_queue** → MonitoringMicroservice

### Why Separate Queues?
When multiple consumers share the same queue, RabbitMQ distributes messages round-robin (load balancing). By giving each service its own queue, every service receives ALL sync messages.

## Message Flow

```
User Service (Publisher)
    ↓
sync_exchange (with routing key: sync.key)
    ↓
    ├─→ auth_sync_queue → AuthorizationService
    ├─→ device_sync_queue → DeviceMicroservice
    └─→ monitoring_sync_queue → MonitoringMicroservice
```

## SyncMessage Structure

```json
{
  "type": "USER" | "DEVICE",
  "action": "CREATE" | "UPDATE" | "DELETE",
  "userId": "uuid-string",
  "username": "string",
  "name": "string",
  "deviceId": "uuid-string",
  "deviceName": "string",
  "maximumConsumptionValue": 0.0,
  "assignedUserId": "uuid-string"
}
```

## Service Responsibilities

### User Service (demo)
- **Publishes**: User CREATE, UPDATE, DELETE events
- **Consumes**: Nothing (source of truth for users)

### AuthorizationService
- **Publishes**: Nothing
- **Consumes**: User DELETE events (to remove users from auth database)
- **Note**: User CREATE is handled via direct REST API call during registration

### DeviceMicroservice
- **Publishes**: Device CREATE, UPDATE, DELETE events
- **Consumes**: User CREATE, UPDATE, DELETE events (to maintain user references)

### MonitoringMicroservice
- **Publishes**: Nothing
- **Consumes**: 
  - User CREATE, UPDATE, DELETE events
  - Device CREATE, UPDATE, DELETE events
  - Device measurement data (separate queue: `device_data_queue`)

## Testing

Run the synchronization test:
```powershell
.\test-sync.ps1
```

Check logs for all services:
```powershell
.\check-rabbitmq-logs.ps1
```

Rebuild services after changes:
```powershell
.\rebuild-services.ps1
```

## Troubleshooting

### Messages not being received
1. Check if services are connected to the same RabbitMQ instance
2. Verify queue names are unique per service
3. Check logs for connection errors
4. Verify exchange and routing key match across all services

### Partial synchronization
- If only some services receive messages, check if they're sharing the same queue name
- Each service MUST have its own unique queue name
