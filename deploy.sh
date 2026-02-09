#!/bin/bash

# Vendor Compliance System - Deployment Script
# Usage: ./deploy.sh [environment]
# Examples: ./deploy.sh local | ./deploy.sh docker | ./deploy.sh render

set -e

ENVIRONMENT=${1:-docker}
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "======================================"
echo "Vendor Compliance System Deployment"
echo "======================================"
echo "Environment: $ENVIRONMENT"
echo ""

# Load environment variables
if [ -f "$PROJECT_DIR/.env" ]; then
    export $(cat "$PROJECT_DIR/.env" | grep -v '^#' | xargs)
    echo "✓ Loaded .env file"
else
    echo "⚠ Warning: .env file not found"
    echo "  Please create .env from .env.example"
fi

case $ENVIRONMENT in
  local)
    echo ""
    echo "Starting local development..."
    echo ""
    
    # Check Java
    if ! command -v java &> /dev/null; then
        echo "✗ Java 17+ is required"
        exit 1
    fi
    echo "✓ Java found: $(java -version 2>&1 | head -n 1)"
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        echo "✗ Maven is required"
        exit 1
    fi
    echo "✓ Maven found"
    
    # Build
    echo ""
    echo "Building project..."
    mvn clean package -DskipTests
    
    # Run
    echo ""
    echo "Starting application..."
    mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
    ;;

  docker)
    echo ""
    echo "Starting Docker deployment..."
    echo ""
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        echo "✗ Docker is required"
        exit 1
    fi
    echo "✓ Docker found: $(docker --version)"
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        echo "✗ Docker Compose is required"
        exit 1
    fi
    echo "✓ Docker Compose found: $(docker-compose --version)"
    
    # Build and start
    echo ""
    echo "Building Docker image..."
    docker-compose build
    
    echo ""
    echo "Starting containers..."
    docker-compose up -d
    
    echo ""
    echo "✓ Deployment complete!"
    echo ""
    echo "Services running:"
    docker-compose ps
    echo ""
    echo "Backend: http://localhost:8080"
    echo "Database: localhost:5432"
    echo ""
    echo "View logs:"
    echo "  docker-compose logs -f app"
    ;;

  render)
    echo ""
    echo "Preparing for Render deployment..."
    echo ""
    
    if ! command -v git &> /dev/null; then
        echo "✗ Git is required"
        exit 1
    fi
    echo "✓ Git found"
    
    # Check for git remote
    if ! git remote get-url origin &> /dev/null; then
        echo "✗ No git remote found"
        echo "  Run: git remote add origin <your-repo-url>"
        exit 1
    fi
    echo "✓ Git remote configured: $(git remote get-url origin)"
    
    # Build
    echo ""
    echo "Building project..."
    mvn clean package -DskipTests
    
    # Push to git
    echo ""
    echo "Pushing to GitHub..."
    git add .
    git commit -m "Deploy to Render" --allow-empty
    git push origin main
    
    echo ""
    echo "✓ Code pushed to GitHub!"
    echo ""
    echo "Next steps:"
    echo "1. Go to https://render.com"
    echo "2. Create new Web Service"
    echo "3. Connect your GitHub repository"
    echo "4. Set environment variables:"
    echo "   - SPRING_DATASOURCE_URL"
    echo "   - SPRING_DATASOURCE_USERNAME"
    echo "   - SPRING_DATASOURCE_PASSWORD"
    echo "   - JWT_SECRET"
    echo "5. Deploy!"
    ;;

  aws)
    echo ""
    echo "Preparing for AWS EC2 deployment..."
    echo ""
    
    if ! command -v aws &> /dev/null; then
        echo "⚠ AWS CLI not found"
        echo "  Install from: https://aws.amazon.com/cli/"
        echo "  Or manually deploy to EC2"
    fi
    
    # Build
    echo ""
    echo "Building project..."
    mvn clean package -DskipTests
    
    echo ""
    echo "✓ Build complete!"
    echo ""
    echo "To deploy to EC2:"
    echo ""
    echo "1. SSH into your instance:"
    echo "   ssh -i your-key.pem ec2-user@your-instance-ip"
    echo ""
    echo "2. Create systemd service:"
    echo "   sudo nano /etc/systemd/system/vendor-compliance.service"
    echo ""
    echo "3. Copy the following and populate environment variables:"
    cat << 'EOF'

[Unit]
Description=Vendor Compliance Backend
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/app
ExecStart=/usr/bin/java -jar /opt/app/app.jar
Restart=on-failure
RestartSec=10

Environment="SPRING_DATASOURCE_URL=your_db_url"
Environment="SPRING_DATASOURCE_USERNAME=postgres"
Environment="SPRING_DATASOURCE_PASSWORD=your_password"
Environment="JWT_SECRET=your_jwt_secret"
Environment="SPRING_PROFILES_ACTIVE=prod"

[Install]
WantedBy=multi-user.target

EOF

    echo ""
    echo "4. Enable and start service:"
    echo "   sudo systemctl daemon-reload"
    echo "   sudo systemctl enable vendor-compliance"
    echo "   sudo systemctl start vendor-compliance"
    ;;

  *)
    echo "Unknown environment: $ENVIRONMENT"
    echo ""
    echo "Usage: ./deploy.sh [environment]"
    echo "Environments: local, docker, render, aws"
    exit 1
    ;;
esac

echo ""
echo "======================================"
