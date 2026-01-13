# Script to query all tables from all databases

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DATABASE QUERY - ALL TABLES" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# ===== EXAMPLE-DB (User Service) =====
Write-Host "`n╔════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║     EXAMPLE-DB (User Service)          ║" -ForegroundColor Yellow
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Yellow

Write-Host "`n--- USERS Table ---" -ForegroundColor Green
docker exec ds2025-postgres psql -U postgres -d example-db -c "SELECT * FROM users;"

# ===== DEVICE-DB (Device Service) =====
Write-Host "`n╔════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║     DEVICE-DB (Device Service)         ║" -ForegroundColor Yellow
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Yellow

Write-Host "`n--- USERS Table ---" -ForegroundColor Green
docker exec ds2025-postgres psql -U postgres -d device-db -c "SELECT * FROM users;"

Write-Host "`n--- DEVICES Table ---" -ForegroundColor Green
docker exec ds2025-postgres psql -U postgres -d device-db -c "SELECT * FROM devices;"

# ===== MONITORING-DB (Monitoring Service) =====
Write-Host "`n╔════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║   MONITORING-DB (Monitoring Service)   ║" -ForegroundColor Yellow
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Yellow

Write-Host "`n--- USERS Table ---" -ForegroundColor Green
docker exec ds2025-postgres psql -U postgres -d monitoring-db -c "SELECT * FROM users;"

Write-Host "`n--- DEVICES Table ---" -ForegroundColor Green
docker exec ds2025-postgres psql -U postgres -d monitoring-db -c "SELECT * FROM devices;"

Write-Host "`n--- DEVICE_MEASUREMENTS Table ---" -ForegroundColor Green
docker exec ds2025-postgres psql -U postgres -d monitoring-db -c "SELECT * FROM device_measurements ORDER BY timestamp DESC LIMIT 10;"

Write-Host "`n--- HOURLY_ENERGY_CONSUMPTION Table ---" -ForegroundColor Green
docker exec ds2025-postgres psql -U postgres -d monitoring-db -c "SELECT * FROM hourly_energy_consumption ORDER BY hour DESC LIMIT 10;"

# ===== AUTH-DB (Authorization Service) =====
Write-Host "`n╔════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║   AUTH-DB (Authorization Service)      ║" -ForegroundColor Yellow
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Yellow

Write-Host "`n--- USERS Table ---" -ForegroundColor Green
docker exec ds2025-postgres psql -U postgres -d auth-db -c "SELECT * FROM users;"

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  QUERY COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
