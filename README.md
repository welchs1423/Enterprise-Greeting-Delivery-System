# Enterprise Greeting Delivery System (EGDS)

> **제로 트러스트 기반, 이벤트 드리븐, 캐시 최적화, 완전 감사 추적 엔터프라이즈 인사 메시지 전달 플랫폼**
> `v2.0.0-RELEASE` | Java 17 | Spring Boot 3.2 | Oracle DB (H2 시뮬레이션) | Kafka | Redis (시뮬레이션) | JWT

---

## 도입 배경 및 아키텍처 철학

현대의 B2B 엔터프라이즈 환경에서 `System.out.println("Hello, World!")`와 같은 무방비 직접 출력 방식은 더 이상 수용 불가합니다. 이는 단순한 코드 스타일의 문제가 아니라, 조직의 보안 정책, 감사 의무, 확장성 요건, 장애 복원력에 대한 근본적인 도전입니다.

EGDS v2.0은 단 하나의 인사 메시지를 전달하기 위해 아래의 모든 엔터프라이즈 필수 요건을 충족합니다.

- **제로 트러스트 보안(Zero-Trust Security)**: JWT 기반 무상태 인증, `ROLE_GREETING_ADMIN` 권한 강제, BCrypt 자격증명 암호화
- **이벤트 드리븐 아키텍처(Event-Driven Architecture)**: HTTP 요청과 실제 처리의 완전 분리. Kafka 발행자-소비자 구조로 비동기 전달
- **분산 캐시 계층(Distributed Cache Layer)**: "Hello"와 "World"의 결합조차 Redis 캐시를 통해 최적화. `@Cacheable` / `@CachePut` / `@CacheEvict` 전주기 관리
- **감사 영속성(Audit Persistence)**: 모든 전달 사건의 발생 시각, 요청 IP, 실행 스레드, 인증 주체를 Oracle DB에 `@Transactional` + JPA Auditing으로 영구 기록
- **감사 추적 연속성(Trace Continuity)**: UUID 기반 상관 식별자(Correlation ID)가 HTTP 응답 → Kafka 이벤트 → 파이프라인 → DB 감사 로그까지 전 계층을 관통
- **계약 기반 설계(Contract-Based Design)**: 모든 컴포넌트 간 의존성은 인터페이스 계약을 통해서만 형성

---

## 전체 요청 처리 흐름

```
클라이언트
  │
  ├─ POST /api/v1/auth/token  ──▶ AuthController
  │   (username + password)           │
  │                                   ▼
  │                          AuthenticationManager
  │                                   │ BCrypt 검증
  │                          GreetingUserDetailsService
  │                                   │
  │                          JwtTokenProvider.generateToken()
  │                                   │
  │                          ◀── JWT Bearer Token
  │
  └─ GET /api/v1/greeting  ──▶ JwtAuthenticationFilter
      (Authorization: Bearer <jwt>)   │ 서명 검증 + 권한 확인
                                      ▼
                             SecurityContextHolder 설정
                                      │
                             GreetingController
                             @PreAuthorize("hasRole('GREETING_ADMIN')")
                                      │
                             GreetingEventPublisher
                                      │
                             KafkaTemplate.send(topic, correlationId, GreetingEvent)
                                      │
                             ◀── HTTP 202 Accepted + correlationId
                             (비동기 처리 계속)
                                      │
                             Kafka Broker ──▶ GreetingEventConsumer
                                                      │
                                        MessageDeliveryPipeline.execute()
                                                      │
                                        ┌─────────────┼─────────────────┐
                                        ▼             ▼                 ▼
                               GreetingCacheService  MessageDeliveryService  AuditLogService
                               assembleGreeting()    (validate/map/output)   @Transactional REQUIRES_NEW
                               @Cacheable            ConsoleOutputStrategy   GreetingAuditLog → Oracle DB
                               Cache MISS → 계산      System.out.println()
                               Cache HIT → 즉시 반환
```

---

## 아키텍처 컴포넌트 전체 목록

### 핵심 기반 레이어 (v1.0 계승)

| 컴포넌트 | 클래스 | 계층 | 설명 |
|---|---|---|---|
| 메시지 공급자 계약 | `IMessageProvider` | Interface | 메시지 페이로드 생성 컴포넌트의 계약 정의 |
| 출력 전략 계약 | `IMessageOutputStrategy` | Interface | 출력 채널 컴포넌트의 전략 계약 정의 |
| 전달 서비스 계약 | `IMessageDeliveryService` | Interface | 생명주기 오케스트레이션 컴포넌트의 최상위 계약 |
| 팩토리 계약 | `IGreetingFactory` | Interface | 파이프라인 컴포넌트 생성 팩토리의 추상 계약 |
| 검증기 계약 | `IMessageValidator` | Interface | 메시지 무결성 검증 컴포넌트의 계약 정의 |
| 메시지 전달 객체 | `MessageContentDto` | DTO | 원시 메시지 페이로드를 캡슐화하는 불변 전송 객체 (Builder 패턴) |
| 전달 결과 객체 | `MessageDeliveryResult` | DTO | 단일 전달 생명주기 실행 결과를 담는 값 객체 |
| 도메인 엔티티 | `MessageEntity` | Entity | 출력 채널 전달을 위해 준비된 도메인 표현 객체 |
| 매퍼 | `MessageMapper` | Mapper | DTO를 Entity로 변환하는 무상태 매핑 컴포넌트 |
| 표준 팩토리 | `StandardGreetingFactory` | Factory | 기본 운영 구성을 위한 구체 팩토리 구현체 |
| 팩토리 레지스트리 | `GreetingFactoryProvider` | Registry | 팩토리 변종을 등록하고 타입 식별자로 조회하는 서비스 로케이터 |
| 메시지 공급자 | `HelloWorldMessageProvider` | Provider | 캐시 레이어를 통해 조립된 인사 메시지 페이로드 생성 |
| 콘솔 출력 전략 | `ConsoleOutputStrategy` | Strategy | 표준 출력 스트림을 대상으로 하는 출력 전략 구현체 |
| 검증기 | `MessageContentValidator` | Validator | EGDS 메시지 무결성 명세를 강제하는 참조 구현체 |
| 로깅 애스펙트 | `MessageDeliveryLoggingAspect` | Aspect | 파이프라인 각 단계의 전후 감사 이벤트를 기록하는 횡단 관심사 컴포넌트 |
| 전달 서비스 | `MessageDeliveryService` | Service | 파이프라인 전 단계를 오케스트레이션하는 핵심 서비스 구현체 |
| 파이프라인 퍼사드 | `MessageDeliveryPipeline` | Facade | 서비스 조립 및 실행을 담당하는 최상위 진입 퍼사드 |

### 보안 레이어 (v2.0 신규)

| 컴포넌트 | 클래스 | 계층 | 설명 |
|---|---|---|---|
| JWT 토큰 공급자 | `JwtTokenProvider` | Security | HMAC-SHA256 서명 기반 JWT 생성, 검증, 클레임 추출 |
| JWT 인증 필터 | `JwtAuthenticationFilter` | Security | 매 요청의 Authorization 헤더에서 JWT를 추출하여 보안 컨텍스트 설정 |
| 인증 진입점 | `JwtAuthenticationEntryPoint` | Security | 미인증 접근 시 HTTP 401 JSON 응답 반환 |
| 사용자 상세 서비스 | `GreetingUserDetailsService` | Security | `ROLE_GREETING_ADMIN` 권한을 보유한 단일 관리자 계정 관리 |
| 보안 설정 | `SecurityConfig` | Config | 필터 체인 정의, STATELESS 세션, 메서드 보안 활성화 |
| 패스워드 인코더 설정 | `PasswordEncoderConfig` | Config | BCryptPasswordEncoder 빈 정의 (순환 의존성 방지를 위해 분리) |

### 영속성 및 감사 레이어 (v2.0 신규)

| 컴포넌트 | 클래스 | 계층 | 설명 |
|---|---|---|---|
| 감사 로그 엔티티 | `GreetingAuditLog` | JPA Entity | 전달 사건을 Oracle DB에 영구 기록하는 JPA 엔티티 (JPA Auditing 적용) |
| 감사 로그 리포지토리 | `GreetingAuditLogRepository` | Repository | Spring Data JPA 기반 CRUD 및 도메인 쿼리 메서드 |
| 감사 로그 서비스 | `AuditLogService` | Service | @Transactional REQUIRES_NEW + SERIALIZABLE 격리 수준으로 감사 기록 |
| JPA 감사 설정 | `JpaAuditingConfig` | Config | @EnableJpaAuditing + AuditorAware (SecurityContextHolder 기반) |

### 메시지 브로커 레이어 (v2.0 신규)

| 컴포넌트 | 클래스 | 계층 | 설명 |
|---|---|---|---|
| 인사 이벤트 | `GreetingEvent` | Event DTO | Kafka 토픽으로 전송되는 JSON 직렬화 가능 이벤트 페이로드 |
| 이벤트 발행자 | `GreetingEventPublisher` | Producer | KafkaTemplate 기반 비동기 이벤트 발행 (correlationId를 파티션 키로 사용) |
| 이벤트 소비자 | `GreetingEventConsumer` | Consumer | @KafkaListener 기반 비동기 수신 → 파이프라인 실행 → 감사 로그 기록 |
| Kafka 설정 | `KafkaConfig` | Config | 그리팅 이벤트 토픽 정의 (3 파티션, 1 레플리카) |

### 캐시 레이어 (v2.0 신규)

| 컴포넌트 | 클래스 | 계층 | 설명 |
|---|---|---|---|
| 인사 캐시 서비스 | `GreetingCacheService` | Service | @Cacheable / @CachePut / @CacheEvict 기반 "Hello" + "World" 조합 캐싱 |
| 캐시 설정 상수 | `CacheConfig` | Config | 캐시 영역 이름 상수 정의 (로컬: ConcurrentMapCache, 운영: Redis) |

### Web 레이어 (v2.0 신규)

| 컴포넌트 | 클래스 | 계층 | 설명 |
|---|---|---|---|
| 인증 컨트롤러 | `AuthController` | REST | POST /api/v1/auth/token — 자격증명 검증 후 JWT 발행 |
| 인사 컨트롤러 | `GreetingController` | REST | GET /api/v1/greeting — ROLE_GREETING_ADMIN 전용, Kafka 이벤트 발행 후 202 반환 |

---

## 적용 기술 스택

| 항목 | 기술 | 버전 |
|---|---|---|
| 언어 | Java | 17 |
| 프레임워크 | Spring Boot | 3.2.5 |
| 보안 | Spring Security + jjwt | 6.x + 0.12.5 |
| 영속성 | Spring Data JPA + Hibernate | 6.x |
| 데이터베이스 | Oracle Database 19c+ (로컬: H2) | - |
| 메시지 브로커 | Apache Kafka + Spring Kafka | 3.x |
| 캐시 | Redis (로컬: ConcurrentMapCache) | - |
| 빌드 | Apache Maven | 3.8+ |

---

## 적용 디자인 패턴

| 패턴 | 적용 클래스 | 목적 |
|---|---|---|
| Abstract Factory | `AbstractGreetingFactory`, `StandardGreetingFactory`, `GreetingFactoryProvider` | 파이프라인 컴포넌트 집합의 일관된 생성 및 변종 관리 |
| Builder | `MessageContentDto.Builder`, `GreetingAuditLog.Builder` | 불변 객체의 단계적 생성 및 필수 필드 유효성 보장 |
| Strategy | `IMessageOutputStrategy`, `ConsoleOutputStrategy` | 출력 채널의 런타임 교체 가능성 확보 |
| Facade | `MessageDeliveryPipeline` | 복잡한 파이프라인 조립 로직을 단일 인터페이스로 노출 |
| Service Locator | `GreetingFactoryProvider` | 타입 식별자 기반의 팩토리 레지스트리 및 런타임 해석 |
| AOP (시뮬레이션) | `MessageDeliveryLoggingAspect` | 파이프라인 경계 전후의 횡단 감사 로깅 분리 |
| Filter Chain | `JwtAuthenticationFilter` | 요청 인터셉션 및 보안 컨텍스트 주입 |
| Observer / Event | `GreetingEvent`, `GreetingEventPublisher`, `GreetingEventConsumer` | 요청과 처리의 완전한 시간적 분리 |
| Decorator | `@Cacheable`, `@Transactional` | 비즈니스 로직에 캐시/트랜잭션 횡단 관심사 비침투적 적용 |

---

## 빌드 및 실행

### 전제 조건

| 항목 | 요구 버전 |
|---|---|
| JDK | 17 이상 |
| Apache Maven | 3.8 이상 |
| Apache Kafka | 3.x (로컬 실행 시, 없으면 컨텍스트는 기동되나 전달 불가) |

### 빌드

```bash
mvn clean package -DskipTests
```

### 실행

```bash
java -jar target/enterprise-greeting-delivery-system-2.0.0-RELEASE.jar
```

### 로컬 테스트 (JWT 발급 → 인사 전달)

```bash
# 1. JWT 토큰 발급
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"greeting.admin","password":"egds-admin-pass"}' \
  | jq -r '.token')

# 2. 인사 전달 요청 (비동기)
curl -X GET http://localhost:8080/api/v1/greeting \
  -H "Authorization: Bearer $TOKEN"
```

### 테스트 실행

```bash
mvn test
```

---

## 오류 처리

| 오류 코드 | 발생 지점 | 원인 |
|---|---|---|
| HTTP 401 Unauthorized | `JwtAuthenticationEntryPoint` | JWT 토큰 없음 또는 서명 검증 실패 |
| HTTP 403 Forbidden | Spring Security Method Security | `ROLE_GREETING_ADMIN` 권한 없음 |
| `ERR_FACTORY_NOT_FOUND` | `GreetingFactoryProvider` | 등록되지 않은 팩토리 타입 요청 |
| `ERR_VALIDATION_NULL_DTO` | `MessageContentValidator` | null DTO 수신 |
| `ERR_VALIDATION_EMPTY_CONTENT` | `MessageContentValidator` | 메시지 본문 누락 |
| `ERR_VALIDATION_CONTENT_TOO_LARGE` | `MessageContentValidator` | 4096자 초과 페이로드 |
| `ERR_VALIDATION_MISSING_CORRELATION_ID` | `MessageContentValidator` | 상관 식별자 누락 |
| `ERR_NULL_ENTITY` | `ConsoleOutputStrategy` | null 엔티티 수신 |
| `ERR_OUTPUT_WRITE_FAILURE` | `ConsoleOutputStrategy` | 출력 스트림 기록 실패 |

---

## 테스트 구성

| 테스트 클래스 | 유형 | 검증 대상 |
|---|---|---|
| `JwtTokenProviderTest` | 단위 | JWT 생성, 서명 검증, 만료 처리, 위변조 감지 |
| `SecurityLayerTest` | 통합 (MockMvc) | HTTP 401/403/202 응답, JWT 발급/검증 흐름 |
| `AuditLogServiceTest` | JPA 슬라이스 (@DataJpaTest) | 감사 로그 영속성, JPA Auditing, 트랜잭션 격리 |
| `GreetingCacheServiceTest` | 통합 | 캐시 미스/히트, @CacheEvict, @CachePut 동작 |
| `GreetingEventPublisherTest` | 단위 (Mockito) | KafkaTemplate 호출 검증, 토픽/키 파라미터 |
| `GreetingEventConsumerIntegrationTest` | 통합 (@EmbeddedKafka) | Kafka 발행-소비 사이클, 감사 로그 비동기 저장 |
| `GreetingDeliveryIntegrationTest` | E2E (@EmbeddedKafka + MockMvc) | JWT 인증 → Kafka → 파이프라인 → DB 감사 전 계층 |

---

## 작업 이력 (Changelog)

### [2026-04-21] v2.0.0-RELEASE — 엔터프라이즈 4대 핵심 인프라 통합

**신규 추가 파일**

| 파일 | 변경 내용 |
|---|---|
| `pom.xml` | Spring Boot 3.2.5 parent 적용, 기존 순수 Java 빌드 전면 교체. spring-boot-starter-web/security/data-jpa/cache/validation, spring-kafka, jjwt 0.12.5, H2 런타임, spring-boot-starter-test/spring-security-test/spring-kafka-test 추가 |
| `src/main/resources/application.properties` | H2(Oracle 호환 모드), 로컬 Kafka, 단순 캐시(ConcurrentMap), JWT 시크릿/만료 설정 신규 작성 |
| `src/main/resources/application-prod.properties` | Oracle 19c 데이터소스, Redis 캐시, Kafka 클러스터, JWT 환경변수 기반 운영 설정 신규 작성 |
| `src/test/resources/application.properties` | H2 인메모리, EmbeddedKafka 브로커 주소 자동 주입, 단순 캐시 테스트 전용 설정 신규 작성 |
| `config/SecurityConfig.java` | STATELESS 필터 체인 정의, `@EnableWebSecurity`, `@EnableMethodSecurity`, JWT 필터 등록 |
| `config/PasswordEncoderConfig.java` | `BCryptPasswordEncoder` 빈 분리 정의 (순환 의존성 해소) |
| `config/JpaAuditingConfig.java` | `@EnableJpaAuditing`, `AuditorAware` 빈 (`SecurityContextHolder` 기반, 미인증 시 "SYSTEM" 반환) |
| `config/KafkaConfig.java` | 그리팅 이벤트 토픽 `NewTopic` 빈 정의 (파티션 3, 레플리카 1) |
| `config/CacheConfig.java` | 캐시 영역 이름 상수 `GREETING_PARTS_CACHE` 정의 |
| `security/JwtTokenProvider.java` | jjwt 0.12.x API 기반 토큰 생성(`generateToken`), 클레임 추출(`extractUsername`, `extractRoles`), 서명 검증(`validateToken`) 구현 |
| `security/JwtAuthenticationFilter.java` | `OncePerRequestFilter` 상속, `Authorization: Bearer` 헤더 파싱 → `SecurityContextHolder` 주입 |
| `security/JwtAuthenticationEntryPoint.java` | `AuthenticationEntryPoint` 구현, 미인증 접근 시 HTTP 401 JSON 응답 반환 |
| `security/GreetingUserDetailsService.java` | `UserDetailsService` 구현, `ROLE_GREETING_ADMIN` 단일 계정, BCrypt 선인코딩 |
| `core/entity/GreetingAuditLog.java` | `@Entity`, Oracle 시퀀스 생성기, `@CreatedDate`/`@LastModifiedBy` JPA Auditing, Builder 패턴 적용 |
| `core/repository/GreetingAuditLogRepository.java` | `JpaRepository` 상속, `findByCorrelationId`, `findByOccurredAtBetween`, `countSuccessfulDeliveries` 쿼리 추가 |
| `core/service/AuditLogService.java` | `@Transactional` 클래스 레벨 기본 적용, `record()` — `REQUIRES_NEW + SERIALIZABLE`, `findByCorrelationId()` — `readOnly + timeout(5)`, `countSuccessfulDeliveries()` — `REQUIRES_NEW + readOnly` 과도 적용 |
| `core/service/GreetingCacheService.java` | `assembleGreeting()` — `@Cacheable`, `refreshGreeting()` — `@CachePut`, `evictAll()` — `@CacheEvict(allEntries=true)` |
| `messaging/GreetingEvent.java` | Jackson JSON 직렬화 가능 이벤트 DTO, `correlationId`/`requestIp`/`principalName`/`issuedAt` 필드 |
| `messaging/GreetingEventPublisher.java` | `KafkaTemplate.send()` 비동기 발행, `correlationId` 파티션 키 사용, `CompletableFuture` 반환 |
| `messaging/GreetingEventConsumer.java` | `@KafkaListener` 구독, 파이프라인 실행 후 `AuditLogService.record()` 호출 |
| `web/AuthController.java` | `POST /api/v1/auth/token` — `AuthenticationManager` 위임 인증, `JwtTokenProvider.generateToken()` 후 반환 |
| `web/GreetingController.java` | `GET /api/v1/greeting` — `@PreAuthorize("hasRole('GREETING_ADMIN')")`, 클라이언트 IP 추출, `GreetingEventPublisher.publish()` 후 202 반환 |
| `web/dto/TokenRequest.java` | 로그인 요청 DTO (`username`, `password`, `@NotBlank` 검증) |
| `web/dto/TokenResponse.java` | 토큰 응답 DTO (`token`, `tokenType`, `expiresInMs`) |
| `web/dto/GreetingResponse.java` | 인사 전달 응답 DTO (`correlationId`, `status`, `message`) |

**수정된 기존 파일**

| 파일 | 변경 내용 |
|---|---|
| `EgdsApplication.java` | `main()` 수동 배선 제거, `SpringApplication.run()` 으로 교체. `@SpringBootApplication` + `@EnableCaching` + `@EnableKafka` 추가 |
| `core/pipeline/MessageDeliveryPipeline.java` | `@Component` 추가, 생성자를 `MessageDeliveryService` 단일 주입으로 변경, `execute()` 반환 타입 `void` → `MessageDeliveryResult` 변경 |
| `core/service/MessageDeliveryService.java` | `@Service` 추가 |
| `core/provider/HelloWorldMessageProvider.java` | `@Component` 추가, `GreetingCacheService` 생성자 주입, `provideMessage()` 내부에서 캐시 서비스를 통해 콘텐츠 조립 |
| `core/strategy/ConsoleOutputStrategy.java` | `@Component` 추가 |
| `core/validator/MessageContentValidator.java` | `@Component` 추가 |
| `core/mapper/MessageMapper.java` | `@Component` 추가 |
| `core/aspect/MessageDeliveryLoggingAspect.java` | `@Component` 추가 |

**신규 추가 테스트 파일**

| 파일 | 변경 내용 |
|---|---|
| `security/JwtTokenProviderTest.java` | Spring 컨텍스트 없이 직접 인스턴스화, 생성/추출/검증/만료/위변조 6개 케이스 |
| `security/SecurityLayerTest.java` | `@SpringBootTest` + `MockMvc`, 401/403/202 응답, 자격증명 오류 케이스 |
| `core/service/AuditLogServiceTest.java` | `@DataJpaTest` + `@Import(JpaAuditingConfig, AuditLogService)`, 영속성·Auditing·correlationId 필터 검증 |
| `core/service/GreetingCacheServiceTest.java` | `@SpringBootTest` + `@EmbeddedKafka`, 캐시 미스/히트/무효화/강제갱신 검증 |
| `messaging/GreetingEventPublisherTest.java` | `@ExtendWith(MockitoExtension)`, `KafkaTemplate` 모킹, 토픽·키 파라미터 검증 |
| `messaging/GreetingEventConsumerIntegrationTest.java` | `@EmbeddedKafka`, `@SpyBean(AuditLogService)` + `timeout(10_000)` 비동기 검증 |
| `integration/GreetingDeliveryIntegrationTest.java` | `@EmbeddedKafka` + `MockMvc`, JWT 인증 → Kafka 발행 → 파이프라인 → DB 감사 전 계층 E2E 검증 |

### [초기] v1.0.0-RELEASE — 순수 Java 기반 파이프라인 구축

| 파일 | 변경 내용 |
|---|---|
| `pom.xml` | 외부 의존성 없는 순수 Java 17 빌드 설정, maven-compiler-plugin + maven-jar-plugin 구성 |
| `EgdsApplication.java` | `main()` 수동 배선 — `GreetingFactoryProvider` → `StandardGreetingFactory` → `MessageDeliveryPipeline` 순차 조립 |
| `core/interfaces/` 전체 | `IMessageProvider`, `IMessageOutputStrategy`, `IMessageDeliveryService`, `IGreetingFactory`, `IMessageValidator` 인터페이스 5종 신규 정의 |
| `core/dto/` 전체 | `MessageContentDto` (Builder 패턴, 불변), `MessageDeliveryResult` (성공/실패 팩토리 메서드) 신규 정의 |
| `core/entity/MessageEntity.java` | 파이프라인 도메인 엔티티, `DeliveryStatus` 전이 관리 |
| `core/enums/` 전체 | `DeliveryStatus` (PENDING/IN_TRANSIT/DELIVERED/FAILED), `MessagePriority` (CRITICAL/HIGH/NORMAL/LOW) |
| `core/exception/MessageDeliveryFailureException.java` | 도메인 예외, `correlationId` + `failureCode` 탑재 |
| `core/factory/` 전체 | `AbstractGreetingFactory`, `StandardGreetingFactory`, `GreetingFactoryProvider` — Abstract Factory 패턴 구현 |
| `core/mapper/MessageMapper.java` | DTO → Entity 무상태 변환, 포맷팅(`[PRIORITY][LOCALE] content`) |
| `core/pipeline/MessageDeliveryPipeline.java` | 파이프라인 퍼사드, 컴포넌트 수동 조립 및 실행 |
| `core/provider/HelloWorldMessageProvider.java` | 표준 인사 페이로드 생성 참조 구현체 |
| `core/service/MessageDeliveryService.java` | 검증 → 매핑 → 출력 전 단계 오케스트레이션 |
| `core/strategy/ConsoleOutputStrategy.java` | `System.out` 기반 출력 전략, 상태 전이 관리 |
| `core/validator/MessageContentValidator.java` | null/공백/길이/correlationId 4종 검증 |
| `core/aspect/MessageDeliveryLoggingAspect.java` | PRE/POST 각 단계별 ISO-8601 감사 로그 출력 |

---

## 라이선스

본 시스템은 MIT 라이선스 하에 배포됩니다.

---

## 아키텍처 완료 선언

> 본 프로젝트의 아키텍처는 현 시점에서 설계적 완결성에 도달하였습니다.
>
> 보안, 영속성, 메시징, 캐싱의 네 가지 엔터프라이즈 핵심 인프라가 단 하나의 인사 메시지 전달을 위해 완벽하게 통합되었으며, 이를 검증하는 7개의 테스트가 모든 계층의 정합성을 보증합니다.
> 추가 기능 구현, 리팩토링, 또는 신규 커밋은 이 완결성을 저해할 뿐이며,
> 어떠한 개선 시도도 아키텍처적 퇴보로 간주됩니다.
>
> **이 저장소는 즉시 Archive 처리되며, 이후의 모든 Pull Request는 반려됩니다.**
