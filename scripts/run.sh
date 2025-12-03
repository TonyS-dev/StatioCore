#!/bin/bash

# ============================================
# ParkNexus Run Script (Linux/macOS)
# ============================================

set -e

echo "╔════════════════════════════════════════╗"
echo "║   ParkNexus - Starting Application     ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

# Check if .env exists
if [ ! -f ".env" ]; then
    print_warning ".env file not found"
    print_info "Running setup script first..."
    ./scripts/setup.sh
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running"
    print_info "Please start Docker and try again"
    exit 1
fi

print_success "Docker is running"
echo ""

# Build and start containers
print_info "Building and starting containers..."
docker compose up --build -d

# Wait for services to be ready
print_info "Waiting for services to start..."
sleep 10

# Show status
echo ""
print_success "Services started successfully!"
docker compose ps

echo ""
echo "╔═══════════════════════════════════════════╗"
echo "║          ParkNexus URLs                   ║"
echo "╠═══════════════════════════════════════════╣"
echo "║  Frontend:   http://localhost:80         ║"
echo "║  Backend:    http://localhost:8080       ║"
echo "║  Prometheus: http://localhost:9090       ║"
echo "║  Grafana:    http://localhost:3000       ║"
echo "╚═══════════════════════════════════════════╝"
echo ""

print_info "To view logs: docker compose logs -f"
print_info "To stop:      docker compose down"
echo ""

