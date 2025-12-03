#!/bin/bash

echo "📋 Showing Logs for development environment..."
echo "   (Press Ctrl+C to exit)"
echo ""

docker compose -f docker-compose.dev.yml logs -f --tail=100
