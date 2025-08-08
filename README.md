# Task Manager V2

A comprehensive, modern task management application built with Spring Boot backend and vanilla JavaScript frontend.

## 🚀 Features

### Core Functionality
- ✅ Create, read, update, and delete tasks
- ✅ Mark tasks as complete/incomplete
- ✅ Search tasks by title
- ✅ Filter tasks (All, Active, Completed)
- ✅ Real-time task statistics
- ✅ Rich, modern UI with responsive design

### Technical Features
- 🏗️ **Modular Architecture**: Separate frontend and backend applications
- 🔒 **Security**: CORS configuration, input validation, SQL injection prevention
- 📊 **API Documentation**: Interactive Swagger/OpenAPI documentation
- 🧪 **Comprehensive Testing**: Unit tests, integration tests, and frontend tests
- 🐳 **Docker Support**: Containerized deployment with Docker Compose
- 🚀 **CI/CD Pipeline**: Automated builds, tests, and security scanning
- 📈 **Monitoring**: Health checks and application metrics

## 🏛️ Architecture

```
task-manager-v2/
├── backend/                 # Spring Boot API
│   ├── src/main/java/      # Java source code
│   ├── src/test/java/      # Backend tests
│   ├── Dockerfile          # Backend container
│   └── pom.xml             # Maven dependencies
├── frontend/               # Vanilla JS Frontend
│   ├── index.html          # Main HTML page
│   ├── styles.css          # Modern CSS styling
│   ├── app.js              # JavaScript application
│   ├── tests/              # Frontend tests
│   └── Dockerfile          # Frontend container
├── docker-compose.yml      # Multi-service orchestration
└── .github/workflows/      # CI/CD pipeline
```

## 🛠️ Technology Stack

### Backend
- **Java 21** - Modern LTS Java version
- **Spring Boot 3.5.4** - Application framework
- **Spring Data JPA** - Database abstraction
- **H2 Database** - In-memory database
- **Spring Security** - Security configuration
- **OpenAPI/Swagger** - API documentation
- **Maven** - Build tool
- **JaCoCo** - Test coverage
- **OWASP Dependency Check** - Security scanning

### Frontend
- **HTML5** - Semantic markup
- **CSS3** - Modern styling with CSS Grid/Flexbox
- **Vanilla JavaScript** - No framework dependencies
- **Font Awesome** - Icon library
- **Google Fonts** - Typography

### DevOps & Testing
- **Docker** - Containerization
- **Docker Compose** - Multi-service orchestration
- **GitHub Actions** - CI/CD pipeline
- **JUnit 5** - Backend testing
- **Jest** - Frontend testing
- **ESLint** - JavaScript linting
- **Nginx** - Frontend web server

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Node.js 18 or higher (for frontend development)
- Docker and Docker Compose (for containerized deployment)
- Git

### Local Development

#### Backend
```bash
cd backend
./mvnw spring-boot:run
```

The backend API will be available at `http://localhost:8080`
- API Documentation: `http://localhost:8080/api/swagger-ui.html`
- H2 Console: `http://localhost:8080/api/h2-console`

#### Frontend
```bash
cd frontend
npm install
npm start
```

The frontend will be available at `http://localhost:8081`

### Docker Deployment

#### Full Application
```bash
# Build and start all services
docker-compose up --build

# Background mode
docker-compose up -d --build
```

#### Individual Services
```bash
# Backend only
docker-compose up backend

# Frontend only
docker-compose up frontend
```

### Access Points
- **Frontend**: http://localhost:8081
- **Backend API**: http://localhost:8080/api
- **API Documentation**: http://localhost:8080/api/swagger-ui.html
- **Database Console**: http://localhost:8080/api/h2-console

## 🧪 Testing

### Backend Tests
```bash
cd backend

# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Frontend Tests
```bash
cd frontend

# Run tests
npm test

# Run with coverage
npm test -- --coverage

# Watch mode
npm run test:watch
```

### Integration Tests
```bash
# Test full application with Docker
docker-compose up --build
# Run integration tests (included in CI pipeline)
```

## 📊 API Endpoints

### Tasks
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks` - Create new task
- `PUT /api/tasks/{id}` - Update task
- `PUT /api/tasks/{id}/complete` - Mark as completed
- `PUT /api/tasks/{id}/incomplete` - Mark as incomplete
- `DELETE /api/tasks/{id}` - Delete task

### Filtering & Search
- `GET /api/tasks/completed` - Get completed tasks
- `GET /api/tasks/incomplete` - Get incomplete tasks
- `GET /api/tasks/search?title={query}` - Search by title

### Statistics
- `GET /api/tasks/statistics` - Get task statistics

### System
- `GET /api/actuator/health` - Health check
- `GET /api/actuator/metrics` - Application metrics

## 🔒 Security Features

### Backend Security
- CORS configuration for cross-origin requests
- Input validation with Bean Validation
- SQL injection prevention with JPA
- Security headers configuration
- Non-root Docker user
- Dependency vulnerability scanning

### Frontend Security
- XSS prevention with HTML escaping
- Content Security Policy headers
- Secure HTTP headers via Nginx
- Input sanitization and validation

## 🚀 CI/CD Pipeline

The project includes a comprehensive GitHub Actions workflow:

### Automated Checks
- ✅ Backend unit and integration tests
- ✅ Frontend tests and linting
- ✅ Security vulnerability scanning
- ✅ Code quality analysis with SonarCloud
- ✅ Docker image building and testing

### Security Scanning
- OWASP Dependency Check for backend
- Trivy filesystem scanning
- Container security scanning
- Code quality and security analysis

### Deployment Pipeline
- Automated staging deployment
- Smoke tests
- Docker image publishing to GitHub Container Registry

## 🏗️ Development Guidelines

### Code Style
- **Backend**: Java Code Conventions, Spring Boot best practices
- **Frontend**: JavaScript Standard Style, modern ES6+ features
- **Testing**: Comprehensive test coverage (>80% target)

### Git Workflow
- Feature branches for new development
- Pull requests with required reviews
- Automated CI checks before merge
- Semantic commit messages

### Security
- Regular dependency updates
- Vulnerability scanning in CI
- Security-first development approach
- Regular security audits

## 📈 Monitoring & Observability

### Health Checks
- Application health endpoints
- Database connectivity checks
- Docker container health checks

### Metrics
- Application performance metrics
- JVM metrics (for backend)
- Request/response metrics
- Error tracking

### Logging
- Structured logging with correlation IDs
- Log levels configured per environment
- Centralized log aggregation ready

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Development Setup
```bash
# Clone repository
git clone https://github.com/your-username/task-manager-v2.git
cd task-manager-v2

# Install backend dependencies
cd backend && ./mvnw dependency:resolve

# Install frontend dependencies
cd ../frontend && npm install

# Run tests
cd ../backend && ./mvnw test
cd ../frontend && npm test
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot community for excellent documentation
- Font Awesome for beautiful icons
- All contributors and testers

## 📞 Support

For support and questions:
- Create an issue in the GitHub repository
- Check the [API documentation](http://localhost:8080/api/swagger-ui.html)
- Review the comprehensive test suites for usage examples

---

**Task Manager V2** - Built with ❤️ using modern web technologies