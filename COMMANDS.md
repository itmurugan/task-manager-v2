# Task Manager V2 - Command Reference

This document provides comprehensive commands for building, testing, running, and managing the Task Manager V2 application.

## 📋 Table of Contents

- [Prerequisites](#prerequisites)
- [Backend Commands](#backend-commands)
- [Frontend Commands](#frontend-commands)
- [Docker Commands](#docker-commands)
- [Testing Commands](#testing-commands)
- [Development Commands](#development-commands)
- [Production Commands](#production-commands)
- [Troubleshooting Commands](#troubleshooting-commands)

## 🔧 Prerequisites

Ensure you have the following installed:
```bash
# Check Java version (requires Java 21)
java -version

# Check Node.js version (requires Node 18+)
node --version && npm --version

# Check Docker version
docker --version && docker compose version

# Check Git version
git --version
```

## 🔙 Backend Commands

### Development
```bash
# Navigate to backend directory
cd backend

# Compile the application
./mvnw clean compile

# Run the application in development mode
./mvnw spring-boot:run

# Run with specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run with debug mode enabled
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"

# Package the application (creates JAR)
./mvnw clean package

# Package without running tests
./mvnw clean package -DskipTests

# Run the packaged JAR
java -jar target/task-manager-api-*.jar
```

### Testing
```bash
# Run all tests
./mvnw test

# Run tests with coverage report
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=TaskServiceTest

# Run specific test method
./mvnw test -Dtest=TaskServiceTest#createTask_WithValidRequest_ShouldReturnCreatedTask

# Run only unit tests (exclude integration tests)
./mvnw test -Dtest=*Test

# Run only integration tests
./mvnw test -Dtest=*IntegrationTest

# Generate test report and open coverage
./mvnw test jacoco:report && open target/site/jacoco/index.html
```

### Security & Quality
```bash
# Run security vulnerability scan
./mvnw org.owasp:dependency-check-maven:check

# View security report
open target/dependency-check-report.html

# Check for dependency updates
./mvnw versions:display-dependency-updates

# Update dependencies
./mvnw versions:use-latest-releases
```

### Database
```bash
# Access H2 console (when app is running)
# URL: http://localhost:8080/api/h2-console
# JDBC URL: jdbc:h2:mem:taskdb
# Username: sa
# Password: password
```

## 🖥️ Frontend Commands

### Development
```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Install specific dependency
npm install package-name

# Install dev dependency
npm install --save-dev package-name

# Start development server
npm start

# Start with specific port
PORT=3000 npm start

# Serve files using http-server
npx http-server -p 8081 -c-1 --cors
```

### Testing
```bash
# Run all tests
npm test

# Run tests with coverage
npm test -- --coverage

# Run tests in watch mode
npm run test:watch

# Run tests with verbose output
npm test -- --verbose

# Run specific test file
npm test -- tests/app.test.js

# Run tests matching pattern
npm test -- --testNamePattern="validation"
```

### Code Quality
```bash
# Run ESLint
npm run lint

# Fix ESLint issues automatically
npm run lint -- --fix

# Check linting without fixing
npm run lint:check

# Validate all (lint + test)
npm run validate
```

## 🐳 Docker Commands

### Single Service Commands
```bash
# Build backend image
docker build -t taskmanager-backend ./backend

# Build frontend image
docker build -t taskmanager-frontend ./frontend

# Run backend container
docker run -p 8080:8080 taskmanager-backend

# Run frontend container
docker run -p 8081:8081 taskmanager-frontend

# Run with environment variables
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker taskmanager-backend
```

### Docker Compose Commands
```bash
# Start all services (build if needed)
docker compose up --build

# Start in background/detached mode
docker compose up -d --build

# Start specific service
docker compose up backend
docker compose up frontend

# View logs
docker compose logs

# View logs for specific service
docker compose logs backend
docker compose logs frontend

# Follow logs in real-time
docker compose logs -f

# Stop all services
docker compose down

# Stop and remove volumes
docker compose down -v

# Restart services
docker compose restart

# Scale services
docker compose up -d --scale backend=2

# Execute command in running container
docker compose exec backend sh
docker compose exec frontend sh
```

### Docker Management
```bash
# List containers
docker ps

# List all containers (including stopped)
docker ps -a

# Remove stopped containers
docker container prune

# Remove unused images
docker image prune

# Remove all unused resources
docker system prune

# View container logs
docker logs <container_id>

# Enter container shell
docker exec -it <container_id> sh
```

## 🧪 Testing Commands

### Full Test Suite
```bash
# Run all backend tests
cd backend && ./mvnw test

# Run all frontend tests
cd frontend && npm test

# Run both with parallel execution
(cd backend && ./mvnw test) & (cd frontend && npm test) & wait
```

### Integration Testing
```bash
# Start services for integration testing
docker compose up -d

# Wait for services to be ready and test endpoints
sleep 30
curl -f http://localhost:8080/api/actuator/health
curl -f http://localhost:8081/health

# Run integration tests
cd backend && ./mvnw test -Dtest=*IntegrationTest

# Cleanup after testing
docker compose down -v
```

### Performance Testing
```bash
# Simple load test using curl
for i in {1..100}; do curl -s http://localhost:8080/api/tasks > /dev/null & done; wait

# Test with Apache Bench (if installed)
ab -n 100 -c 10 http://localhost:8080/api/tasks

# Monitor resource usage
docker stats
```

## 🔨 Development Commands

### Setup New Environment
```bash
# Clone repository
git clone <repository-url>
cd task-manager-v2

# Setup backend
cd backend
./mvnw dependency:resolve
./mvnw compile

# Setup frontend
cd ../frontend
npm install

# Verify setup
cd ../backend && ./mvnw test
cd ../frontend && npm test
```

### Code Generation & Scaffolding
```bash
# Generate new Spring Boot controller
# (Manual creation following existing patterns)

# Add new Maven dependency
./mvnw dependency:tree
./mvnw dependency:analyze

# Update Maven wrapper
./mvnw wrapper:wrapper

# Generate project reports
./mvnw site
```

### Database Operations
```bash
# Generate JPA DDL scripts
./mvnw hibernate:ddl -Dhibernate.hbm2ddl.auto=create

# Validate JPA mapping
./mvnw hibernate:validate

# View datasource info
./mvnw spring-boot:run -Dspring.datasource.url=jdbc:h2:file:./data/taskdb
```

## 🚀 Production Commands

### Build for Production
```bash
# Build optimized backend JAR
cd backend
./mvnw clean package -Pproduction

# Build production Docker images
docker build -t taskmanager-backend:prod ./backend
docker build -t taskmanager-frontend:prod ./frontend

# Build with specific tag
docker build -t taskmanager-backend:v2.0.0 ./backend
```

### Deployment
```bash
# Deploy with production profile
docker compose --profile production up -d

# Deploy to specific environment
ENVIRONMENT=staging docker compose up -d

# Rolling update (zero downtime)
docker compose up -d --no-deps backend
docker compose up -d --no-deps frontend

# Health check after deployment
curl -f http://localhost:8080/api/actuator/health
curl -f http://localhost:8081/health
```

### Monitoring
```bash
# View application metrics
curl http://localhost:8080/api/actuator/metrics

# View specific metric
curl http://localhost:8080/api/actuator/metrics/http.server.requests

# Monitor container resources
docker compose top
docker stats $(docker compose ps -q)

# Export metrics
curl -s http://localhost:8080/api/actuator/metrics | jq '.'
```

## 🔧 Troubleshooting Commands

### Diagnostics
```bash
# Check port usage
lsof -i :8080
lsof -i :8081

# Kill processes on specific ports
pkill -f "TaskManagerApiApplication"
pkill -f "http-server"

# Check Java processes
jps -v

# Check system resources
free -h
df -h
```

### Logs and Debugging
```bash
# View application logs
tail -f backend/logs/application.log

# Debug Spring Boot application
./mvnw spring-boot:run -Dlogging.level.com.taskmanager=DEBUG

# Debug Docker container
docker logs --tail 50 -f <container_name>

# Enter container for debugging
docker exec -it <container_name> sh

# Check container filesystem
docker exec <container_name> ls -la /app
```

### Network Issues
```bash
# Test connectivity
ping localhost
telnet localhost 8080
curl -v http://localhost:8080/api/actuator/health

# Check Docker networking
docker network ls
docker network inspect task-manager-v2_taskmanager-network

# Reset Docker networking
docker compose down
docker network prune
docker compose up
```

### Performance Issues
```bash
# Java memory analysis
./mvnw spring-boot:run -Xmx512m -XX:+PrintGCDetails

# Container resource limits
docker run --memory=512m --cpus=0.5 taskmanager-backend

# Monitor real-time performance
htop
iostat 1
```

### Clean Reset
```bash
# Complete cleanup and restart
docker compose down -v
docker system prune -f
docker volume prune -f

# Clean backend
cd backend
./mvnw clean
rm -rf target/

# Clean frontend
cd frontend
rm -rf node_modules/
rm -rf coverage/
npm install

# Restart everything
docker compose up --build
```

## 📚 Quick Reference

### Most Common Commands
```bash
# Start development
cd backend && ./mvnw spring-boot:run
cd frontend && npm start

# Run tests
cd backend && ./mvnw test
cd frontend && npm test

# Docker quick start
docker compose up --build

# Full cleanup and restart
docker compose down -v && docker compose up --build
```

### Environment Variables
```bash
# Backend
export SPRING_PROFILES_ACTIVE=dev
export SERVER_PORT=8080
export SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb

# Frontend
export PORT=8081
export REACT_APP_API_URL=http://localhost:8080

# Docker
export COMPOSE_PROJECT_NAME=taskmanager
export DOCKER_BUILDKIT=1
```

### Useful Aliases
Add these to your `.bashrc` or `.zshrc`:
```bash
alias tmb="cd ~/task-manager-v2/backend"
alias tmf="cd ~/task-manager-v2/frontend"
alias tm="cd ~/task-manager-v2"
alias tmd="cd ~/task-manager-v2 && docker compose"
alias tmtest="cd ~/task-manager-v2/backend && ./mvnw test && cd ../frontend && npm test"
alias tmstart="docker compose up --build"
alias tmstop="docker compose down"
alias tmclean="docker compose down -v && docker system prune -f"
```

---

**Note:** Replace `./mvnw` with `mvnw.cmd` on Windows systems, and ensure all commands are run from the appropriate directory context.