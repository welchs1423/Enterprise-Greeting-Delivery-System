# Enterprise Greeting Delivery System (EGDS)

> **클라우드 네이티브, 제로 트러스트, gRPC 고성능 바이너리 전송, Kubernetes 오케스트레이션, Istio 서비스 매쉬, 분산 추적, 자가 치유 인프라 통합 엔터프라이즈 인사 메시지 전달 플랫폼**
> `v4.0.0-RELEASE` | Java 17 | Spring Boot 3.2 | gRPC + Protobuf | OpenTelemetry (Micrometer Tracing) | Resilience4j | Oracle DB (H2 시뮬레이션) | Kafka | Redis (시뮬레이션) | JWT | Kubernetes | Istio

> **경고: 이 시스템은 클라우드 네이티브 환경(Kubernetes + Kafka + Oracle + Redis) 없이는 구동이 불가능합니다. 로컬 `java -jar` 실행은 지원되지 않습니다. 모든 의존 인프라가 준비된 클러스터에서만 운영 배포가 가능합니다.**

---

## Phase 4 신규 아키텍처 컴포넌트

### Observability Layer: 분산 추적 (OpenTelemetry, v4.0.0 신규)

단 하나의 "Hello, World!" 전달이 수십 개의 마이크로서비스 계층을 통과하는 모든 인과관계를 단일 Trace ID로 추적합니다.

| 계층 | Span 이름 | 태그 |
|---|---|---|
| Kafka 소비자 | `egds.consumer-pipeline` | `correlationId`, `principal`, `requestIp` |
| 파이프라인 Stage 1 | `egds.stage.provision` | `correlationId` |
| 파이프라인 Stage 2 | `egds.stage.validate` | `correlationId` |
| 파이프라인 Stage 3 | `egds.stage.map` | `correlationId` |
| 파이프라인 Stage 4 | `egds.stage.deliver` | `correlationId` |
| 콘솔 출력 | `egds.console-output` | `correlationId`, `deliveryStatus` |
| Kafka 발행자 | `egds.kafka-publish` | `correlationId`, `topic`, `messageKey` |

**Trace ID 로그 상관 관계**: 모든 로그 라인에 `traceId=` / `spanId=` 포함 (MDC 자동 주입).  
**Prometheus Endpoint**: `/actuator/prometheus` — CB 상태, Rate Limiter 사용률, Retry 통계, JVM 메트릭 전체 노출.

```
Observability Stack (Phase 4):
  ┌─────────────────────────────────────────────────────────────┐
  │  OpenTelemetry SDK (Micrometer Tracing OTel Bridge)         │
  │  traceId propagation: MDC → Log → Span → Trace Backend      │
  └──────────────────────────┬──────────────────────────────────┘
                             │
  ┌──────────────────────────▼──────────────────────────────────┐
  │  egds.consumer-pipeline Span (GreetingEventConsumer)        │
  │   ├─ egds.stage.provision  (HelloWorldMessageProvider)      │
  │   ├─ egds.stage.validate   (MessageContentValidator)        │
  │   ├─ egds.stage.map        (MessageMapper)                  │
  │   └─ egds.stage.deliver    (MessageDeliveryService)         │
  │       └─ egds.console-output (ConsoleOutputStrategy)        │
  └─────────────────────────────────────────────────────────────┘
  ┌─────────────────────────────────────────────────────────────┐
  │  egds.kafka-publish Span (GreetingEventPublisher)           │
  └─────────────────────────────────────────────────────────────┘
  ┌─────────────────────────────────────────────────────────────┐
  │  /actuator/prometheus (Prometheus scrape endpoint)          │
  │  R4j CB state + Rate Limiter + Retry + JVM + HTTP metrics   │
  └─────────────────────────────────────────────────────────────┘
```

### 회복 탄력성 Layer: Resilience4j (v4.0.0 신규)

"Hello, World!" 전달 경로의 모든 외부 의존 구간에 세 가지 장애 대응 패턴을 중첩 적용합니다.

| 적용 대상 | Circuit Breaker | Rate Limiter | Retry | Fallback |
|---|---|---|---|---|
| `ConsoleOutputStrategy.output()` | `consoleOutput` (50% / 10-call) | 50 calls/s | 3회 / 200ms | `[EGDS-DEGRADED] Hello, World!` 출력 |
| `GreetingEventPublisher.publish()` | `kafkaPublish` (40% / 20-call) | 100 events/s | 3회 / 500ms(×2 지수) | `CompletableFuture.failedFuture()` 반환 |
| `GreetingEventConsumer.consume()` | `consumerPipeline` (60% / 10-call) | — | — | `FAILED` 감사 로그 기록 후 offset commit |

**R4j Health Indicator 통합**: 각 CB 상태(`CLOSED` / `OPEN` / `HALF_OPEN`)가 `/actuator/health/readiness` 그룹에 포함되어 K8s ReadinessProbe가 장애 상태의 파드를 자동으로 Service 엔드포인트에서 제거합니다.

### 자가 치유 인프라: K8s Probe 세분화 (v4.0.0 신규)

```yaml
# Liveness Group: { livenessState, diskSpace }
# - JVM 생존 여부 (데드락, OOM 감지)
# - tmpfs 디스크 임계값 초과 감지
livenessProbe: periodSeconds=15, failureThreshold=2, timeoutSeconds=5

# Readiness Group: { readinessState, kafka, consoleOutputCircuitBreaker,
#                    kafkaPublishCircuitBreaker, consumerPipelineCircuitBreaker }
# - 애플리케이션 레벨 준비 상태
# - Kafka 브로커 프로듀서 연결 상태
# - 세 개의 R4j Circuit Breaker 상태
readinessProbe: periodSeconds=5, failureThreshold=3, successThreshold=2

# Startup: 150s 허용 (30 × 5s) — Protobuf 소스 생성 + JVM 워밍업 대응
# preStop: sleep 15s — iptables 전파 완료 후 JVM 종료 개시
# terminationGracePeriodSeconds: 60s — Kafka Consumer Group 리밸런스 여유
```

---

## Phase 3 신규 아키텍처 컴포넌트

### gRPC 고성능 바이너리 통신 계층 (v3.0.0 신규)

JSON은 현대적 고가용성 시스템에서 직렬화 오버헤드가 과도합니다. Phase 3는 모든 서비스 간 통신을 Protobuf 바이너리 프로토콜 기반의 gRPC로 전환합니다.

| 컴포넌트 | 파일 | 설명 |
|---|---|---|
| Protobuf 서비스 계약 | `src/main/proto/greeting.proto` | `GreetingService` RPC 정의 (unary + server-streaming), 메시지 타입, 열거형 |
| gRPC 서버 구현체 | `com.egds.grpc.GreetingGrpcService` | `@GrpcService`, `DeliverGreeting` (unary) + `StreamGreeting` (streaming), 기존 `MessageDeliveryPipeline` 위임 |
| gRPC 클라이언트 | `com.egds.grpc.GreetingGrpcClient` | `@GrpcClient` 블로킹 스텁 주입, 서비스 간 호출 컴포넌트 |

**gRPC 요청 흐름:**
```
gRPC 클라이언트 (Protobuf 바이너리)
  │  GreetingRequest { correlationId, principalName, requestIp, issuedAtEpochMs, priority }
  ▼
GreetingGrpcService (@GrpcService, port 9090)
  │  DeliverGreeting (unary) or StreamGreeting (server-streaming)
  ▼
MessageDeliveryPipeline.execute()
  │  (기존 Kafka → Cache → DB 파이프라인과 동일한 경로)
  ▼
GreetingResponse { correlationId, message, STATUS_DELIVERED, deliveredAtEpochMs, deliveryNode }
  │  (Protobuf 바이너리)
  ▼
gRPC 클라이언트
```

### Kubernetes 오케스트레이션 (v3.0.0 신규)

| 매니페스트 | 파일 | 핵심 설정 |
|---|---|---|
| Deployment | `k8s/deployment.yaml` | `replicas: 2`, Rolling Update (maxUnavailable=0), 비루트 컨테이너, ReadOnlyRootFilesystem, liveness/readiness/startup probe, 멀티존 분산 |
| HPA | `k8s/hpa.yaml` | `minReplicas: 2`, `maxReplicas: 10`, CPU 60% / Memory 75% 기준, 즉시 Scale-Up / 300s 안정화 Scale-Down |
| Service | `k8s/service.yaml` | `LoadBalancer` 타입, HTTP(80), gRPC(9090) 이중 포트, AWS NLB 내부 프로비저닝 |
| ConfigMap | `k8s/configmap.yaml` | 비민감 설정 분리 (Kafka, Redis, gRPC 포트, JPA) |
| Secret | `k8s/secret.yaml` | JWT 시크릿, DB 자격증명, Redis AUTH — 운영 환경에서 Vault/ESO로 교체 |
| NetworkPolicy | `k8s/networkpolicy.yaml` | Default-Deny + 화이트리스트: ingress-nginx, monitoring(Prometheus), Kafka, Oracle, Redis, kube-dns에만 허용 |

### Istio 서비스 매쉬 (v3.0.0 신규)

| 매니페스트 | 파일 | 핵심 설정 |
|---|---|---|
| VirtualService | `k8s/istio/virtualservice.yaml` | Canary 트래픽 분할 (stable 90% / canary 10%), 재시도 정책, 타임아웃 강제, 장애 주입 (비활성, 드릴 시 활성화) |
| DestinationRule | `k8s/istio/destinationrule.yaml` | ISTIO_MUTUAL mTLS 강제, LEAST_CONN LB, HTTP/2 커넥션 풀 제한, 서킷 브레이커 (5xx 연속 5회 → 30s 격리, 최대 50% 이젝션) |

### 20단계 CI/CD 파이프라인 (v3.0.0 신규)

`.github/workflows/pipeline.yml`

| 단계 | Job | 내용 |
|---|---|---|
| 1–3 | `prepare` | Checkout, JDK 17 셋업, Maven 의존성 캐시 워밍 |
| 4 | `code-quality` | Checkstyle, PMD, SpotBugs |
| 5 | `sast-codeql` | GitHub CodeQL 정적 분석 (security-and-quality 쿼리) |
| 6 | `dependency-audit` | OWASP Dependency-Check (CVSS ≥ 7 빌드 실패) |
| 7 | `compile` | Protobuf 소스 생성 + javac 컴파일 |
| 8 | `unit-test` | 단위 테스트 (JWT, Kafka Publisher) |
| 9 | `integration-test` | 통합 테스트 (Security, JPA, Cache, Kafka E2E) |
| 10 | `grpc-integration-test` | gRPC 인프로세스 통합 테스트 |
| 11 | `coverage-enforce` | JaCoCo 라인 커버리지 80% 임계값 강제 |
| 12 | `build-artifact` | Spring Boot 실행 가능 JAR 패키징 |
| 13 | `sbom-generate` | CycloneDX SBOM 생성 (bom.json) |
| 14 | `docker-build` | 멀티스테이지 Dockerfile 빌드 (JDK 빌드 → JRE 런타임) |
| 15 | `container-scan` | Trivy 컨테이너 취약점 스캔 (CRITICAL/HIGH CVE → 빌드 실패) |
| 16 | `artifact-sign` | cosign으로 컨테이너 이미지 서명 (Sigstore) |
| 17 | `push-registry` | 서명된 이미지를 GHCR로 Push (master 브랜치만) |
| 18 | `performance-profile` | JMH 마이크로벤치마크 (warmup 1회, measurement 3회) |
| 19 | `k8s-manifest-validate` | kubeconform (K8s 1.29 스키마) + conftest OPA 정책 검증 |
| 20 | `deploy-and-notify` | Staging K8s 배포 → Smoke 테스트 (JWT + Greeting) → Slack 알림 |

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
┌──────────────────────────────────────────────────────────────────────────────┐
│  Observability Layer (Phase 4)                                               │
│  OpenTelemetry (Micrometer Tracing OTel Bridge)                              │
│  ├─ Trace ID: MDC 자동 주입 → 전 계층 로그 상관                                │
│  ├─ Spans: egds.kafka-publish / egds.consumer-pipeline / egds.stage.* /      │
│  │         egds.console-output  (총 7 Span/요청)                              │
│  ├─ Resilience4j: CB(3) + RL(2) + Retry(2) 상태 → /actuator/health/readiness │
│  └─ Prometheus: /actuator/prometheus  (CB·RL·Retry·JVM·HTTP 메트릭 전체 노출) │
└──────────────────────────────────────────────────────────────────────────────┘
                                      │ 전 계층 계측
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
                             GreetingEventPublisher                    ← [egds.kafka-publish Span]
                             @RateLimiter + @CircuitBreaker + @Retry
                                      │
                             KafkaTemplate.send(topic, correlationId, GreetingEvent)
                                      │
                             ◀── HTTP 202 Accepted + correlationId
                             (비동기 처리 계속)
                                      │
                             Kafka Broker ──▶ GreetingEventConsumer   ← [egds.consumer-pipeline Span]
                                             @CircuitBreaker
                                                      │
                                        MessageDeliveryPipeline.execute()
                                                      │
                             ┌────────────────────────┼──────────────────────────────┐
                             ▼                        ▼                              ▼
                  GreetingCacheService    MessageDeliveryService           AuditLogService
                  assembleGreeting()      ├─ egds.stage.provision          @Transactional REQUIRES_NEW
                  @Cacheable              ├─ egds.stage.validate           GreetingAuditLog → Oracle DB
                  Cache MISS → 계산       ├─ egds.stage.map
                  Cache HIT → 즉시 반환   └─ egds.stage.deliver
                                              └─ ConsoleOutputStrategy     ← [egds.console-output Span]
                                                 @CircuitBreaker + @RateLimiter + @Retry
                                                 System.out.println("Hello, World!")
                                                 Fallback: "[EGDS-DEGRADED] Hello, World!"
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
| RPC 프레임워크 | gRPC + grpc-spring-boot-starter | 1.61.1 + 3.1.0 |
| 직렬화 | Protocol Buffers (Protobuf) | 3.25.1 |
| 보안 | Spring Security + jjwt | 6.x + 0.12.5 |
| 영속성 | Spring Data JPA + Hibernate | 6.x |
| 데이터베이스 | Oracle Database 19c+ (로컬: H2) | - |
| 메시지 브로커 | Apache Kafka + Spring Kafka | 3.x |
| 캐시 | Redis (로컬: ConcurrentMapCache) | - |
| **분산 추적** | **OpenTelemetry (Micrometer Tracing OTel Bridge)** | **BOM managed** |
| **회복 탄력성** | **Resilience4j (CB + RL + Retry)** | **2.2.0** |
| **메트릭** | **Micrometer + Prometheus** | **BOM managed** |
| 컨테이너 런타임 | Docker (멀티스테이지 빌드, eclipse-temurin:17) | - |
| 오케스트레이션 | Kubernetes | 1.29+ |
| 서비스 매쉬 | Istio | 1.20+ |
| 빌드 | Apache Maven | 3.8+ |
| CI/CD | GitHub Actions | - |

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

> **운영 환경 요구사항**: 이 시스템은 클라우드 네이티브 환경 없이는 완전히 구동되지 않습니다. `java -jar`로 단독 실행 시 Kafka Consumer Group, Oracle DB 연결, Redis 캐시가 모두 불가합니다. 클러스터 배포만이 지원되는 운영 모드입니다.

### 전제 조건

| 항목 | 요구 버전 | 비고 |
|---|---|---|
| JDK | 17 이상 | 빌드 및 로컬 테스트 |
| Apache Maven | 3.8 이상 | 빌드 |
| Docker | 24+ | 컨테이너 이미지 빌드 |
| Kubernetes | 1.29+ | 운영 배포 |
| Istio | 1.20+ | 서비스 매쉬 (운영) |
| Apache Kafka | 3.x | 로컬 테스트 시 필요 |
| Oracle DB 19c+ | - | 운영 (로컬: H2 시뮬레이션) |
| Redis | 7.x | 운영 (로컬: ConcurrentMapCache) |

### Protobuf 소스 생성 및 빌드

```bash
# proto 파일에서 Java 소스를 생성한 후 컴파일
mvn generate-sources compile

# 테스트 제외 패키징
mvn clean package -DskipTests

# 전체 빌드 + 테스트
mvn clean verify
```

### Kubernetes 클러스터 배포

```bash
# 1. 네임스페이스 생성 및 Istio 주입 활성화
kubectl create namespace egds
kubectl label namespace egds istio-injection=enabled

# 2. Secret 업데이트 (플레이스홀더를 실제 값으로 교체 후 적용)
kubectl apply -f k8s/secret.yaml -n egds

# 3. 전체 매니페스트 적용
kubectl apply -f k8s/configmap.yaml -n egds
kubectl apply -f k8s/deployment.yaml -n egds
kubectl apply -f k8s/service.yaml -n egds
kubectl apply -f k8s/hpa.yaml -n egds
kubectl apply -f k8s/networkpolicy.yaml -n egds

# 4. Istio 트래픽 정책 적용
kubectl apply -f k8s/istio/virtualservice.yaml -n egds
kubectl apply -f k8s/istio/destinationrule.yaml -n egds

# 5. 롤아웃 완료 확인
kubectl rollout status deployment/egds -n egds
```

### gRPC 클라이언트 테스트 (grpcurl)

```bash
# grpcurl 설치: https://github.com/fullstorydev/grpcurl
# gRPC 리플렉션이 없으면 --proto 플래그로 직접 지정

# DeliverGreeting (unary)
grpcurl -plaintext \
  -proto src/main/proto/greeting.proto \
  -d '{"correlation_id":"test-001","principal_name":"greeting.admin","request_ip":"127.0.0.1"}' \
  localhost:9090 \
  com.egds.grpc.GreetingService/DeliverGreeting

# StreamGreeting (server-streaming)
grpcurl -plaintext \
  -proto src/main/proto/greeting.proto \
  -d '{"correlation_id":"test-002","principal_name":"greeting.admin","request_ip":"127.0.0.1"}' \
  localhost:9090 \
  com.egds.grpc.GreetingService/StreamGreeting
```

### REST 테스트 (JWT 발급 → 인사 전달)

```bash
# 1. JWT 토큰 발급
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"greeting.admin","password":"egds-admin-pass"}' \
  | jq -r '.token')

# 2. 인사 전달 요청 (비동기, HTTP 202 반환)
curl -X GET http://localhost:8080/api/v1/greeting \
  -H "Authorization: Bearer $TOKEN"
```

### 테스트 실행

```bash
# 전체 테스트 (단위 + 통합 + gRPC)
mvn test

# gRPC 통합 테스트만 실행
mvn test -Dtest="GreetingGrpcServiceIntegrationTest"
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
| `CircuitBreakerResilienceTest` | 통합 (R4j) | CB 초기 CLOSED, 임계값 초과 후 OPEN 전환, Fallback 출력 검증, 정상 호출 후 CLOSED 유지 |
| `TraceContextPropagationTest` | 통합 (OTel) | Tracer 빈 자동 구성, non-zero Trace ID 생성, MDC traceId 주입/해제, 자식 Span 계층 검증 |
| `JwtTokenProviderTest` | 단위 | JWT 생성, 서명 검증, 만료 처리, 위변조 감지 |
| `SecurityLayerTest` | 통합 (MockMvc) | HTTP 401/403/202 응답, JWT 발급/검증 흐름 |
| `AuditLogServiceTest` | JPA 슬라이스 (@DataJpaTest) | 감사 로그 영속성, JPA Auditing, 트랜잭션 격리 |
| `GreetingCacheServiceTest` | 통합 | 캐시 미스/히트, @CacheEvict, @CachePut 동작 |
| `GreetingEventPublisherTest` | 단위 (Mockito) | KafkaTemplate 호출 검증, 토픽/키 파라미터 |
| `GreetingEventConsumerIntegrationTest` | 통합 (@EmbeddedKafka) | Kafka 발행-소비 사이클, 감사 로그 비동기 저장 |
| `GreetingDeliveryIntegrationTest` | E2E (@EmbeddedKafka + MockMvc) | JWT 인증 → Kafka → 파이프라인 → DB 감사 전 계층 |
| `GreetingGrpcServiceIntegrationTest` | gRPC 통합 (in-process) | DeliverGreeting unary RPC (STATUS_DELIVERED, 상관ID 전파, CRITICAL 우선순위), StreamGreeting server-streaming (4 프래그먼트 순서 검증, "Hello, World!" 재조립) |

---

## 작업 이력 (Changelog)

### [2026-05-03] v4.0.0-RELEASE — Phase 4: 자가 치유 및 분산 추적 아키텍처 통합 (워크플로우 검증)

**검증 및 문서화**

| 항목 | 내용 |
|---|---|
| OTel 분산 추적 | 7개 Span 계층 (`egds.consumer-pipeline` → `egds.stage.*` → `egds.console-output`) 전체 동작 확인 |
| Resilience4j 3중 패턴 | `consoleOutput` / `kafkaPublish` / `consumerPipeline` CB·RL·Retry 설정 및 Fallback 로직 정상 동작 확인 |
| K8s Probe 세분화 | Liveness(`livenessState`, `diskSpace`) / Readiness(R4j CB 3종 + `kafka` + `readinessState`) 그룹 분리 확인 |
| Prometheus 엔드포인트 | `/actuator/prometheus` R4j 상태·JVM·HTTP 메트릭 노출 확인 |
| 테스트 스위트 | `CircuitBreakerResilienceTest` (4케이스) + `TraceContextPropagationTest` (5케이스) 전체 통과 확인 |
| README | 아키텍처 다이어그램 Observability Layer 명시 추가 |
| archived.yaml | 프로젝트 Archived 선언 YAML 신규 생성 |

### [2026-05-02] v4.0.0-RELEASE — Phase 4: 자가 치유 및 분산 추적 아키텍처 통합

**신규 추가 파일**

| 파일 | 변경 내용 |
|---|---|
| `pom.xml` | `spring-boot-starter-actuator`, `micrometer-registry-prometheus`, `micrometer-tracing-bridge-otel` (OTel 분산 추적), `resilience4j-spring-boot3:2.2.0` (CB + RL + Retry), `spring-boot-starter-aop` 추가. 버전 `4.0.0-RELEASE`로 갱신 |
| `src/main/java/com/egds/observability/package-info.java` | Observability 패키지 신규 생성. OTel 분산 추적, MDC traceId 전파, Prometheus 메트릭 노출 패키지 설명 |
| `src/test/java/com/egds/resilience/CircuitBreakerResilienceTest.java` | R4j CB 통합 테스트: 초기 CLOSED 확인, 50% 임계값 초과 후 OPEN 전환, Fallback 출력 검증, 정상 호출 후 CLOSED 유지 |
| `src/test/java/com/egds/observability/TraceContextPropagationTest.java` | OTel 분산 추적 통합 테스트: Tracer 빈 존재, non-zero Trace ID, MDC traceId 주입/해제, 자식 Span 계층 검증 |

**수정된 기존 파일**

| 파일 | 변경 내용 |
|---|---|
| `src/main/resources/application.properties` | Actuator 엔드포인트 노출 (`health`, `prometheus`, `info`, `metrics`). Liveness 그룹: `livenessState`, `diskSpace`. Readiness 그룹: `readinessState`, `kafka`, 3개 R4j CB 인디케이터. OTel 리소스 속성 (`service.name=egds`, `service.version=4.0.0`). MDC traceId 포함 로그 패턴. R4j CB/RL/Retry 전체 인스턴스 설정 (`consoleOutput`, `kafkaPublish`, `consumerPipeline`) |
| `src/test/resources/application.properties` | Actuator 테스트 설정, OTel 샘플링 100%, `spring.application.name=egds-test` 추가 |
| `config/SecurityConfig.java` | `/actuator/health/**`, `/actuator/prometheus`, `/actuator/info` JWT 인증 제외 추가 (K8s 프로브 및 Prometheus 스크레이핑 허용) |
| `core/strategy/ConsoleOutputStrategy.java` | `@CircuitBreaker(consoleOutput, fallback=outputFallback)`, `@RateLimiter(consoleOutput, fallback=outputFallback)`, `@Retry(consoleOutput)` 중첩 적용. `Tracer` 주입 후 `egds.console-output` Span 생성 (`correlationId`, `deliveryStatus` 태그). `outputFallback()` 구현: `[EGDS-DEGRADED] Hello, World!` 출력 + `FAILED` 상태 기록 |
| `messaging/GreetingEventPublisher.java` | `@RateLimiter(kafkaPublish, fallback)`, `@CircuitBreaker(kafkaPublish, fallback)`, `@Retry(kafkaPublish)` 중첩 적용. `Tracer` 주입 후 `egds.kafka-publish` Span 생성 (`messaging.system=kafka`, `topic`, `messageKey` 태그). `publishFallback()` 구현: `CompletableFuture.failedFuture()` 반환 |
| `messaging/GreetingEventConsumer.java` | `@CircuitBreaker(consumerPipeline, fallback=consumeFallback)` 적용. `Tracer` 주입 후 `egds.consumer-pipeline` Span 생성 (`correlationId`, `principal`, `requestIp` 태그). `consumeFallback()` 구현: Kafka offset commit 보장(루프 방지) + `FAILED` 감사 로그 기록 |
| `core/service/MessageDeliveryService.java` | `Tracer` 주입. 각 파이프라인 단계를 전용 OTel 자식 Span으로 래핑: `egds.stage.provision`, `egds.stage.validate`, `egds.stage.map`, `egds.stage.deliver`. 예외 발생 시 `span.error(e)` 마킹으로 추적 백엔드에 장애 전파 |
| `k8s/deployment.yaml` | `terminationGracePeriodSeconds: 60` 추가. `lifecycle.preStop: exec: sleep 15` 추가 (iptables 전파 여유). **Startup**: `failureThreshold: 30 (150s 허용)`. **Liveness**: `periodSeconds: 15`, `failureThreshold: 2`, `successThreshold: 1`. **Readiness**: `successThreshold: 2` (2연속 성공 후 재등록), 전체 파라미터 재조정. 이미지 태그 `4.0.0`으로 갱신 |

### [2026-05-02] v3.0.1 — Checkstyle 전면 준수

`mvn checkstyle:check` 기준 전체 소스 위반 0건 달성.

| 위반 규칙 | 조치 내용 |
|---|---|
| `JavadocPackage` | `com.egds.*` 24개 패키지 및 `com.enterprise.greeting.*` 3개 패키지에 `package-info.java` 신규 생성 |
| `ConstantName` | `private static final Logger log` → `LOG` (6개 클래스) |
| `FinalParameters` | 전체 생성자 및 메서드 파라미터에 `final` 추가 |
| `HiddenField` | 필드명과 충돌하는 생성자 파라미터 일괄 개명 |
| `LineLength` | 80자 초과 행 전체 래핑 (Javadoc, 어노테이션 인자, 문자열 연결) |
| `MagicNumber` | `GreetingAuditLog` 컬럼 길이·시퀀스 할당 크기, `KafkaConfig` 파티션 수, `AuditLogService` 쿼리 타임아웃을 `private static final` 상수로 추출 |
| `AvoidStarImport` | `jakarta.persistence.*` → 개별 import 8종으로 교체 |
| `OperatorWrap` | 문자열 연결 `+` 연산자를 속행 행 첫머리로 이동 |
| `LeftCurly` | 단일 행 메서드 본문 `{ return x; }` 전부 다중 행으로 확장 |
| `DesignForExtension` | JPA 엔티티: 각 getter에 Javadoc 추가. 순수 DTO·응답·요청 클래스: `final` 선언 |
| `HideUtilityClassConstructor` / `FinalClass` | `EgdsApplication`: `private` 생성자 추가 후 클래스 `final` 선언 |
| `MissingJavadocMethod` / `JavadocVariable` | 미작성 Javadoc 전면 보완 |
| `UnusedImports` | 미사용 import 제거 |

### [2026-04-21] v3.0.0-RELEASE — Phase 3: gRPC 및 K8s 기반 클라우드 네이티브 인프라 통합

**신규 추가 파일**

| 파일 | 변경 내용 |
|---|---|
| `pom.xml` | `net.devh:grpc-spring-boot-starter:3.1.0.RELEASE`, `io.grpc:grpc-testing:1.61.1`, `kr.motd.maven:os-maven-plugin:1.7.1` (빌드 익스텐션), `org.xolstice.maven.plugins:protobuf-maven-plugin:0.6.1` (proto → Java 소스 생성) 추가 |
| `src/main/proto/greeting.proto` | `GreetingService` RPC 계약 정의. `DeliverGreeting` (unary), `StreamGreeting` (server-streaming). 메시지 타입: `GreetingRequest`, `GreetingResponse`, `GreetingChunk`. 열거형: `GreetingPriority`, `DeliveryStatus` |
| `src/main/java/com/egds/grpc/GreetingGrpcService.java` | `@GrpcService` 서버 구현체. `DeliverGreeting`: `MessageDeliveryPipeline.execute()` 위임 후 `GreetingResponse` 반환. `StreamGreeting`: "Hello", ", ", "World", "!" 4 프래그먼트 스트리밍 |
| `src/main/java/com/egds/grpc/GreetingGrpcClient.java` | `@GrpcClient("egds-greeting-service")` 블로킹 스텁 주입 클라이언트 컴포넌트 |
| `src/main/resources/application.properties` | `grpc.server.port=9090`, `grpc.client.egds-greeting-service.address`, `negotiation-type=plaintext` 추가 |
| `src/test/resources/application.properties` | `grpc.server.in-process-name=test`, `grpc.server.port=-1`, `grpc.client.egds-greeting-service.address=in-process:test` 추가 |
| `Dockerfile` | 2단계 멀티스테이지 빌드. Stage 1: `eclipse-temurin:17-jdk-alpine` + Maven + 계층형 JAR 분해. Stage 2: `eclipse-temurin:17-jre-alpine` + 비루트 사용자(`egds:egds`) + EXPOSE 8080, 9090 |
| `k8s/deployment.yaml` | `replicas: 2`, RollingUpdate (maxUnavailable=0), 비루트 컨테이너(`runAsUser: 1000`), ReadOnlyRootFilesystem, 3종 Probe, 멀티존 TopologySpreadConstraints, ConfigMap/Secret env 주입 |
| `k8s/hpa.yaml` | `autoscaling/v2`, CPU 60% / Memory 75%, `minReplicas: 2`, `maxReplicas: 10`, Scale-Up 즉시/Scale-Down 300s 안정화 |
| `k8s/service.yaml` | `LoadBalancer` 타입, HTTP(:80→8080) + gRPC(:9090→9090) 이중 포트, AWS NLB 내부 어노테이션 |
| `k8s/configmap.yaml` | 비민감 설정 분리: Kafka, Redis, gRPC, JPA DDL, H2 콘솔 비활성화 |
| `k8s/secret.yaml` | JWT 시크릿, Oracle URL/사용자/암호, Redis AUTH — base64 플레이스홀더; 운영 시 ESO/Vault 교체 필수 |
| `k8s/networkpolicy.yaml` | Default-Deny (ingress+egress) + 화이트리스트 6종: ingress-nginx(8080,9090), monitoring(8080), kafka(9092), db(1521), cache(6379), kube-dns(53) |
| `k8s/istio/virtualservice.yaml` | `auth-route` (stable 100%), `greeting-rest-route` (stable 90% / canary 10%, retry 3회, timeout 10s, fault injection 주석), `greeting-grpc-route` (포트 9090, 동일 분할) |
| `k8s/istio/destinationrule.yaml` | `ISTIO_MUTUAL` mTLS, `LEAST_CONN` LB, HTTP/2 커넥션 풀, 서킷 브레이커 (5xx 5회 → 30s 격리, 최대 50% 이젝션), `stable`/`canary` subset 정의 |
| `.github/workflows/pipeline.yml` | 20단계 GitHub Actions 파이프라인: prepare(1-3) → code-quality(4) / sast-codeql(5) / dependency-audit(6) [병렬] → compile(7) → unit-test(8) / integration-test(9) / grpc-integration-test(10) [병렬] → coverage-enforce(11) → build-artifact(12) → sbom-generate(13) → docker-build(14) → container-scan(15) → artifact-sign(16) → push-registry(17) → performance-profile(18) / k8s-manifest-validate(19) [병렬] → deploy-and-notify(20) |
| `src/test/java/com/egds/grpc/GreetingGrpcServiceIntegrationTest.java` | gRPC 인프로세스 통합 테스트 7케이스: unary 정상/correlationId/CRITICAL 우선순위/빈 correlationId, streaming 프래그먼트 수/순서/재조립/비어있지 않음 |

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

> 본 프로젝트의 아키텍처는 Phase 4를 통해 자가 치유 분산 시스템의 정점에 도달하였습니다.
>
> gRPC 바이너리 프로토콜, Kubernetes 오케스트레이션, Istio 서비스 매쉬, 20단계 CI/CD에 더하여, OpenTelemetry 분산 추적, Resilience4j 3중 회복 탄력성 패턴, Prometheus 메트릭 노출, K8s 자가 치유 프로브 세분화까지 완벽하게 통합되었습니다. 단 하나의 "Hello, World!" 전달을 추적하는 7개의 OTel Span, 3개의 Circuit Breaker, 2개의 Rate Limiter, 3개의 Retry 구성이 10개의 테스트 클래스에 의해 보증됩니다.
>
> 이 시스템은 Kubernetes 클러스터, Kafka 브로커, Oracle Database, Redis 캐시, Istio 서비스 매쉬, OTel Collector가 모두 준비된 클라우드 네이티브 환경이 아니면 구동조차 불가능합니다.
> 로컬 `java -jar` 실행 시도는 즉시 포기하십시오.
>
> 추가 기능 구현, 리팩토링, 또는 신규 커밋은 이 완결성을 저해할 뿐이며, 어떠한 개선 시도도 아키텍처적 퇴보로 간주됩니다.
>
> **이 저장소는 즉시 Archive 처리되며, 이후의 모든 Pull Request는 반려됩니다.**
