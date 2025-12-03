#!/bin/bash

# ============================================
# ParkNexus Setup Script (Linux/macOS)
# ============================================

set -e  # Exit on error

echo "╔════════════════════════════════════════╗"
echo "║   ParkNexus Setup - Linux/macOS        ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Detect OS
detect_os() {
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="linux"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="macos"
    else
        print_error "Unsupported OS: $OSTYPE"
        exit 1
    fi
    print_info "Detected OS: $OS"
}

# Check if Docker is installed
check_docker() {
    if command -v docker &> /dev/null; then
        DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
        print_success "Docker is installed (version $DOCKER_VERSION)"
        return 0
    else
        print_warning "Docker is not installed"
        return 1
    fi
}

# Check if Docker Compose is installed
check_docker_compose() {
    if docker compose version &> /dev/null; then
        COMPOSE_VERSION=$(docker compose version --short)
        print_success "Docker Compose is installed (version $COMPOSE_VERSION)"
        return 0
    elif command -v docker-compose &> /dev/null; then
        COMPOSE_VERSION=$(docker-compose --version | awk '{print $4}' | sed 's/,//')
        print_success "Docker Compose is installed (version $COMPOSE_VERSION)"
        return 0
    else
        print_warning "Docker Compose is not installed"
        return 1
    fi
}

# Install Docker (Linux)
install_docker_linux() {
    print_info "Installing Docker on Linux..."

    # Detect Linux distribution
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        DISTRO=$ID
    else
        print_error "Cannot detect Linux distribution"
        exit 1
    fi

    case $DISTRO in
        ubuntu|debian)
            print_info "Detected Debian/Ubuntu"
            sudo apt-get update
            sudo apt-get install -y ca-certificates curl gnupg
            sudo install -m 0755 -d /etc/apt/keyrings
            curl -fsSL https://download.docker.com/linux/$DISTRO/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
            sudo chmod a+r /etc/apt/keyrings/docker.gpg
            echo \
              "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/$DISTRO \
              $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
              sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
            sudo apt-get update
            sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
            ;;
        fedora|rhel|centos)
            print_info "Detected Fedora/RHEL/CentOS"
            sudo dnf -y install dnf-plugins-core
            sudo dnf config-manager --add-repo https://download.docker.com/linux/fedora/docker-ce.repo
            sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
            ;;
        arch|manjaro)
            print_info "Detected Arch Linux"
            sudo pacman -Sy --noconfirm docker docker-compose
            ;;
        *)
            print_error "Unsupported Linux distribution: $DISTRO"
            print_info "Please install Docker manually from https://docs.docker.com/engine/install/"
            exit 1
            ;;
    esac

    # Start Docker service
    sudo systemctl start docker
    sudo systemctl enable docker

    # Add current user to docker group
    sudo usermod -aG docker $USER

    print_success "Docker installed successfully"
    print_warning "You may need to log out and back in for group changes to take effect"
}

# Install Docker (macOS)
install_docker_macos() {
    print_info "To install Docker on macOS:"
    print_info "1. Download Docker Desktop from: https://www.docker.com/products/docker-desktop"
    print_info "2. Install the .dmg file"
    print_info "3. Run Docker Desktop"
    print_info "4. Re-run this script after installation"
    exit 0
}

# Prompt user to install Docker
prompt_install_docker() {
    echo ""
    read -p "Do you want to install Docker now? (y/n): " -n 1 -r
    echo ""

    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if [ "$OS" == "linux" ]; then
            install_docker_linux
        elif [ "$OS" == "macos" ]; then
            install_docker_macos
        fi
    else
        print_error "Docker is required to run ParkNexus"
        print_info "Please install Docker manually and re-run this script"
        exit 1
    fi
}

# Create .env file from .env.example
setup_env_file() {
    if [ -f ".env" ]; then
        print_warning ".env file already exists"
        read -p "Do you want to overwrite it with .env.example? (y/n): " -n 1 -r
        echo ""

        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            print_info "Keeping existing .env file"
            return 0
        fi
    fi

    if [ ! -f ".env.example" ]; then
        print_error ".env.example not found"
        exit 1
    fi

    cp .env.example .env
    print_success ".env file created from .env.example"
    print_warning "Please edit .env and set secure values for DB_PASSWORD and JWT_SECRET"
}

# Main setup flow
main() {
    detect_os
    echo ""

    # Check Docker
    if ! check_docker; then
        prompt_install_docker
    fi

    echo ""

    # Check Docker Compose
    if ! check_docker_compose; then
        print_error "Docker Compose not found"
        print_info "Docker Compose should be included with Docker Desktop"
        exit 1
    fi

    echo ""

    # Setup .env
    setup_env_file

    echo ""
    print_success "Setup completed successfully!"
    echo ""
    print_info "Next steps:"
    echo "  1. Edit .env and set secure passwords"
    echo "  2. Run: ./scripts/run.sh"
    echo ""
}

# Run main function
main

