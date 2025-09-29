# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

notimo는 카카오톡 알림톡 템플릿 검증,생성 위한 시스템으로, 공유템플릿 제공, AI와 자연어를 통해 알림톡 템플릿을 생성, 수정 하며 개인화된 템플릿을 통해 효율적인 커뮤니케이션을 지원하는 Spring Boot + React + AI 멀티 모듈 프로젝트입니다.

### Tech Stack
- **Backend**: Spring Boot 3.5.5 (Java 21, Virtual Threads), Redis, MySQL
- **AI Service**: FastAPI (Python 3.11), LangChain, OpenAI/Google AI
- **Frontend**: React 19.1.1, Material-UI
- **Infrastructure**: Docker Compose, AWS RDS, EC2

## Development Environment Setup

### Prerequisites
1. Java 21+ (Project Loom Virtual Threads enabled)
2. Node.js 22.17.0 + npm 10.9.2
3. Python 3.11 with Poetry
4. Docker & Docker Compose
5. AWS PEM key for RDS connection

### Start Development Environment
```bash
# 전체 백엔드 인프라 시작 (SSH tunnel + Redis + Python-AI)
./start-backend.sh

# 또는 개별 서비스 시작
docker-compose up -d redis python-ai
```

### Run Applications

#### Spring Boot (Main Backend)
```bash
# Gradle로 빌드 및 실행
./gradlew bootRun

# 또는 IDE에서 직접 실행
# Main class: com.jober.final2teamdrhong.Final2teamDrHongApplication
```

#### Frontend (React)
```bash
cd frontend
npm install
npm start  # http://localhost:3000
```

#### Python AI Service
```bash
cd backend-ai
poetry install
poetry run uvicorn app.main:app --reload --port 8000
```

## Testing

### Spring Boot Tests
```bash
./gradlew test                    # 전체 테스트
./gradlew test --tests ClassName  # 특정 클래스 테스트
./gradlew test -i                 # 상세 로그와 함께 테스트
```

### Frontend Tests
```bash
cd frontend
npm test                          # 테스트 실행
npm test -- --coverage           # 커버리지와 함께
```

## Architecture & Code Organization

### Multi-Module Structure
```
Final-2team-DrHong/
├── backend-spring/     # 메인 Spring Boot 애플리케이션
├── backend-ai/         # AI 기능 FastAPI 서비스
├── frontend/           # React 웹 애플리케이션
└── (Docker configs)    # 개발환경 Docker 설정
```

### Spring Boot Package Structure
```
com.jober.final2teamdrhong/
├── config/         # 설정 클래스들 (Security, JWT, Redis, CORS 등)
├── controller/     # REST API 컨트롤러들
├── service/        # 비즈니스 로직 서비스들
├── repository/     # JPA 레포지토리들
├── entity/         # JPA 엔티티들
├── dto/           # 요청/응답 DTO들
├── exception/     # 커스텀 예외 처리
├── filter/        # 커스텀 필터들
└── util/          # 유틸리티 클래스들
```

### Key Configuration Points

#### Database & JPA
- **Primary DB**: MySQL via SSH tunnel (localhost:3307)
- **JPA DDL**: `update` mode for development
- **OSIV**: Disabled for resource optimization
- All DB operations must complete within service layer transactions

#### Security & Authentication
- **JWT-based authentication** with refresh tokens
- **OAuth2 integration** (Google Social Login)
- **Rate limiting** implemented for security endpoints
- **CORS** configured for development origins
- **Spring Security** with method-level security

#### Redis Integration
- **Session storage**, **JWT blacklisting**, **rate limiting**
- **Email verification codes**, **OAuth2 temporary data**
- Connection: localhost:6379

#### Email Service
- **Gmail SMTP** integration for verification emails
- **Rate-limited** email sending and verification

## Development Guidelines

### Environment Configuration
- Use `.env` file for sensitive data (copy from `.env.example`)
- Required variables: `DB_PASSWORD`, `JWT_SECRET_KEY`, `MAIL_PASSWORD`, etc.
- Development vs Production controlled by `app.environment.development`

### API Development
- **Base path**: `/api` (configured in `server.servlet.context-path`)
- **Swagger UI**: Available at `/api/swagger-ui.html`
- **API Docs**: Available at `/api/v3/api-docs`

### Database Migrations
- DDL auto-update enabled for development
- For production, use explicit migration scripts
- Always test schema changes in development first

### Testing Strategy & Guidelines

#### Test Code Writing Principles
1. **실제 구현 기반 작성**: 테스트 코드는 반드시 실제 구현된 메서드 시그니처와 비즈니스 로직을 기반으로 작성
2. **순환 의존성 파악**: 서비스 간 의존성을 철저히 분석하여 Mock 설정 누락 방지
3. **계층별 테스트 분리**: Repository → Service → Controller 순서로 계층별 독립 테스트 작성
4. **한국어 테스트 설명**: `@DisplayName`으로 테스트 목적을 한국어로 명확히 기술

#### Test Layer Conventions

**Repository Tests** (`@DataJpaTest`)
- `TestEntityManager`를 활용한 데이터 준비
- 실제 DB 연산 검증 (저장, 조회, 삭제)
- 연관관계 매핑 및 N+1 쿼리 방지 확인
- 예시: `UserRepositoryTest.java`

**Service Tests** (`@ExtendWith(MockitoExtension.class)`)
- `@Mock`으로 의존성 주입, `@InjectMocks`로 대상 서비스
- `@Nested` 클래스로 메서드별 테스트 그룹화
- BDDMockito 사용 (`given().willReturn()`, `then().should()`)
- 비즈니스 로직과 예외 상황 모두 검증
- 예시: `UserServiceTest.java`

**Controller Tests** (`@SpringBootTest + @AutoConfigureMockMvc`)
- 전체 Spring Context 로딩으로 실제 환경 시뮬레이션
- `@WithAnonymousUser` 또는 커스텀 `@WithMockJwtClaims` 사용
- UTF-8 인코딩 설정으로 한글 응답 처리
- Rate Limiting, Redis 연동 등 인프라 계층까지 통합 테스트
- 예시: `AuthControllerTest.java`

#### Mock & Test Data Management

**커스텀 Security Context**
```java
@WithMockJwtClaims(userId = 1, email = "test@example.com", role = "USER")
```

**테스트 데이터 격리**
- `@BeforeEach`에서 DB 초기화 및 시퀀스 리셋
- Redis Rate Limit 키 정리로 테스트 간 격리 보장
- `EntityManager.flush()`와 `clear()`로 영속성 컨텍스트 동기화

**순환 의존성 해결**
- 서비스 간 의존성 체인 분석: `UserService` → `AuthService` → `RefreshTokenService`
- Mock 설정 시 모든 의존성 체인을 고려하여 누락 방지
- 특히 `UserValidationService`, `BlacklistService` 등 보안 관련 서비스 의존성 주의

#### Common Testing Patterns

**Entity 생성 패턴**
```java
User testUser = User.create("테스트사용자", "test@example.com", "010-1234-5678");
UserAuth userAuth = UserAuth.createLocalAuth(testUser, passwordEncoder.encode("password"));
```

**MockMvc 요청 패턴**
```java
mockMvc.perform(post("/auth/login")
    .contentType(MediaType.APPLICATION_JSON)
    .characterEncoding("UTF-8")
    .content(objectMapper.writeValueAsString(request)))
    .andDo(print())
    .andExpect(status().isOk());
```

### Docker Development
- **SSH tunnel** required for AWS RDS access (handled by `start-backend.sh`)
- **Redis & Python-AI** run in containers
- **Spring Boot & React** run locally for faster development

## Important Notes

### Multi-Service Communication
- **Spring Boot** ↔ **Python-AI**: HTTP/REST calls to localhost:8000
- **Frontend** ↔ **Spring Boot**: HTTP/REST calls to localhost:8080/api
- **All services** share Redis for session/cache management

### Performance Considerations
- **Virtual Threads** enabled for improved request handling
- **Connection pooling** configured for database
- **Redis caching** for frequently accessed data
- **Rate limiting** prevents abuse

### Security Best Practices
- **JWT secrets** managed via environment variables
- **Database credentials** never committed to version control
- **CORS** properly configured for allowed origins
- **Input validation** on all API endpoints

### Monitoring & Debugging
- **SQL logging** enabled in development
- **Swagger documentation** available for API testing
- **Detailed error handling** with custom exceptions
- **Request/Response logging** configured

## Git Commit Conventions

### Commit Message Format
```
<type>(<scope>): <subject>

<body>
```

### Commit Types
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `chore`: 빌드 설정, 패키지 매니저 설정 등
- `docs`: 문서 변경
- `style`: 코드 포매팅, 세미콜론 누락 수정 등
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드 추가/수정
- `merge`: 브랜치 병합

### Commit Rules
1. **파일 단위 커밋**: 한 번에 하나의 파일만 수정하여 커밋
2. **기능 단위 분리**: 관련 없는 변경사항은 별도 커밋으로 분리
3. **한국어 커밋 메시지**: 제목과 본문 모두 한국어로 작성
4. **Claude Code 푸터 사용 안함**: Claude Code의 자동 생성 푸터는 사용하지 않음

### Commit Examples
```bash
# 좋은 예시
feat(UserController): 사용자 목록 조회 API 추가
fix(AuthService): JWT 토큰 만료 시간 검증 로직 수정
chore(application.properties): 미사용 설정 제거
test(UserServiceTest): 사용자 저장 테스트 케이스 추가

# 나쁜 예시
feat: 여러 파일 동시 수정 (파일 단위 분리 필요)
Update files (영어 사용, 구체적이지 않음)
```

### Scope Guidelines
- **Controller**: `UserController`, `AuthController` 등
- **Service**: `UserService`, `AuthService` 등
- **Entity**: `User`, `Template` 등
- **Config**: `SecurityConfig`, `RedisConfig` 등
- **Test**: `UserServiceTest`, `AuthControllerTest` 등