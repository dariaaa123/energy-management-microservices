# Quick query examples for common use cases

Write-Host "=== Quick Query Examples ===" -ForegroundColor Cyan

# Example 1: Get all users from User Service
Write-Host "`n1. All users from User Service:" -ForegroundColor Yellow
docker exec ds2025-postgres psql -U postgres -d example-db -c "SELECT id, username, name, role FROM users;"

# Example 2: Get all devices with their assigned users
Write-Host "`n2. All devices with assigned users (Device Service):" -ForegroundColor Yellow
docker exec ds2025-postgres psql -U postgres -d device-db -c "SELECT device_id, name, maximum_consumption_value, user_id FROM devices;"

# Example 3: Get recent device measurements
Write-Host "`n3. Recent device measurements (last 10):" -ForegroundColor Yellow
docker exec ds2025-postgres psql -U postgres -d monitoring-db -c "SELECT device_id, measurement_value, timestamp FROM device_measurements ORDER BY timestamp DESC LIMIT 10;"

# Example 4: Get hourly consumption summary
Write-Host "`n4. Hourly consumption summary (last 10 hours):" -ForegroundColor Yellow
docker exec ds2025-postgres psql -U postgres -d monitoring-db -c "SELECT device_id, hour, total_consumption FROM hourly_energy_consumption ORDER BY hour DESC LIMIT 10;"

# Example 5: Count users in each database
Write-Host "`n5. User count in each database:" -ForegroundColor Yellow
Write-Host "   example-db:" -ForegroundColor Gray
docker exec ds2025-postgres psql -U postgres -d example-db -t -c "SELECT COUNT(*) FROM users;"
Write-Host "   device-db:" -ForegroundColor Gray
docker exec ds2025-postgres psql -U postgres -d device-db -t -c "SELECT COUNT(*) FROM users;"
Write-Host "   monitoring-db:" -ForegroundColor Gray
docker exec ds2025-postgres psql -U postgres -d monitoring-db -t -c "SELECT COUNT(*) FROM users;"
Write-Host "   auth-db:" -ForegroundColor Gray
docker exec ds2025-postgres psql -U postgres -d auth-db -t -c "SELECT COUNT(*) FROM users;"

# Example 6: Find a specific user by username across all databases
Write-Host "`n6. Find user 'synctest_161800' across all databases:" -ForegroundColor Yellow
Write-Host "   example-db:" -ForegroundColor Gray
docker exec ds2025-postgres psql -U postgres -d example-db -c "SELECT id, username, name FROM users WHERE username='synctest_161800';"
Write-Host "   device-db:" -ForegroundColor Gray
docker exec ds2025-postgres psql -U postgres -d device-db -c "SELECT user_id, username, name FROM users WHERE username='synctest_161800';"
Write-Host "   monitoring-db:" -ForegroundColor Gray
docker exec ds2025-postgres psql -U postgres -d monitoring-db -c "SELECT user_id, username, name FROM users WHERE username='synctest_161800';"

Write-Host "`n=== Examples Complete ===" -ForegroundColor Cyan
