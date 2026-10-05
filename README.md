# ImageHost - Cloudinary Alternative

> A self-hosted image hosting service with WebP conversion, project organization, and comprehensive API access.

## 🚀 Features

### ✅ **Complete Image Management**
- Upload images in **14+ formats**: JPEG, PNG, GIF, BMP, TIFF, WEBP, HEIC, HEIF, SVG, ICO, PSD, PDF
- Automatic conversion to **WebP format** for optimized delivery
- Original images preserved for compatibility
- Thumbnail generation
- Custom file naming with UUIDs

### ✅ **Project/Folder Organization**
- Create unlimited projects to organize images
- Set default projects per user
- Delete projects (cascading image deletion)
- Search within projects
- Project-based statistics

### ✅ **User Management**
- User registration with validation
- JWT-based authentication
- Role-based access control (ADMIN, USER, GUEST)
- Password hashing with BCrypt (strength 12)
- Account lock after 5 failed attempts
- Storage limits per user (default: 100MB)
- Storage usage tracking

### ✅ **Powerful API**
- RESTful endpoints for all operations
- OpenAPI/Swagger documentation
- JWT token authentication
- Token refresh capability
- Pagination support
- Comprehensive error handling

### ✅ **Web Interface (Thymeleaf)**
- Responsive Bootstrap 5 UI
- Image gallery with filtering
- Upload form with drag-and-drop
- Project management interface
- User profile page
- Real-time statistics

### ✅ **Storage & Performance**
- File system storage for images
- H2 database (development) / PostgreSQL (production)
- Automatic WebP conversion and optimization
- Storage quota enforcement
- CORS support

## 📁 Project Structure

```
imagehost/
├── src/
│   ├── main/
│   │   ├── java/com/openlabmx/imagehost/
│   │   │   ├── config/               # Spring configurations
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── AppConfig.java
│   │   │   ├── controller/          # REST & Web controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ImageController.java
│   │   │   │   ├── ProjectController.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── ViewController.java
│   │   │   ├── dto/                  # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   │   ├── ImageUploadRequest.java
│   │   │   │   │   ├── ProjectRequest.java
│   │   │   │   │   ├── UserLoginRequest.java
│   │   │   │   │   └── UserRegisterRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ErrorResponse.java
│   │   │   │       ├── ImageResponse.java
│   │   │   │       ├── JwtResponse.java
│   │   │   │       ├── ProjectResponse.java
│   │   │   │       └── UserResponse.java
│   │   │   ├── entity/               # JPA Entities
│   │   │   │   ├── ImageEntity.java
│   │   │   │   ├── Project.java
│   │   │   │   ├── Role.java
│   │   │   │   └── User.java
│   │   │   ├── enums/               # Enumerations
│   │   │   │   └── RoleType.java
│   │   │   ├── exception/          # Custom exceptions
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── ForbiddenException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ImageProcessingException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── StorageException.java
│   │   │   │   ├── UnauthorizedException.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── repository/          # JPA Repositories
│   │   │   │   ├── ImageRepository.java
│   │   │   │   ├── ProjectRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── security/            # Security components
│   │   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtAuthenticationToken.java
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   └── JwtUserDetails.java
│   │   │   ├── service/             # Business services
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── ImageService.java
│   │   │   │   ├── ProjectService.java
│   │   │   │   └── UserService.java
│   │   │   ├── service/impl/       # Service implementations
│   │   │   │   ├── AuthServiceImpl.java
│   │   │   │   ├── ImageServiceImpl.java
│   │   │   │   ├── ProjectServiceImpl.java
│   │   │   │   └── UserServiceImpl.java
│   │   │   └── util/                # Utility classes
│   │   │       └── ImageUtils.java
│   │   ├── resources/
│   │   │   ├── static/
│   │   │   │   ├── css/
│   │   │   │   │   └── style.css
│   │   │   │   ├── js/
│   │   │   │   │   └── main.js
│   │   │   │   └── images/
│   │   │   ├── templates/
│   │   │   │   ├── base.html
│   │   │   │   ├── fragments/
│   │   │   │   │   ├── footer.html
│   │   │   │   │   └── navbar.html
│   │   │   │   ├── about.html
│   │   │   │   ├── gallery.html
│   │   │   │   ├── index.html
│   │   │   │   ├── login.html
│   │   │   │   ├── profile.html
│   │   │   │   ├── projects.html
│   │   │   │   ├── register.html
│   │   │   │   └── upload.html
│   │   │   └── application.properties
│   │   │   └── data.sql
│   │   └── ImageHostApplication.java
│   └── test/
│       └── java/com/openlabmx/imagehost/
│           └── ImageHostApplicationTests.java
├── pom.xml
├── README.md
├── LICENSE
└── PLAN.md
```

## 🔧 Requirements

### Java & Maven
- **Java 25** (or Java 17+)
- **Maven 3.8+**

### Dependencies (managed by Maven)
All dependencies are defined in `pom.xml`:
- Spring Boot 4.1.1
- Spring Security 6
- Spring Data JPA
- Thymeleaf
- H2 Database (dev)
- PostgreSQL (prod)
- JWT (jjwt-api)
- ModelMapper
- SpringDoc OpenAPI
- Thumbnailator
- WebP ImageIO
- PDFBox (for PDF support)
- Batik (for SVG support)
- And more...

## 🚀 Quick Start

### 1. Clone & Build

```bash
cd /c/Users/Lucia/Documents/Lionel/proyectos_web/claudinary
mvn clean install
```

### 2. Run with Maven

```bash
mvn spring-boot:run
```

### 3. Or Run with Java

```bash
mvn package
java -jar target/imagehost-1.0.0.jar
```

The application will start on `http://localhost:8080`

## 📊 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT token |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | Logout (clear session) |
| GET | `/api/auth/validate` | Validate JWT token |
| POST | `/api/auth/reset-password` | Request password reset |
| POST | `/api/auth/change-password` | Change user password |

### Users
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | Get current user |
| GET | `/api/users/{userId}` | Get user by ID |
| GET | `/api/users/username/{username}` | Get user by username |
| PUT | `/api/users/{userId}` | Update user |
| DELETE | `/api/users/{userId}` | Delete user |
| GET | `/api/users/all` | Get all users (admin) |
| GET | `/api/users/admins` | Get all admins (admin) |
| POST | `/api/users/{userId}/activate` | Activate user (admin) |
| POST | `/api/users/{userId}/deactivate` | Deactivate user (admin) |
| POST | `/api/users/{userId}/lock` | Lock user (admin) |
| POST | `/api/users/{userId}/unlock` | Unlock user (admin) |
| POST | `/api/users/{userId}/update-storage` | Update storage limit (admin) |
| GET | `/api/users/{userId}/stats` | Get user statistics |
| GET | `/api/users/check-username` | Check username availability |
| GET | `/api/users/check-email` | Check email availability |
| POST | `/api/users/change-password` | Change password |

### Projects
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/projects` | Create new project |
| GET | `/api/projects/{projectId}` | Get project by ID |
| GET | `/api/projects/user/{userId}` | Get user projects |
| GET | `/api/projects/my` | Get my projects |
| GET | `/api/projects/default` | Get default project |
| PUT | `/api/projects/{projectId}` | Update project |
| DELETE | `/api/projects/{projectId}` | Delete project |
| POST | `/api/projects/{projectId}/set-default` | Set default project |
| GET | `/api/projects/{projectId}/images` | Get project images |
| GET | `/api/projects/{projectId}/stats` | Get project statistics |
| GET | `/api/projects/search` | Search projects |
| POST | `/api/projects/{projectId}/delete-all-images` | Delete all images in project |
| GET | `/api/projects/exists/{projectName}` | Check project name availability |

### Images
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/images/upload` | Upload image |
| POST | `/api/images/upload/{projectId}` | Upload to specific project |
| GET | `/api/images/{imageId}` | Get image details |
| GET | `/api/images/filename/{filename}` | Get image by filename |
| GET | `/api/images/user/{userId}` | Get user images |
| GET | `/api/images/project/{projectId}` | Get project images |
| GET | `/api/images/user/{userId}/project/{projectId}` | Get user+project images |
| GET | `/api/images/search` | Search images |
| GET | `/api/images/{imageId}/download` | Download original image |
| GET | `/api/images/{imageId}/webp` | Get WebP version |
| GET | `/api/images/{imageId}/thumbnail` | Get thumbnail |
| DELETE | `/api/images/{imageId}` | Delete image |
| DELETE | `/api/images/project/{projectId}` | Delete all images in project |
| PATCH | `/api/images/{imageId}` | Update image metadata |
| GET | `/api/images/user/{userId}/stats` | Get user image stats |
| GET | `/api/images/{imageId}/stats` | Get image stats |
| POST | `/api/images/{imageId}/convert-webp` | Convert to WebP |
| GET | `/api/images/supported-formats` | Get supported formats |

### Web Pages (Thymeleaf)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Home page |
| GET | `/login` | Login page |
| GET | `/register` | Registration page |
| GET | `/upload` | Upload page |
| GET | `/gallery` | Gallery page |
| GET | `/gallery?projectId={id}` | Gallery with project filter |
| GET | `/projects` | Projects management |
| GET | `/profile` | User profile |
| GET | `/view-image/{imageId}` | View single image |
| GET | `/about` | About page |

## 🔐 Authentication

### JWT Configuration
The application uses JWT tokens for authentication with the following settings (configurable in `application.properties`):

```properties
jwt.secret=mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong123456789
jwt.access.expiration=3600 # 1 hour in seconds
jwt.refresh.expiration=86400 # 24 hours in seconds
```

### Token Storage
- Access tokens are stored in `localStorage`
- Tokens are sent in the `Authorization` header: `Bearer <token>`
- Refresh tokens are used to get new access tokens

### Authentication Flow
1. User submits credentials to `/api/auth/login`
2. Server validates credentials and returns JWT tokens
3. Client stores tokens and includes them in API requests
4. Server validates tokens using `JwtAuthenticationFilter`
5. Access is granted based on token validity and user roles

### Security Features
- **CSRF Protection**: Disabled for JWT (stateless)
- **CORS**: Configured in `CorsConfig.java`
- **Session Management**: Stateless (JWT)
- **Password Hashing**: BCrypt with strength 12
- **Account Lock**: After 5 failed login attempts
- **Role-Based Access**: Using `@PreAuthorize` annotations

## 🗃️ Database Configuration

### Development (H2 - Default)
The application uses H2 database in development mode:

```properties
# application.properties
spring.datasource.url=jdbc:h2:file:./data/imagehost-db
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Enable H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Access H2 Console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/imagehost-db`
- Username: `sa`
- Password: (leave empty)

### Production (PostgreSQL)
Create `application-prod.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/imagehost
spring.datasource.username=imagehost
spring.datasource.password=yourpassword
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

Run with production profile:
```bash
java -jar target/imagehost-1.0.0.jar --spring.profiles.active=prod
```

## 📦 Storage Configuration

### File Storage
Images are stored in the file system with the following structure:

```
uploads/
├── {userId}/
│   ├── {projectId}/
│   │   ├── {unique-filename}.jpg    # Original image
│   │   ├── webp/
│   │   │   └── {unique-filename}.webp  # WebP version
│   │   └── thumbnails/
│   │       └── {unique-filename}_thumb.webp  # Thumbnail
```

### Storage Settings (application.properties)
```properties
imagehost.storage.directory=./uploads
imagehost.max-file-size=10485760 # 10MB
imagehost.max-storage-per-user=104857600 # 100MB
```

## 🎨 Supported Image Formats

### Common Formats
- **JPEG** (`.jpg`, `.jpeg`) - Full support
- **PNG** (`.png`) - Full support
- **GIF** (`.gif`) - Full support (static images)
- **BMP** (`.bmp`) - Full support
- **WEBP** (`.webp`) - Full support

### Professional Formats
- **TIFF** (`.tiff`, `.tif`) - Full support (using JAI ImageIO)
- **HEIC** (`.heic`) - Supported (requires heif4j library)
- **HEIF** (`.heif`) - Supported (requires heif4j library)
- **SVG** (`.svg`) - Supported (rendered to PNG using Batik)
- **ICO** (`.ico`) - Supported
- **PSD** (`.psd`) - Supported (Photoshop format)
- **PDF** (`.pdf`) - Supported (first page rendered to image)

### Format Detection
The application detects image formats from:
1. File extension
2. Content-Type header
3. Magic bytes (file signature)

## 🔧 Customization

### JWT Secret
Generate a secure JWT secret (minimum 256 bits / 32 characters):

```bash
# Linux/macOS
openssl rand -base64 32

# Or use any random 32+ character string
```

### Storage Limits
Modify in `application.properties`:

```properties
# Per user storage limit (bytes)
imagehost.max-storage-per-user=1048576000 # 1GB

# Maximum file upload size (bytes)
imagehost.max-file-size=52428800 # 50MB
```

### WebP Quality
Modify in `ImageServiceImpl.java`:

```java
private static final float WEBP_QUALITY = 0.85f; // 0.0 - 1.0
```

### Default User Storage
Modify in `UserServiceImpl.java`:

```java
.storageLimit(100L * 1024L * 1024L) // 100MB default
```

## 📊 Monitoring & Logging

### Logging Configuration
The application uses Spring Boot's logging with the following levels:

```properties
# application.properties
logging.level.root=INFO
logging.level.com.openlabmx.imagehost=DEBUG
logging.level.org.springframework.security=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Log File
By default, logs are output to console. To write to file:

```properties
logging.file.name=logs/imagehost.log
logging.file.max-size=10MB
logging.file.max-history=7
```

## 🐳 Docker Deployment

### Dockerfile
```dockerfile
FROM openjdk:25-jdk-slim
WORKDIR /app
COPY target/imagehost-1.0.0.jar app.jar
COPY data.sql /docker-entrypoint-initdb.d/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    volumes:
      - ./uploads:/app/uploads
      - ./data:/app/data
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/imagehost
      - SPRING_DATASOURCE_USERNAME=imagehost
      - SPRING_DATASOURCE_PASSWORD=yourpassword
    depends_on:
      - db

  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=imagehost
      - POSTGRES_USER=imagehost
      - POSTGRES_PASSWORD=yourpassword
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Run with Docker Compose:
```bash
docker-compose up -d
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Manual Testing

#### Test Credentials
The application comes with pre-configured test users:

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| testuser | test123 | USER |

#### Test Scenarios

1. **Registration**: POST `/api/auth/register`
2. **Login**: POST `/api/auth/login`
3. **Upload Image**: POST `/api/images/upload`
4. **List Images**: GET `/api/images/user/{userId}`
5. **Download Image**: GET `/api/images/{imageId}/download`
6. **Create Project**: POST `/api/projects`
7. **User Statistics**: GET `/api/users/{userId}/stats`

## 🛠️ Troubleshooting

### Common Issues

#### 1. WebP Conversion Not Working
**Solution**: Ensure `webp-imageio` dependency is in classpath.

#### 2. HEIC/HEIF Support Not Available
**Solution**: Add `heif4j` library (already in pom.xml). For production, you may need native libraries.

#### 3. Out of Memory Errors
**Solution**: Increase JVM memory:
```bash
java -Xmx512m -jar target/imagehost-1.0.0.jar
```

#### 4. Database Connection Failed
**Solution**: Check database URL, username, and password in `application.properties`.

#### 5. File Upload Fails
**Solution**: Ensure `uploads` directory exists and has write permissions.

### Debug Mode
Enable debug logging:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--debug
```

Or in `application.properties`:
```properties
logging.level.root=DEBUG
logging.level.org.springframework=DEBUG
```

## 📚 API Documentation

### Swagger UI
Access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/v3/api-docs
```

### API Features
- **JWT Authentication**: All endpoints support JWT token authentication
- **Validation**: Request validation with detailed error messages
- **Pagination**: Support for paginated responses
- **Error Handling**: Consistent error responses
- **Documentation**: OpenAPI annotations for all endpoints

## 🌐 Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `JWT_SECRET` | JWT signing secret | `mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong123456789` |
| `JWT_ACCESS_EXPIRATION` | Access token expiration (seconds) | `3600` |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (seconds) | `86400` |
| `SPRING_DATASOURCE_URL` | Database URL | `jdbc:h2:file:./data/imagehost-db` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `sa` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | (empty) |

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 🙏 Acknowledgments

- Spring Boot - Application framework
- Thymeleaf - Server-side Java template engine
- JWT - JSON Web Tokens for authentication
- Thumbnailator - Java thumbnail generation library
- WebP ImageIO - WebP support for Java ImageIO
- And all other open-source libraries used in this project

---

**ImageHost** - Your self-hosted Cloudinary alternative.

Built with ❤️ and Java.
