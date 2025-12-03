# ============================================
# ParkNexus Run Script (Windows)
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   ParkNexus - Starting Application     ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

function Print-Success {
    param($Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Print-Info {
    param($Message)
    Write-Host "ℹ $Message" -ForegroundColor Blue
}

function Print-Warning {
    param($Message)
    Write-Host "⚠ $Message" -ForegroundColor Yellow
}

function Print-Error {
    param($Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

# Check if .env exists
if (-not (Test-Path ".env")) {
    Print-Warning ".env file not found"
    Print-Info "Running setup script first..."
    & ".\scripts\setup.ps1"
}

# Check if Docker is running
try {
    docker info 2>$null | Out-Null
    Print-Success "Docker is running"
}
catch {
    Print-Error "Docker is not running"
    Print-Info "Please start Docker Desktop and try again"
    exit 1
}

Write-Host ""

# Build and start containers
Print-Info "Building and starting containers..."
docker compose up --build -d

# Wait for services to be ready
Print-Info "Waiting for services to start..."
Start-Sleep -Seconds 10

# Show status
Write-Host ""
Print-Success "Services started successfully!"
docker compose ps

Write-Host ""
Write-Host "╔═══════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║          ParkNexus URLs                   ║" -ForegroundColor Cyan
Write-Host "╠═══════════════════════════════════════════╣" -ForegroundColor Cyan
Write-Host "║  Frontend:   http://localhost:80         ║" -ForegroundColor Cyan
Write-Host "║  Backend:    http://localhost:8080       ║" -ForegroundColor Cyan
Write-Host "║  Prometheus: http://localhost:9090       ║" -ForegroundColor Cyan
Write-Host "║  Grafana:    http://localhost:3000       ║" -ForegroundColor Cyan
Write-Host "╚═══════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

Print-Info "To view logs: docker compose logs -f"
Print-Info "To stop:      docker compose down"
Write-Host ""

