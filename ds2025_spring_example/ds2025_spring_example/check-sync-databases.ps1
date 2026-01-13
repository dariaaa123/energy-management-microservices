# Script to verify user synchronization across all databases

Write-Host "=== Checking User Synchronization Across Databases ===" -ForegroundColor Cyan

# Get the most recent user from the main database
Write-Host "`n1. Checking example-db (User Service)..." -ForegroundColor Yellow
$exampleDbUsers = docker exec ds2025-postgres psql -U postgres -d example-db -t -c "SELECT id, username, name FROM users ORDER BY id DESC LIMIT 5;"
Write-Host $exampleDbUsers

# Check device-db
Write-Host "`n2. Checking device-db (Device Service)..." -ForegroundColor Yellow
$deviceDbUsers = docker exec ds2025-postgres psql -U postgres -d device-db -t -c "SELECT user_id, username, name FROM users ORDER BY user_id DESC LIMIT 5;"
if ($deviceDbUsers) {
    Write-Host $deviceDbUsers -ForegroundColor Green
} else {
    Write-Host "   No users found" -ForegroundColor Red
}

# Check monitoring-db
Write-Host "`n3. Checking monitoring-db (Monitoring Service)..." -ForegroundColor Yellow
$monitoringDbUsers = docker exec ds2025-postgres psql -U postgres -d monitoring-db -t -c "SELECT user_id, username, name FROM users ORDER BY user_id DESC LIMIT 5;"
if ($monitoringDbUsers) {
    Write-Host $monitoringDbUsers -ForegroundColor Green
} else {
    Write-Host "   No users found" -ForegroundColor Red
}

# Check auth-db
Write-Host "`n4. Checking auth-db (Authorization Service)..." -ForegroundColor Yellow
$authDbUsers = docker exec ds2025-postgres psql -U postgres -d auth-db -t -c "SELECT id, username, role FROM users ORDER BY id DESC LIMIT 5;"
if ($authDbUsers) {
    Write-Host $authDbUsers -ForegroundColor Green
} else {
    Write-Host "   No users found" -ForegroundColor Red
}

Write-Host "`n=== Database Check Complete ===" -ForegroundColor Cyan
Write-Host "`nNote: User Service uses 'id' (UUID), other services use 'user_id' (String UUID)" -ForegroundColor Gray
