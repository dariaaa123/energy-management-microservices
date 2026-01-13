#!/usr/bin/env pwsh
# Test script for user-device synchronization via RabbitMQ

Write-Host "=== Testing User-Device Synchronization ===" -ForegroundColor Cyan

# Step 1: Create a test user with unique username
$timestamp = Get-Date -Format "HHmmss"
$username = "synctest_$timestamp"
Write-Host "`n1. Creating test user: $username..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost/api/users" -Method Post -ContentType "application/json" -Body "{`"username`":`"$username`",`"password`":`"test123`",`"role`":`"CLIENT`",`"name`":`"Sync Test`",`"address`":`"Test Address`",`"age`":30}"
    $userId = $response
    Write-Host "   User created with ID: $userId" -ForegroundColor Green
} catch {
    Write-Host "   Failed to create user: $_" -ForegroundColor Red
    exit 1
}

# Step 2: Wait for sync
Write-Host "`n2. Waiting 3 seconds for RabbitMQ sync..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# Step 3: Check DeviceMicroservice
Write-Host "`n3. Checking DeviceMicroservice..." -ForegroundColor Yellow
try {
    $deviceUsers = Invoke-RestMethod -Uri "http://localhost/api/devices/users" -Method Get
    $foundInDevice = $deviceUsers | Where-Object { $_.id -eq $userId }
    if ($foundInDevice) {
        Write-Host "   User found in DeviceMicroservice" -ForegroundColor Green
    } else {
        Write-Host "   User NOT found in DeviceMicroservice" -ForegroundColor Red
    }
} catch {
    Write-Host "   Error checking DeviceMicroservice: $_" -ForegroundColor Red
}

# Step 4: Check MonitoringMicroservice
Write-Host "`n4. Checking MonitoringMicroservice..." -ForegroundColor Yellow
try {
    $monitoringUsers = Invoke-RestMethod -Uri "http://localhost/api/monitoring/users" -Method Get
    $foundInMonitoring = $monitoringUsers | Where-Object { $_.id -eq $userId }
    if ($foundInMonitoring) {
        Write-Host "   User found in MonitoringMicroservice" -ForegroundColor Green
    } else {
        Write-Host "   User NOT found in MonitoringMicroservice" -ForegroundColor Red
    }
} catch {
    Write-Host "   Error checking MonitoringMicroservice: $_" -ForegroundColor Red
}

# Step 5: Update user
Write-Host "`n5. Updating user..." -ForegroundColor Yellow
$updatedUsername = "${username}_updated"
try {
    Invoke-RestMethod -Uri "http://localhost/api/users/$userId" -Method Put -ContentType "application/json" -Body "{`"username`":`"$updatedUsername`",`"role`":`"CLIENT`",`"name`":`"Updated Name`",`"address`":`"Updated Address`",`"age`":35}" | Out-Null
    Write-Host "   User updated to: $updatedUsername" -ForegroundColor Green
} catch {
    Write-Host "   Failed to update user: $_" -ForegroundColor Red
}

# Step 6: Wait and verify update
Write-Host "`n6. Waiting 3 seconds for update sync..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

Write-Host "`n7. Verifying update in services..." -ForegroundColor Yellow
$updatedUsername = "${username}_updated"
try {
    $deviceUsers = Invoke-RestMethod -Uri "http://localhost/api/devices/users" -Method Get
    $updatedUser = $deviceUsers | Where-Object { $_.id -eq $userId }
    if ($updatedUser.username -eq $updatedUsername) {
        Write-Host "   Update synced to DeviceMicroservice" -ForegroundColor Green
    } else {
        Write-Host "   Update NOT synced to DeviceMicroservice (expected: $updatedUsername, got: $($updatedUser.username))" -ForegroundColor Red
    }
} catch {
    Write-Host "   Error verifying update: $_" -ForegroundColor Red
}

# Step 7: Delete user
Write-Host "`n8. Deleting user..." -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "http://localhost/api/users/$userId" -Method Delete | Out-Null
    Write-Host "   User deleted" -ForegroundColor Green
} catch {
    Write-Host "   Failed to delete user: $_" -ForegroundColor Red
}

# Step 8: Wait and verify deletion
Write-Host "`n9. Waiting 3 seconds for delete sync..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

Write-Host "`n10. Verifying deletion in services..." -ForegroundColor Yellow
try {
    $deviceUsers = Invoke-RestMethod -Uri "http://localhost/api/devices/users" -Method Get
    $deletedUser = $deviceUsers | Where-Object { $_.id -eq $userId }
    if (-not $deletedUser) {
        Write-Host "   Deletion synced to DeviceMicroservice" -ForegroundColor Green
    } else {
        Write-Host "   User still exists in DeviceMicroservice" -ForegroundColor Red
    }
} catch {
    Write-Host "   Error verifying deletion: $_" -ForegroundColor Red
}

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
