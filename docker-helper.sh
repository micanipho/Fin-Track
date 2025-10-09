#!/bin/bash

# FinTrack Docker Management Script
# This script provides easy commands to manage the FinTrack Docker environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if .env file exists, if not copy from example
check_env_file() {
    if [ ! -f .env ]; then
        print_warning ".env file not found. Creating from .env.example..."
        cp .env.example .env
        print_status "Please update .env file with your actual values before continuing."
        print_status "Edit the .env file with: nano .env"
        exit 1
    fi
}

# Build the application
build() {
    print_status "Building FinTrack application..."
    docker-compose build --no-cache
    print_success "Build completed successfully!"
}

# Start services
start() {
    check_env_file
    print_status "Starting FinTrack services..."
    docker-compose up -d
    print_success "Services started successfully!"
    print_status "Application will be available at: http://localhost:8080"
    print_status "Swagger UI will be available at: http://localhost:8080/swagger-ui.html"
    print_status "API docs will be available at: http://localhost:8080/api-docs"
}

# Stop services
stop() {
    print_status "Stopping FinTrack services..."
    docker-compose down
    print_success "Services stopped successfully!"
}

# Restart services
restart() {
    print_status "Restarting FinTrack services..."
    docker-compose down
    docker-compose up -d
    print_success "Services restarted successfully!"
}

# View logs
logs() {
    if [ -z "$1" ]; then
        print_status "Showing logs for all services..."
        docker-compose logs -f
    else
        print_status "Showing logs for service: $1"
        docker-compose logs -f "$1"
    fi
}

# Clean up everything
clean() {
    print_warning "This will remove all containers, volumes, and images. Are you sure? (y/N)"
    read -r response
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        print_status "Cleaning up Docker environment..."
        docker-compose down -v --remove-orphans
        docker system prune -f
        print_success "Cleanup completed!"
    else
        print_status "Cleanup cancelled."
    fi
}

# Show status
status() {
    print_status "FinTrack Docker Services Status:"
    docker-compose ps
}

# Show help
help() {
    echo "FinTrack Docker Management Script"
    echo ""
    echo "Usage: ./docker-helper.sh [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  build     Build the FinTrack application"
    echo "  start     Start all services"
    echo "  stop      Stop all services"
    echo "  restart   Restart all services"
    echo "  logs      Show logs (optional: specify service name)"
    echo "  status    Show status of all services"
    echo "  clean     Clean up all Docker resources"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./docker-helper.sh start"
    echo "  ./docker-helper.sh logs fintrack-app"
    echo "  ./docker-helper.sh logs postgres"
}

# Main script logic
case "$1" in
    build)
        build
        ;;
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    logs)
        logs "$2"
        ;;
    status)
        status
        ;;
    clean)
        clean
        ;;
    help|--help|-h)
        help
        ;;
    *)
        print_error "Unknown command: $1"
        echo ""
        help
        exit 1
        ;;
esac
