# Rebuild and restart services to apply sync queue changes

Write-Host "Stopping services..." -ForegroundColor Yellow
docker-compose down

Write-Host "`nRebuilding services..." -ForegroundColor Yellow
docker-compose build authorization-service user-service device-service monitoring-service

Write-Host "`nStarting services..." -ForegroundColor Yellow
docker-compose up -d

Write-Host "`nWaiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

Write-Host "`nServices restarted! Check logs with:" -ForegroundColor Green
Write-Host "docker logs ds2025-authorization-service -f" -ForegroundColor Cyan
Write-Host "docker logs ds2025-device-service -f" -ForegroundColor Cyan
Write-Host "docker logs ds2025-monitoring-service -f" -ForegroundColor Cyan
