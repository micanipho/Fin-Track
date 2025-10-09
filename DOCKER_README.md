# FinTrack Docker Environment & API Documentation

This document provides comprehensive instructions for setting up and using the FinTrack Docker environment with integrated OpenAPI/Swagger documentation.

## 🐳 Docker Environment Setup

### Prerequisites

- Docker and Docker Compose installed
- Java 21 (for local development)
- Maven 3.9+ (for local builds)

### Quick Start

1. **Clone and Navigate to Project**
   ```bash
   cd /path/to/FinTrack
   ```

2. **Set Up Environment Variables**
   ```bash
   cp .env.example .env
   nano .env  # Update with your actual values
   ```

3. **Start the Application**
   ```bash
   ./docker-helper.sh start
   ```

### Docker Services

The Docker environment includes:

- **fintrack-app**: Main Spring Boot application (Port 8080)
- **postgres**: PostgreSQL 16 database (Port 5432)
- **redis**: Redis cache for session management (Port 6379)

### Docker Helper Script Commands

```bash
# Build the application
./docker-helper.sh build

# Start all services
./docker-helper.sh start

# Stop all services
./docker-helper.sh stop

# Restart services
./docker-helper.sh restart

# View logs (all services or specific service)
./docker-helper.sh logs
./docker-helper.sh logs fintrack-app

# Check service status
./docker-helper.sh status

# Clean up everything (containers, volumes, images)
./docker-helper.sh clean

# Show help
./docker-helper.sh help
```

## 📚 API Documentation (OpenAPI/Swagger)

### Accessing Documentation

Once the application is running, you can access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health

### Documentation Features

- **Interactive API Testing**: Try out endpoints directly from Swagger UI
- **JWT Authentication**: Built-in authentication support
- **Comprehensive Examples**: Request/response examples for all endpoints
- **Error Scenarios**: Detailed error response documentation
- **Schema Validation**: Automatic request/response validation

### API Documentation Structure

The API documentation includes:

1. **Authentication Endpoints** (`/api/auth`)
   - Login with JWT token generation
   - User registration with validation
   - Token refresh mechanism
   - Password reset flow
   - Logout functionality

2. **User Management** (`/api/users`)
   - User profile management
   - Account settings
   - User preferences

3. **Account Management** (`/api/accounts`)
   - Financial account operations
   - Account balance tracking
   - Account types and categories

### Using OpenAPI Annotations

Example of comprehensive API documentation in controllers:

```java
@Tag(name = "Authentication", description = "Authentication and user management endpoints")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(
        summary = "User Login",
        description = "Authenticate user with username/email and password to receive JWT tokens"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "423", description = "Account locked")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        // Implementation
    }
}
```

## 🔧 Configuration

### Environment Variables

Key environment variables in `.env` file:

```properties
# JWT Configuration
JWT_SECRET=your-secure-jwt-secret-key

# Email Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Application Configuration
FRONTEND_URL=http://localhost:3000

# Database Configuration
POSTGRES_DB=fintrack_db
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin
```

### Application Profiles

- **Default Profile**: Local development with external database
- **Docker Profile**: Containerized environment with service discovery

### Database Setup

The Docker environment automatically:
- Creates the `fintrack` schema
- Sets up proper permissions
- Initializes database extensions
- Runs Flyway migrations

## 🔍 Monitoring & Health Checks

### Health Endpoints

- **Application Health**: `/actuator/health`
- **Database Health**: Included in health check
- **Custom Health Indicators**: Database connectivity, external services

### Docker Health Checks

All services include health checks:
- **Application**: HTTP health endpoint
- **PostgreSQL**: Connection test
- **Redis**: Ping test

### Logging

View service logs:
```bash
# All services
./docker-helper.sh logs

# Specific service
./docker-helper.sh logs fintrack-app
./docker-helper.sh logs postgres
./docker-helper.sh logs redis
```

## 🚀 Development Workflow

### Local Development

1. **Start Database Only**
   ```bash
   docker-compose up -d postgres redis
   ```

2. **Run Application Locally**
   ```bash
   mvn spring-boot:run
   ```

### Full Docker Development

1. **Build and Start**
   ```bash
   ./docker-helper.sh build
   ./docker-helper.sh start
   ```

2. **Make Changes and Rebuild**
   ```bash
   ./docker-helper.sh restart
   ```

### Testing API Endpoints

Use Swagger UI at http://localhost:8080/swagger-ui.html:

1. **Authenticate**: Use the login endpoint to get JWT token
2. **Authorize**: Click "Authorize" button and enter "Bearer <token>"
3. **Test Endpoints**: Try out any protected endpoints

## 🔒 Security Features

### JWT Authentication

- **Access Tokens**: Short-lived (24 hours)
- **Refresh Tokens**: Long-lived (7 days)
- **Secure Headers**: Proper CORS and security headers

### Account Security

- **Rate Limiting**: Prevents brute force attacks
- **Account Lockout**: After failed login attempts
- **Password Validation**: Strong password requirements

### Docker Security

- **Non-root User**: Application runs as non-root user
- **Network Isolation**: Services communicate through dedicated network
- **Secret Management**: Environment-based configuration

## 🐛 Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Check what's using the port
   sudo lsof -i :8080
   
   # Stop conflicting service or change ports in docker-compose.yml
   ```

2. **Database Connection Issues**
   ```bash
   # Check database health
   ./docker-helper.sh logs postgres
   
   # Restart database
   docker-compose restart postgres
   ```

3. **Build Failures**
   ```bash
   # Clean rebuild
   ./docker-helper.sh clean
   ./docker-helper.sh build
   ```

### Debug Mode

Enable debug logging by setting in `.env`:
```properties
SPRING_PROFILES_ACTIVE=docker,debug
```

## 📝 API Testing Examples

### Authentication Flow

1. **Register User**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"firstName":"John","lastName":"Doe","email":"john@example.com","password":"SecurePass123!"}'
   ```

2. **Login**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"john@example.com","password":"SecurePass123!"}'
   ```

3. **Use Protected Endpoint**
   ```bash
   curl -X GET http://localhost:8080/api/users/profile \
     -H "Authorization: Bearer <your-jwt-token>"
   ```

## 📈 Production Considerations

### Environment-Specific Configuration

- Update `FRONTEND_URL` for production
- Use secure `JWT_SECRET`
- Configure proper email credentials
- Set up SSL/TLS certificates

### Scaling

- Use Docker Swarm or Kubernetes for orchestration
- Add load balancer for multiple application instances
- Configure Redis for session clustering
- Set up database replication

### Monitoring

- Integrate with monitoring tools (Prometheus, Grafana)
- Set up log aggregation (ELK Stack)
- Configure alerting for critical failures

## 🤝 Contributing

When adding new endpoints:

1. **Add OpenAPI Annotations**
   - Use `@Operation` for endpoint description
   - Add `@ApiResponses` for all possible responses
   - Include request/response examples

2. **Update Documentation**
   - Add endpoint to this README
   - Include usage examples
   - Document any new configuration

3. **Test Documentation**
   - Verify Swagger UI displays correctly
   - Test example requests work
   - Validate response schemas

---

For additional help or questions, please refer to the project documentation or create an issue in the repository.
