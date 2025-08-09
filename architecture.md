# Task Manager V2 - Architecture Diagrams

## 1. System Architecture Overview

```mermaid
graph TB
    subgraph "Client Layer"
        Browser[Web Browser<br/>HTML5/CSS3/JS]
    end
    
    subgraph "Frontend Service"
        Nginx[Nginx Web Server<br/>:8081]
        Static[Static Files<br/>index.html, app.js, styles.css]
        Nginx --> Static
    end
    
    subgraph "Backend Service"
        API[Spring Boot API<br/>:8080]
        subgraph "Application Layers"
            Controller[REST Controllers]
            Service[Business Services]
            Repository[JPA Repositories]
        end
        H2[(H2 Database<br/>In-Memory)]
    end
    
    subgraph "DevOps & Infrastructure"
        Docker[Docker Containers]
        GHA[GitHub Actions CI/CD]
    end
    
    Browser -->|HTTP/REST| Nginx
    Nginx -->|Proxy API calls| API
    API --> Controller
    Controller --> Service
    Service --> Repository
    Repository --> H2
    
    Docker -.->|Containerizes| Nginx
    Docker -.->|Containerizes| API
    GHA -.->|Builds & Tests| Docker
```

## 2. Backend Architecture - Layered Design

```mermaid
graph TD
    subgraph "Presentation Layer"
        TC[TaskController<br/>REST Endpoints]
        OAC[OpenAPI Config<br/>Swagger Documentation]
        GEH[GlobalExceptionHandler<br/>Error Handling]
    end
    
    subgraph "Business Layer"
        TS[TaskService<br/>Business Logic]
    end
    
    subgraph "Data Transfer Objects"
        TCR[TaskCreateRequest]
        TUR[TaskUpdateRequest]
        TD[TaskDTO]
    end
    
    subgraph "Persistence Layer"
        TR[TaskRepository<br/>JPA Interface]
        TE[Task Entity<br/>JPA Model]
    end
    
    subgraph "Configuration"
        SC[SecurityConfig<br/>CORS & Security]
        AP[application.properties<br/>App Configuration]
    end
    
    TC --> TS
    TC --> TCR
    TC --> TUR
    TS --> TD
    TS --> TR
    TR --> TE
    TE --> |Maps to| H2DB[(H2 Database)]
    
    SC -.->|Configures| TC
    OAC -.->|Documents| TC
    GEH -.->|Handles Errors| TC
```

## 3. Frontend Architecture - Component Structure

```mermaid
graph TD
    subgraph "User Interface"
        HTML[index.html<br/>Page Structure]
        CSS[styles.css<br/>Styling & Layout]
        JS[app.js<br/>Application Logic]
    end
    
    subgraph "JavaScript Modules"
        Init[Initialization<br/>DOMContentLoaded]
        API[API Service<br/>Fetch Wrapper]
        TaskMgr[Task Manager<br/>CRUD Operations]
        UI[UI Controller<br/>DOM Manipulation]
        Filter[Filter System<br/>Task Filtering]
        Search[Search System<br/>Title Search]
        Stats[Statistics<br/>Task Counters]
    end
    
    subgraph "UI Components"
        Header[Header<br/>Title & Stats]
        Form[Task Form<br/>Create/Edit]
        List[Task List<br/>Display Tasks]
        Controls[Filter Controls<br/>All/Active/Complete]
    end
    
    HTML --> JS
    CSS --> HTML
    JS --> Init
    Init --> API
    Init --> TaskMgr
    TaskMgr --> UI
    UI --> Header
    UI --> Form
    UI --> List
    UI --> Controls
    TaskMgr --> Filter
    TaskMgr --> Search
    TaskMgr --> Stats
```

## 4. Data Flow Diagram

```mermaid
sequenceDiagram
    participant U as User
    participant B as Browser
    participant N as Nginx
    participant API as Spring Boot API
    participant S as TaskService
    participant R as Repository
    participant DB as H2 Database
    
    U->>B: Create New Task
    B->>N: POST /api/tasks
    N->>API: Forward Request
    API->>API: Validate Input
    API->>S: createTask(request)
    S->>R: save(task)
    R->>DB: INSERT INTO tasks
    DB-->>R: Task ID
    R-->>S: Task Entity
    S-->>API: TaskDTO
    API-->>N: 201 Created
    N-->>B: JSON Response
    B-->>U: Display New Task
    
    U->>B: View All Tasks
    B->>N: GET /api/tasks
    N->>API: Forward Request
    API->>S: getAllTasks()
    S->>R: findAll()
    R->>DB: SELECT * FROM tasks
    DB-->>R: Task List
    R-->>S: List<Task>
    S-->>API: List<TaskDTO>
    API-->>N: 200 OK
    N-->>B: JSON Array
    B-->>U: Display Tasks
```

## 5. API Endpoints Structure

```mermaid
graph LR
    subgraph "Task Management APIs"
        Root[/api/tasks]
        
        Root --> GET_ALL[GET /<br/>Get all tasks]
        Root --> GET_ID[GET /{id}<br/>Get task by ID]
        Root --> POST[POST /<br/>Create task]
        Root --> PUT[PUT /{id}<br/>Update task]
        Root --> DELETE[DELETE /{id}<br/>Delete task]
        
        Root --> COMPLETE[PUT /{id}/complete<br/>Mark complete]
        Root --> INCOMPLETE[PUT /{id}/incomplete<br/>Mark incomplete]
        
        Root --> FILTER_COMP[GET /completed<br/>Get completed]
        Root --> FILTER_INCOMP[GET /incomplete<br/>Get incomplete]
        Root --> SEARCH[GET /search?title=<br/>Search by title]
        Root --> STATS[GET /statistics<br/>Get statistics]
    end
    
    subgraph "System APIs"
        Health[/api/actuator/health<br/>Health Check]
        Metrics[/api/actuator/metrics<br/>App Metrics]
        Swagger[/api/swagger-ui.html<br/>API Docs]
        H2[/api/h2-console<br/>Database Console]
    end
```

## 6. Database Schema

```mermaid
erDiagram
    TASKS {
        BIGINT id PK "Auto-generated ID"
        VARCHAR(100) title "Task title (required)"
        VARCHAR(500) description "Task description (optional)"
        BOOLEAN completed "Completion status (default: false)"
        TIMESTAMP created_at "Creation timestamp"
        TIMESTAMP updated_at "Last update timestamp"
    }
    
    TASKS ||--o{ TASKS : "Self-referential for future features"
```

## 7. CI/CD Pipeline Flow

```mermaid
graph TD
    subgraph "GitHub Repository"
        Code[Source Code]
        PR[Pull Request]
    end
    
    subgraph "GitHub Actions Workflow"
        Trigger[Workflow Triggered]
        
        subgraph "Backend Pipeline"
            BTest[Maven Test]
            BCoverage[JaCoCo Coverage]
            BSecurity[OWASP Dependency Check]
            BBuild[Build JAR]
            BDocker[Build Docker Image]
        end
        
        subgraph "Frontend Pipeline"
            FInstall[NPM Install]
            FLint[ESLint]
            FTest[Jest Tests]
            FCoverage[Test Coverage]
            FBuild[Build Static Files]
            FDocker[Build Docker Image]
        end
        
        subgraph "Integration"
            DockerCompose[Docker Compose Test]
            Trivy[Security Scan]
            SonarCloud[Code Quality]
        end
        
        subgraph "Deployment"
            Registry[GitHub Container Registry]
            Deploy[Deploy to Environment]
        end
    end
    
    Code --> PR
    PR --> Trigger
    Trigger --> BTest
    BTest --> BCoverage
    BCoverage --> BSecurity
    BSecurity --> BBuild
    BBuild --> BDocker
    
    Trigger --> FInstall
    FInstall --> FLint
    FLint --> FTest
    FTest --> FCoverage
    FCoverage --> FBuild
    FBuild --> FDocker
    
    BDocker --> DockerCompose
    FDocker --> DockerCompose
    DockerCompose --> Trivy
    Trivy --> SonarCloud
    SonarCloud --> Registry
    Registry --> Deploy
```

## 8. Docker Container Architecture

```mermaid
graph TD
    subgraph "Docker Host"
        subgraph "Docker Network: task-manager-network"
            subgraph "Frontend Container"
                FNginx[Nginx:alpine<br/>Port 8081]
                FStatic[Static Files<br/>/usr/share/nginx/html]
            end
            
            subgraph "Backend Container"
                BJava[OpenJDK 21<br/>Port 8080]
                BApp[Spring Boot App<br/>task-manager-api.jar]
                BH2[Embedded H2 DB]
            end
        end
        
        subgraph "Volumes"
            AppData[App Data Volume]
            Logs[Logs Volume]
        end
    end
    
    Host[Host Machine] -->|8081| FNginx
    Host -->|8080| BJava
    FNginx -->|API Proxy| BJava
    BApp --> BH2
    BH2 -.->|Persist| AppData
    BJava -.->|Write| Logs
```

## 9. Security Architecture

```mermaid
graph TD
    subgraph "Security Layers"
        subgraph "Frontend Security"
            CSP[Content Security Policy]
            XSS[XSS Prevention]
            InputVal[Input Validation]
            HTTPS[HTTPS Headers]
        end
        
        subgraph "Backend Security"
            CORS[CORS Configuration]
            BeanVal[Bean Validation]
            SQLInj[SQL Injection Prevention]
            SecHeaders[Security Headers]
        end
        
        subgraph "Infrastructure Security"
            NonRoot[Non-root Docker User]
            DepScan[Dependency Scanning]
            ContScan[Container Scanning]
            Secrets[Secret Management]
        end
        
        subgraph "CI/CD Security"
            OWASP[OWASP Dependency Check]
            Trivy[Trivy Scanner]
            SonarSec[SonarCloud Security]
            GitSec[GitHub Security]
        end
    end
    
    Request[Client Request] --> CSP
    CSP --> XSS
    XSS --> InputVal
    InputVal --> CORS
    CORS --> BeanVal
    BeanVal --> SQLInj
    SQLInj --> SecHeaders
    
    NonRoot -.->|Protects| Backend[Backend Container]
    DepScan -.->|Scans| Dependencies[Maven/NPM Deps]
    ContScan -.->|Scans| Containers[Docker Images]
```

## 10. Testing Strategy Pyramid

```mermaid
graph TD
    subgraph "Testing Pyramid"
        E2E[End-to-End Tests<br/>Full System Tests]
        Integration[Integration Tests<br/>API & Database Tests]
        Unit[Unit Tests<br/>Service & Component Tests]
        
        subgraph "Backend Tests"
            BUnit[TaskServiceTest<br/>TaskControllerTest]
            BInt[TaskControllerIntegrationTest]
        end
        
        subgraph "Frontend Tests"
            FUnit[app.test.js<br/>Component Tests]
            FInt[API Integration Tests]
        end
        
        subgraph "Coverage Tools"
            JaCoCo[JaCoCo - Java Coverage]
            Jest[Jest - JS Coverage]
        end
    end
    
    E2E --> Integration
    Integration --> Unit
    Unit --> BUnit
    Unit --> FUnit
    Integration --> BInt
    Integration --> FInt
    BUnit --> JaCoCo
    FUnit --> Jest
    
    style E2E fill:#f9f,stroke:#333,stroke-width:2px
    style Integration fill:#bbf,stroke:#333,stroke-width:2px
    style Unit fill:#bfb,stroke:#333,stroke-width:2px
```

## Key Architecture Decisions

### 1. **Modular Design**
- Separate frontend and backend services for independent scaling
- Clear separation of concerns with layered architecture
- RESTful API design for frontend-backend communication

### 2. **Technology Choices**
- **Spring Boot**: Mature, enterprise-ready framework with excellent ecosystem
- **H2 Database**: Lightweight, embedded database perfect for development
- **Vanilla JavaScript**: No framework dependencies, lighter footprint
- **Docker**: Consistent deployment across environments
- **GitHub Actions**: Native CI/CD integration with repository

### 3. **Security First**
- Multiple security layers from frontend to infrastructure
- Automated security scanning in CI/CD pipeline
- Regular dependency updates and vulnerability checks

### 4. **Testing Strategy**
- Comprehensive test coverage at all levels
- Automated testing in CI/CD pipeline
- Both unit and integration tests for confidence

### 5. **Observability**
- Health checks and metrics endpoints
- Structured logging with correlation IDs
- Performance monitoring capabilities

## How to Read These Diagrams

1. **System Architecture Overview**: Shows the high-level components and their relationships
2. **Backend Architecture**: Details the Spring Boot application structure
3. **Frontend Architecture**: Shows the vanilla JavaScript application organization
4. **Data Flow**: Illustrates how a request flows through the system
5. **API Endpoints**: Maps out all available REST endpoints
6. **Database Schema**: Shows the data model structure
7. **CI/CD Pipeline**: Visualizes the automated build and deployment process
8. **Docker Architecture**: Shows containerization structure
9. **Security Architecture**: Illustrates security layers and measures
10. **Testing Pyramid**: Shows testing strategy and coverage

These diagrams use Mermaid syntax and can be rendered in any Markdown viewer that supports Mermaid diagrams (GitHub, GitLab, VS Code with extensions, etc.).
