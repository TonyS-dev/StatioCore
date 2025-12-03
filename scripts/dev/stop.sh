#!/bin/bash

echo "🛑 Stopping development environment..."
echo ""

# Stop and remove containers
docker compose -f docker-compose.dev.yml down

echo ""
echo "✅ Services stopped successfully"
echo ""
echo "💡 To also remove volumes (database), use:"
echo "   docker compose -f docker-compose.dev.yml down -v"
echo ""
