# ============================================
# ParkNexus Setup Script (Windows)
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   ParkNexus Setup - Windows            ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Function to print colored messages
function Print-Success {
    param($Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Print-Error {
    param($Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Print-Info {
    param($Message)
    Write-Host "ℹ $Message" -ForegroundColor Blue
}

function Print-Warning {
    param($Message)
    Write-Host "⚠ $Message" -ForegroundColor Yellow
}

# Check if Docker is installed
function Test-Docker {
    try {
        $version = docker --version 2>$null
        if ($version) {
            Print-Success "Docker is installed ($version)"
            return $true
        }
    }
    catch {
        Print-Warning "Docker is not installed"
        return $false
    }
    return $false
}

# Check if Docker Compose is installed
function Test-DockerCompose {
    try {
        $version = docker compose version 2>$null
        if ($version) {
            Print-Success "Docker Compose is installed ($version)"
            return $true
        }
    }
    catch {
        Print-Warning "Docker Compose is not installed"
        return $false
    }
    return $false
}

# Prompt to install Docker
function Install-DockerPrompt {
    Write-Host ""
    $response = Read-Host "Do you want to install Docker Desktop? (y/n)"

    if ($response -eq 'y' -or $response -eq 'Y') {
        Print-Info "Opening Docker Desktop download page..."
        Start-Process "https://www.docker.com/products/docker-desktop"
        Print-Info "Please install Docker Desktop and re-run this script"
        exit 0
    }
    else {
        Print-Error "Docker is required to run ParkNexus"
        Print-Info "Please install Docker manually and re-run this script"
        exit 1
    }
}

# Create .env file from .env.example
function Setup-EnvFile {
    if (Test-Path ".env") {
        Print-Warning ".env file already exists"
        $response = Read-Host "Do you want to overwrite it with .env.example? (y/n)"

        if ($response -ne 'y' -and $response -ne 'Y') {
            Print-Info "Keeping existing .env file"
            return
        }
    }

    if (-not (Test-Path ".env.example")) {
        Print-Error ".env.example not found"
        exit 1
    }

    Copy-Item ".env.example" ".env"
    Print-Success ".env file created from .env.example"
    Print-Warning "Please edit .env and set secure values for DB_PASSWORD and JWT_SECRET"
}

# Main setup flow
function Main {
    Print-Info "Detected OS: Windows"
    Write-Host ""

    # Check Docker
    if (-not (Test-Docker)) {
        Install-DockerPrompt
    }

    Write-Host ""

    # Check Docker Compose
    if (-not (Test-DockerCompose)) {
        Print-Error "Docker Compose not found"
        Print-Info "Docker Compose should be included with Docker Desktop"
        exit 1
    }

    Write-Host ""

    # Setup .env
    Setup-EnvFile

    Write-Host ""
    Print-Success "Setup completed successfully!"
    Write-Host ""
    Print-Info "Next steps:"
    Write-Host "  1. Edit .env and set secure passwords"
    Write-Host "  2. Run: .\scripts\run.ps1"
    Write-Host ""
}

# Run main function
Main

