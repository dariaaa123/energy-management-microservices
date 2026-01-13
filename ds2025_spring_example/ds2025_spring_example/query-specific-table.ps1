# Script to query a specific table from a specific database
# Usage: .\query-specific-table.ps1 -Database "example-db" -Table "users"

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("example-db", "device-db", "monitoring-db", "auth-db")]
    [string]$Database,
    
    [Parameter(Mandatory=$true)]
    [string]$Table,
    
    [Parameter(Mandatory=$false)]
    [string]$Where = "",
    
    [Parameter(Mandatory=$false)]
    [int]$Limit = 0
)

$query = "SELECT * FROM $Table"

if ($Where -ne "") {
    $query += " WHERE $Where"
}

if ($Limit -gt 0) {
    $query += " LIMIT $Limit"
}

$query += ";"

Write-Host "Querying: $Database.$Table" -ForegroundColor Cyan
Write-Host "Query: $query" -ForegroundColor Gray
Write-Host ""

docker exec ds2025-postgres psql -U postgres -d $Database -c $query
