#!/bin/bash

echo "🚀 Starting development environment..."
echo ""

# Start the containers
docker compose -f docker-compose.dev.yml up -d

# Wait for the services to start
echo "⏳ Waiting for services to be ready..."
sleep 8

# Show status
echo ""
echo "✅ Services started:"
docker compose -f docker-compose.dev.yml ps

echo ""
echo "📋 To view logs in real-time, run:"
echo "   docker compose -f docker-compose.dev.yml logs -f"
echo ""
echo "🌐 Application available at: http://localhost:8080"
echo "📊 Database available at: localhost:5432"
echo ""
