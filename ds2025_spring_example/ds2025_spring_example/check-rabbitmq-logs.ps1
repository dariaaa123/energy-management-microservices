# Check RabbitMQ sync logs from all services

Write-Host "=== Checking User Service Logs ===" -ForegroundColor Cyan
docker logs ds2025-user-service 2>&1 | Select-String -Pattern "sync|Sync|SYNC|RabbitMQ|rabbitmq" | Select-Object -Last 15

Write-Host "`n=== Checking Authorization Service Logs ===" -ForegroundColor Cyan
docker logs ds2025-authorization-service 2>&1 | Select-String -Pattern "sync|Sync|SYNC|RabbitMQ|rabbitmq|SyncConsumer" | Select-Object -Last 15

Write-Host "`n=== Checking Device Service Logs ===" -ForegroundColor Cyan
docker logs ds2025-device-service 2>&1 | Select-String -Pattern "sync|Sync|SYNC|RabbitMQ|rabbitmq|SyncConsumer" | Select-Object -Last 15

Write-Host "`n=== Checking Monitoring Service Logs ===" -ForegroundColor Cyan
docker logs ds2025-monitoring-service 2>&1 | Select-String -Pattern "sync|Sync|SYNC|RabbitMQ|rabbitmq|SyncConsumer" | Select-Object -Last 15
