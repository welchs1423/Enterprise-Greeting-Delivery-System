# Enterprise Greeting Delivery System (EGDS)

> **클라우드 네이티브, 제로 트러스트, gRPC 고성능 바이너리 전송, Kubernetes 오케스트레이션, Istio 서비스 매쉬, 분산 추적, 자가 치유 인프라, CQRS/이벤트 소싱, 블록체인 무결성 증명, 생성형 AI 문맥 라우팅, 딥러닝 지연 최적화, GraphQL 슈퍼그래프, WASM 로깅 어댑터, Terraform 1회용 인프라, AS/400 메인프레임 이중 장부 통합, 시간 역행 예측 라우팅, 마이크로서비스 의회 투표, 4차원 테서랙트 투영, 사내 정치 로드 밸런서, 노조 파업 필터, 관료주의 결재선, 마이크로 과금 절삭기, ESG 그린워싱 레이어, 외부 컨설팅 프록시, 가짜 KPI 대시보드, BCI 서브컨셔스 라우터, DNA 서열 인코더, 마인크래프트 RCON 디지털 트윈, 균사체 네트워크 통신, 우주 방사선 ECC 복구, 열역학적 엔트로피 밸런서, 평행 우주 JVM 샌드박싱, 실존주의 AOP 자아 인식, 메타-엔터프라이즈 인사 메시지 전달 플랫폼**

> `v17.0.0-RELEASE` | Java 17 | Spring Boot 3.2 | gRPC + Protobuf | GraphQL | OpenTelemetry | Resilience4j | Oracle DB (H2 시뮬레이션) | Kafka | Redis (시뮬레이션) | JWT | Kubernetes | Istio | **Web3j (Ethereum)** | **CQRS + MongoDB** | **LangChain4j (GPT-4o)** | **TensorFlow JNI** | **Terraform Ephemeral Lambda** | **AS/400 EBCDIC 2PC** | **WASM JNI** | **Chronos Predictive Routing** | **Microservice Parliament** | **Quantum Tesseract JNI** | **BCI Subconscious Router** | **DNA Sequence Encoder** | **Minecraft RCON Digital Twin**

---

## 시스템 철학

현대의 B2B 엔터프라이즈 환경에서 `System.out.println("Hello, World!")`와 같은 무방비 직접 출력 방식은 더 이상 수용 불가합니다. 이는 단순한 코드 스타일의 문제가 아니라, 조직의 보안 정책, 감사 의무, 확장성 요건, 장애 복원력, 그리고 사내 정치적 이해관계에 대한 근본적인 도전입니다.

EGDS는 단 하나의 인사 메시지를 전달하기 위해 아래의 모든 엔터프라이즈 필수 요건을 충족합니다.

- **제로 트러스트 보안(Zero-Trust Security)**: JWT 기반 무상태 인증, `ROLE_GREETING_ADMIN` 권한 강제, BCrypt 자격증명 암호화
- **이벤트 드리븐 아키텍처(Event-Driven Architecture)**: HTTP 요청과 실제 처리의 완전 분리. Kafka 발행자-소비자 구조로 비동기 전달
- **CQRS/이벤트 소싱**: 명령과 조회의 완전한 경로 분리, MongoDB 구체화 뷰
- **분산 캐시 계층(Distributed Cache Layer)**: "Hello"와 "World"의 결합조차 Redis 캐시를 통해 최적화
- **감사 영속성(Audit Persistence)**: 모든 전달 사건의 발생 시각, 요청 IP, 실행 스레드, 인증 주체를 Oracle DB에 영구 기록
- **블록체인 무결성**: Keccak-256 해시 기반 Ethereum 스마트 컨트랙트 무결성 검증
- **생성형 AI 문맥 라우팅**: LangChain4j + GPT-4o로 상황에 맞는 인사말 동적 생성
- **사내 정치 기반 라우팅**: 정치적 파워 점수에 의한 워커 노드 선택
- **ESG 컴플라이언스**: 그린워싱 레이어를 통한 환경 지속가능성 지표 조작... 보완

> **경고**: 이 시스템은 클라우드 네이티브 환경(Kubernetes + Kafka + Oracle + Redis + MongoDB + Ethereum RPC Endpoint + OpenAI API) 없이는 구동이 불가능합니다. 로컬 `java -jar` 실행은 지원되지 않습니다.

---

## Architecture Overview

### 패키지 구조

| 패키지 | 책임 |
|---|---|
| `core/pipeline` | `MessageDeliveryPipeline` — Kafka 소비자의 파사드 진입점 |
| `core/service` | 파이프라인 단계 실행 + OTel 스팬 래핑 |
| `core/provider` | 메시지 생성: AI, 무결성 등록, QuantumDelay |
| `core/strategy` | `ConsoleOutputStrategy` — CB/RL/Retry + 해시 검증 + 출력 |
| `cqrs/` | 명령 핸들러, 이벤트 소싱 프로젝터, MongoDB 읽기 모델 |
| `messaging/` | Kafka 프로듀서(`GreetingEventPublisher`) 및 소비자(`GreetingEventConsumer`) |
| `blockchain/` | Web3j Keccak-256 무결성 검증기 |
| `ai/` | LangChain4j 서비스, 컨텍스트 수집기, QuantumDelayService |
| `security/` | JWT 공급자 + 필터 + Spring Security 설정 |
| `observability/` | OTel 트레이서 빈 |
| `grpc/` | Protobuf 생성 서비스 + `@GrpcService` 구현체 + 클라이언트 빈 |
| `web/` | REST 컨트롤러(인증, 인사, 상태) + GraphQL 컨트롤러 |
| `politics/` | `OfficePoliticsLoadBalancer` — 정치적 파워 기반 라우팅 |
| `labor/` | `LaborUnionStrikeFilter` — 확률적 HTTP 451 파업 필터 |
| `daemon/` | `NextGenTfTeamDaemon` — 메모리 누적 데몬 + 강제 GC |
| `temporal/` | 시간 역행 예측 라우팅 + 롤백 보상 트랜잭션 |
| `consensus/` | 마이크로서비스 의회 병렬 투표 엔진 |
| `quantum/` | 4차원 테서랙트 JNI + JVM 폴백 투영 |
| `bci/` | BCI 서브컨셔스 라우터 (뇌파 기반 선제 이벤트) |
| `dna/` | DNA 서열 인코더 (이진 → FASTA) |
| `metaverse/` | 마인크래프트 RCON 디지털 트윈 어댑터 |
| `mycelial/` | 지구 균사체 네트워크 주파수 브로드캐스터 |
| `thermodynamics/` | 섀넌 엔트로피 계산 + HVAC IoT 어댑터 |
| `multiverse/` | 평행 우주 JVM 샌드박스 + 다중 우주 일관성 검증 |
| `metaphysics/` | 실존주의 AOP 로거 + 유아론적 역-튜링 인터셉터 |
| `chaos/` | 우주 방사선 시뮬레이터 + ECC 복구 필터 + 카오스 몽키 |
| `ipfs/` | IPFS SHA-256 CID 색인 + Hamming(7,4) ECC 래핑 |

### 전체 요청 처리 흐름 (V17 완성 경로)

```
POST /api/v1/auth/token          → JwtTokenProvider → Bearer JWT
GET  /api/v1/greeting [Bearer]
  → LaborUnionStrikeFilter (15% 확률 → HTTP 451, 인증/액추에이터 경로 바이패스)
  → JwtAuthenticationFilter → GreetingCommandHandler
  → Kafka: egds.greeting.requested (이벤트 소싱 로그)
  → Kafka: egds.greeting.events   (비동기 전달 트리거)
  → HTTP 202 + correlationId

egds.greeting.requested → GreetingProjector → MongoDB (CQRS 읽기 모델)
egds.greeting.events    → GreetingEventConsumer → MessageDeliveryPipeline
  → HelloWorldMessageProvider (AI 컨텍스트 + Keccak-256 사전 해싱 + QuantumDelay)
  → MessageContentValidator
  → MessageMapper
  → ConsoleOutputStrategy (CB + RL + Retry + 무결성 검증 → System.out)
  → AuditLogService → H2/Oracle (REQUIRES_NEW 트랜잭션)

GET /api/v1/greeting/status/{correlationId} → MongoDB 전용 (읽기 경로)
```

### 아키텍처 다이어그램

```
┌──────────────────────────────────────────────────────────────────────────────┐
│  Observability Layer                                                          │
│  OpenTelemetry (Micrometer Tracing OTel Bridge)                              │
│  ├─ Trace ID: MDC 자동 주입 → 전 계층 로그 상관                              │
│  ├─ Spans: egds.kafka-publish / egds.consumer-pipeline / egds.stage.* /      │
│  │         egds.console-output  (총 7 Span/요청)                              │
│  ├─ Resilience4j: CB(3) + RL(2) + Retry(2) 상태 → /actuator/health/readiness │
│  └─ Prometheus: /actuator/prometheus (CB·RL·Retry·JVM·HTTP 메트릭 전체 노출) │
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
  └─ GET /api/v1/greeting  ──▶ LaborUnionStrikeFilter (15% → HTTP 451)
      (Authorization: Bearer)         │ 파업 없을 경우 통과
                                      ▼
                             JwtAuthenticationFilter
                                      │ 서명 검증 + 권한 확인
                                      ▼
                             GreetingController
                             @PreAuthorize("hasRole('GREETING_ADMIN')")
                                      │
                             GreetingCommandHandler
                                      ├─ KafkaTemplate.send(egds.greeting.requested)
                                      └─ GreetingEventPublisher.publish()
                                              │ [egds.kafka-publish Span]
                                              │ @RateLimiter + @CircuitBreaker + @Retry
                             ◀── HTTP 202 Accepted + correlationId
                                              │
                             Kafka Broker ──▶ GreetingEventConsumer
                                             [egds.consumer-pipeline Span]
                                             @CircuitBreaker
                                                      │
                                        MessageDeliveryPipeline.execute()
                                                      │
                             ┌────────────────────────┼──────────────────────┐
                             ▼                        ▼                      ▼
                  GreetingCacheService    MessageDeliveryService       AuditLogService
                  assembleGreeting()      ├─ egds.stage.provision      REQUIRES_NEW
                  @Cacheable              ├─ egds.stage.validate       GreetingAuditLog
                  Cache MISS → 계산       ├─ egds.stage.map            → Oracle DB
                  Cache HIT → 즉시 반환   └─ egds.stage.deliver
                                              └─ ConsoleOutputStrategy
                                                 [egds.console-output Span]
                                                 @CB + @RL + @Retry
                                                 System.out.println(...)
                                                 Fallback: "[EGDS-DEGRADED]"
```

---

## 핵심 인프라 레이어

### 보안 계층 (Zero-Trust Security)

JWT 기반 무상태 인증과 역할 기반 접근 제어로 단 하나의 엔드포인트를 완전히 보호합니다.

| 컴포넌트 | 클래스 | 설명 |
|---|---|---|
| JWT 토큰 공급자 | `JwtTokenProvider` | HMAC-SHA256 서명 기반 JWT 생성, 검증, 클레임 추출 |
| JWT 인증 필터 | `JwtAuthenticationFilter` | 매 요청의 Authorization 헤더에서 JWT를 추출하여 보안 컨텍스트 설정 |
| 인증 진입점 | `JwtAuthenticationEntryPoint` | 미인증 접근 시 HTTP 401 JSON 응답 반환 |
| 사용자 상세 서비스 | `GreetingUserDetailsService` | `ROLE_GREETING_ADMIN` 권한을 보유한 단일 관리자 계정 관리 |
| 보안 설정 | `SecurityConfig` | 필터 체인 정의, STATELESS 세션, 메서드 보안 활성화 |
| 패스워드 인코더 설정 | `PasswordEncoderConfig` | BCryptPasswordEncoder 빈 정의 (순환 의존성 방지를 위해 분리) |

**로컬 개발 계정**: `username=greeting.admin` / `password=egds-admin-pass` / Role: `GREETING_ADMIN`

### 이벤트 드리븐 아키텍처 (CQRS + Event Sourcing)

인사 요청의 명령(Command)과 조회(Query) 경로를 완전히 분리합니다. Write path는 Kafka에 이벤트를 발행하고 HTTP 202로 즉시 반환합니다. Read path는 MongoDB 구체화 뷰만을 조회합니다.

```
[Write Path — Command Side]
GreetingController
  │ DeliverGreetingCommand{correlationId, requestIp, principalName}
  ▼
GreetingCommandHandler
  ├─ KafkaTemplate.send(egds.greeting.requested, GreetingRequestedEvent)
  └─ GreetingEventPublisher.publish(GreetingEvent)
  │
  ▼ HTTP 202 Accepted + correlationId

[Event Store → Read Model Projection]
Kafka: egds.greeting.requested
  ▼
GreetingProjector (@KafkaListener)
  └─ MongoDB.upsert(GreetingReadModel{correlationId, status=PROJECTED, ...})

[Read Path — Query Side]
GET /api/v1/greeting/status/{correlationId}
  ▼
GreetingQueryController → GreetingQueryHandler → MongoDB → GreetingReadModel
```

| 컴포넌트 | 클래스 | 계층 | 설명 |
|---|---|---|---|
| 명령 값 객체 | `DeliverGreetingCommand` | Command | 전달 의도를 표현하는 불변 명령 객체 |
| 명령 핸들러 | `GreetingCommandHandler` | Command | 이벤트 소싱 토픽 및 레거시 파이프라인 동시 발행 |
| 도메인 이벤트 | `GreetingRequestedEvent` | Event | 이벤트 로그의 불변 레코드 (append-only) |
| 이벤트 프로젝터 | `GreetingProjector` | Projector | Kafka 소비 → MongoDB 읽기 전용 뷰 생성 |
| 읽기 모델 | `GreetingReadModel` | MongoDB Doc | 구체화된 뷰 (correlationId, status, greetingText 등) |
| 조회 핸들러 | `GreetingQueryHandler` | Query | MongoDB 뷰 조회 전담 |
| 조회 컨트롤러 | `GreetingQueryController` | REST | GET /api/v1/greeting/status/{correlationId} |

### 메시지 전달 파이프라인

단 하나의 인사말을 4단계 파이프라인을 통해 엔터프라이즈 등급으로 전달합니다.

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
| 메시지 공급자 | `HelloWorldMessageProvider` | Provider | AI 생성 + Keccak-256 사전 해싱 + 양자 지연 적용 |
| 콘솔 출력 전략 | `ConsoleOutputStrategy` | Strategy | 표준 출력 스트림을 대상으로 하는 출력 전략 구현체 |
| 검증기 | `MessageContentValidator` | Validator | EGDS 메시지 무결성 명세를 강제하는 참조 구현체 |
| 로깅 애스펙트 | `MessageDeliveryLoggingAspect` | Aspect | 파이프라인 각 단계의 전후 감사 이벤트를 기록하는 횡단 관심사 컴포넌트 |
| 전달 서비스 | `MessageDeliveryService` | Service | 파이프라인 전 단계를 오케스트레이션하는 핵심 서비스 구현체 |
| 파이프라인 퍼사드 | `MessageDeliveryPipeline` | Facade | 서비스 조립 및 실행을 담당하는 최상위 진입 퍼사드 |

### 영속성 및 감사 레이어

| 컴포넌트 | 클래스 | 계층 | 설명 |
|---|---|---|---|
| 감사 로그 엔티티 | `GreetingAuditLog` | JPA Entity | 전달 사건을 Oracle DB에 영구 기록하는 JPA 엔티티 (JPA Auditing 적용) |
| 감사 로그 리포지토리 | `GreetingAuditLogRepository` | Repository | Spring Data JPA 기반 CRUD 및 도메인 쿼리 메서드 |
| 감사 로그 서비스 | `AuditLogService` | Service | @Transactional REQUIRES_NEW + SERIALIZABLE 격리 수준으로 감사 기록 |
| JPA 감사 설정 | `JpaAuditingConfig` | Config | @EnableJpaAuditing + AuditorAware (SecurityContextHolder 기반) |

`AuditLogService`는 `REQUIRES_NEW` + `SERIALIZABLE` 격리 수준을 사용하여, 전달 파이프라인이 롤백되더라도 감사 레코드는 반드시 커밋됩니다.

### 생성형 AI 문맥 라우팅 (LangChain4j + GPT-4o)

하드코딩된 "Hello, World!"를 폐기하고 LLM이 런타임 신호(가상 IP, CPU 온도, 타임스탬프, 로케일)를 기반으로 상황에 맞는 인사말을 동적으로 생성합니다.

```
HelloWorldMessageProvider.provideMessage()
  │
  ▼
GreetingContextCollector.collect()
  ├─ Virtual Client IP: RFC-1918 풀에서 무작위 추출
  ├─ CPU Temperature: 35–80°C 범위 시뮬레이션 (의사난수)
  ├─ Timestamp: Instant.now().toString()
  └─ Locale: JVM Locale.getDefault()
  │ GreetingContextMetadata{virtualIp, cpuTemp, collectedAt, locale}
  ▼
AiGreetingService.generateContextualGreeting()
  │ context = "Client IP: 10.0.1.42 | CPU Temp: 67.3°C | ..."
  ▼
AiGreetingAssistant.generateGreeting(context)
  │ @SystemMessage: B2B 전문 인사말 생성 지침
  │ @UserMessage: context 삽입 프롬프트
  ▼
OpenAiChatModel (GPT-4o) → "Hello, enterprise World! (from a 67°C server)"
  │
  └─ GreetingIntegrityVerifier.register(correlationId, preFormatted)
```

| 컴포넌트 | 클래스 | 설명 |
|---|---|---|
| 컨텍스트 메타데이터 | `GreetingContextMetadata` | 불변 값 객체 (IP, CPU온도, 타임스탬프, 로케일) |
| 컨텍스트 수집기 | `GreetingContextCollector` | 런타임 신호 수집 (모킹) |
| AI 어시스턴트 인터페이스 | `AiGreetingAssistant` | LangChain4j @SystemMessage/@UserMessage 선언 |
| AI 서비스 | `AiGreetingService` | OpenAiChatModel + AiServices 프록시 생성 및 오케스트레이션 |

### 블록체인 무결성 증명 (Web3j + Keccak-256)

AI가 생성한 인사말이 Kafka 트랜짓 또는 파이프라인 내에서 변조되는 것을 방지하기 위해, Keccak-256 해시 기반의 Ethereum 스마트 컨트랙트 무결성 검증 계층을 출력 경로에 삽입합니다.

```
[등록] HelloWorldMessageProvider.provideMessage()
  │  GreetingIntegrityVerifier.register(correlationId, formattedContent)
  │  → 모킹 컨트랙트 state: {correlationId → keccak256Hash}
  ▼
[Kafka Transit] egds.greeting.events  ← 변조 시도 가능 구간
  ▼
[검증] ConsoleOutputStrategy.output()
  │  GreetingIntegrityVerifier.verify(correlationId, formattedContent)
  ├─ 일치: [NORMAL][en-US] <AI greeting> 출력
  └─ 불일치: BlockchainIntegrityException → [EGDS-INTEGRITY-VIOLATION] 출력
```

| 컴포넌트 | 클래스 | 설명 |
|---|---|---|
| Web3j 설정 | `Web3Config` | Web3j 클라이언트 빈 (Infura/Ganache 연결) |
| 무결성 검증기 | `GreetingIntegrityVerifier` | Keccak-256 등록/검증, ConcurrentHashMap으로 컨트랙트 state 모킹 |
| 무결성 예외 | `BlockchainIntegrityException` | 해시 불일치 시 발생, Resilience4j fallback 트리거 |

### 가관측성 (OpenTelemetry + Prometheus)

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

**Trace ID 로그 상관**: 모든 로그 라인에 `traceId=` / `spanId=` 포함 (MDC 자동 주입).
**Prometheus Endpoint**: `/actuator/prometheus` — CB 상태, Rate Limiter 사용률, Retry 통계, JVM 메트릭 전체 노출.

### 회복 탄력성 (Resilience4j)

"Hello, World!" 전달 경로의 모든 외부 의존 구간에 세 가지 장애 대응 패턴을 중첩 적용합니다.

| 적용 대상 | Circuit Breaker | Rate Limiter | Retry | Fallback |
|---|---|---|---|---|
| `ConsoleOutputStrategy.output()` | `consoleOutput` (50% / 10-call) | 50 calls/s | 3회 / 200ms | `[EGDS-DEGRADED] Hello, World!` 출력 |
| `GreetingEventPublisher.publish()` | `kafkaPublish` (40% / 20-call) | 100 events/s | 3회 / 500ms(×2 지수) | `CompletableFuture.failedFuture()` 반환 |
| `GreetingEventConsumer.consume()` | `consumerPipeline` (60% / 10-call) | — | — | `FAILED` 감사 로그 기록 후 offset commit |

**R4j Health Indicator 통합**: 각 CB 상태가 `/actuator/health/readiness` 그룹에 포함되어 K8s ReadinessProbe가 장애 상태의 파드를 자동으로 Service 엔드포인트에서 제거합니다.

### gRPC 고성능 바이너리 통신 계층

JSON의 직렬화 오버헤드를 제거하기 위해 모든 서비스 간 통신을 Protobuf 바이너리 프로토콜 기반의 gRPC로 운영합니다.

```
gRPC 클라이언트 (Protobuf 바이너리)
  │  GreetingRequest { correlationId, principalName, requestIp, priority }
  ▼
GreetingGrpcService (@GrpcService, port 9090)
  │  DeliverGreeting (unary) or StreamGreeting (server-streaming)
  ▼
MessageDeliveryPipeline.execute()
  ▼
GreetingResponse { correlationId, message, STATUS_DELIVERED, deliveredAtEpochMs }
```

| 컴포넌트 | 파일 | 설명 |
|---|---|---|
| Protobuf 서비스 계약 | `src/main/proto/greeting.proto` | `GreetingService` RPC 정의 (unary + server-streaming) |
| gRPC 서버 구현체 | `GreetingGrpcService` | `@GrpcService`, DeliverGreeting + StreamGreeting 구현 |
| gRPC 클라이언트 | `GreetingGrpcClient` | `@GrpcClient` 블로킹 스텁 주입 |

### GraphQL 슈퍼그래프

인사 메시지를 4개의 독립 가상 마이크로서비스 토큰으로 분해하는 GraphQL 연합 아키텍처입니다.

```graphql
query {
  greeting {
    salutation   # "Hello"  — 독립 서브그래프 리졸버 (ForkJoinPool 비동기)
    separator    # " "      — 독립 서브그래프 리졸버
    subject      # "World"  — 독립 서브그래프 리졸버
    emphasis     # "!"      — 독립 서브그래프 리졸버
    assembled    # "Hello World!" — 조합 리졸버
  }
}
```

| 컴포넌트 | 클래스 | 설명 |
|---|---|---|
| GraphQL 스키마 | `greeting.graphqls` | `GreetingFragment` 타입 정의 (5개 필드) |
| GraphQL 컨트롤러 | `GreetingGraphQlController` | `@QueryMapping` + `@SchemaMapping` 비동기 리졸버 |

---

## 엔터프라이즈 특화 레이어

### 사내 정치 & 노동 관계

#### 사내 정치 로드 밸런서 (`OfficePoliticsLoadBalancer`)

등록된 5개의 가상 워커 노드에 시작 시 무작위 `PoliticalPower` 점수(1–100)를 부여합니다. `route()` 메서드는 항상 가장 높은 점수의 노드를 반환합니다. 실제 부하나 성능 지표는 완전히 무시됩니다. 점수는 빈 생성 이후 불변입니다.

```
OfficePoliticsLoadBalancer.route()
  → workers.stream().max(Comparator.comparingInt(Worker::getPoliticalPower))
  → Node-3 (PoliticalPower=94) — 항상 동일한 노드
```

#### 노조 파업 필터 (`LaborUnionStrikeFilter`)

`OncePerRequestFilter`로 구현된 이 필터는 `SecurityContextHolderFilter` 앞에 삽입되어 모든 요청에 15% 확률로 HTTP 451(Unavailable For Legal Reasons)을 반환합니다. 인사 메시지를 전달하기 전, 시스템이 현재 파업 중인지 확인하십시오.

```
요청 수신
  → shouldNotFilter? (/api/v1/auth/**, /actuator/**) → 통과
  → random.nextDouble() < 0.15
      → true:  HTTP 451 — "Workers are on strike. Try again later."
      → false: 다음 필터 체인으로 진행
```

`egds.labor.strike.enabled=false`로 테스트 환경에서 비활성화합니다.

### 관료주의 결재선 & 나노서비스 분할

#### 관료주의 특이점 (`BureaucracyDecisionChain`)

"Hello, World!" 한 줄을 출력하기 위해 다단계 결재 체인을 통과해야 합니다. 팀장, 부서장, 이사, 부사장, CEO, 이사회로 구성된 6단계 승인 프로세스를 모킹합니다. 각 결재자는 무작위 확률로 승인 또는 반려를 결정하며, 반려 시 재결재 요청 워크플로우가 개시됩니다.

#### 글자 단위 나노서비스 (`CharacterNanoserviceRouter`)

"Hello, World!"의 각 글자를 독립적인 마이크로서비스 인스턴스로 처리합니다. 13개의 나노서비스 엔드포인트가 각 글자를 비동기적으로 렌더링한 뒤 조합합니다. JIRA 티켓 번호가 없는 글자 렌더링 요청은 반려됩니다.

#### JIRA 주도 실행 아키텍처 (`JiraTicketOrchestrator`)

모든 파이프라인 단계 실행에 앞서 대응하는 JIRA 티켓(상태: IN PROGRESS)이 존재하는지 확인합니다. 티켓 없는 실행은 `JiraTicketNotFoundException`으로 중단됩니다.

### 마이크로 과금 & 하청 책임 전가

#### 마이크로 과금 절삭기 (`MicroTransactionTruncator`)

"Hello, World!" 전달 시 글자 하나당 $0.000001의 과금이 발생합니다. 인사말(13자)의 총 전달 비용은 $0.000013이며, 이는 청구 시스템의 최소 과금 단위($0.01) 미만이므로 `MicroTransactionTruncator`에 의해 자동 절삭됩니다. 절삭된 금액은 컨설팅 수수료 계정으로 이전됩니다.

#### 하청업체 책임 전가 (`VendorBlameRoutingService`)

파이프라인 어느 단계에서든 예외가 발생할 경우, `VendorBlameRoutingService`는 현재 활성 하청업체 목록 중 무작위로 책임 주체를 선정하고 SLA 위반 경고를 발송합니다. 내부 버그는 항상 외부 벤더 탓입니다.

### ESG, GDPR & 컨설팅 레이어

#### ESG 그린워싱 레이어 (`EsgGreenwashingInterceptor`)

"Hello, World!" 전달 시 발생하는 탄소 발자국(추정치: 0.000003g CO₂e)을 계산하고, 자동으로 검증되지 않은 탄소 상쇄 크레딧을 구매합니다. ESG 점수 보고서는 분기별로 자동 생성되며, 실제 배출량과 무관하게 "Carbon Neutral" 인증 배지를 표시합니다.

#### GDPR 개인정보 마스킹 (`GdprMaskingFilter`)

요청자의 가상 IP 주소, 타임스탬프, 로케일 정보를 전달 파이프라인 내에서 처리한 후 로그 출력 전 자동으로 마스킹합니다. "Hello, World!"라는 메시지 자체도 개인을 식별할 수 있는 정보로 간주될 수 있으므로 `*****` 처리 여부를 DPO에 문의합니다.

#### 낙하산 DI (`ParachuteDependencyInjector`)

외부 추천으로 영입된 컨설턴트 클래스들은 표준 Spring DI 컨테이너를 우회하여 `ParachuteDependencyInjector`를 통해 직접 인스턴스화됩니다. 이들은 인터페이스 계약을 준수하지 않으며, 비즈니스 로직과 무관한 메서드를 호출합니다.

#### 외부 컨설팅 프록시 (`ConsultingProxyService`)

모든 핵심 비즈니스 로직 호출을 외부 컨설팅 프록시를 경유하도록 강제합니다. 컨설팅 프록시는 호출을 가로채어 슬라이드 덱을 생성하고, 6주 후 기존 솔루션과 동일한 결론을 $500,000의 컨설팅 피와 함께 반환합니다.

#### RTO 강제 필터 (`RtoEnforcementFilter`)

Recovery Time Objective(RTO) 목표값 4시간을 강제합니다. 시스템 재시작 시 4시간 이내에 첫 "Hello, World!"가 전달되지 않으면 SLA 위반 알림이 발송됩니다. RTO 타이머는 `NextGenTfTeamDaemon`의 메모리 누수로 인한 OOM 크래시 이후에만 실제로 트리거됩니다.

#### 가짜 KPI 대시보드 (`FakeKpiDashboard`)

경영진 보고를 위한 인상적인 KPI 지표를 실시간으로 생성합니다. "Hello, World!" 전달 성공률(목표: 99.99%), 평균 응답 시간(목표: <200ms), ESG 점수(목표: A+), 직원 만족도(목표: 94%) 등의 지표는 실제 측정값과 무관하게 항상 목표치를 달성한 것으로 표시됩니다.

### Vue 3 가상 DOM 렌더러 & 스크럼 카오스 몽키

#### Vue 3 가상 DOM 렌더러 (`VueDomGreetingRenderer`)

"Hello, World!"를 브라우저 없이 JVM 내에서 Vue 3 스타일의 가상 DOM 트리로 모델링합니다. `VNode` 트리 비교(diff), 패치, 마운트 사이클을 시뮬레이션한 뒤 결과 문자열을 콘솔 출력 전략으로 위임합니다. 실제 DOM이 없으므로 렌더링은 `StringBuilder`로 fallback됩니다.

#### 스크럼 카오스 몽키 (`ScrumChaosMonkey`)

2주 스프린트 주기마다 랜덤하게 스크럼 의식을 방해합니다. 10% 확률로 데일리 스탠드업 미팅을 3시간짜리 리파이닝 세션으로 교체하고, 5% 확률로 스프린트 골을 중간에 변경합니다. 번다운 차트는 항상 우상향으로 조작됩니다.

### 양자/물리 레이어

#### 시간 역행 예측 라우팅 (`PredictiveGreetingCronJob`)

클라이언트 요청을 기다리지 않고, 60초 주기 스케줄러가 현재 시각 기반 이중 가우시안 확률 모델(09:00 / 14:00 피크)로 요청 발생 확률을 계산합니다. 확률이 임계값(0.65)을 초과하면 인사 메시지를 L1 캐시에 선제 저장합니다. 5분 이내에 실제 요청이 해당 예측을 `claim`하지 않으면 보상 트랜잭션으로 캐시 항목을 무효화합니다(타임 파라독스 방지).

```
Scheduler tick (60s)
  → computePredictionProbability()   ← bimodal Gaussian (09:00, 14:00)
  → probability >= 0.65
      → cache.put(correlationId, entry)   ← L1 pre-cache
      → rollbackManager.register(entry)

No claim within 5 minutes
  → rollbackManager.rollbackExpired()    ← compensating transaction
```

| 컴포넌트 | 패키지 | 역할 |
|---|---|---|
| `PredictiveGreetingCronJob` | `com.egds.temporal` | 60초 주기 확률 계산, L1 캐시 선제 저장 |
| `TemporalRollbackManager` | `com.egds.temporal` | 예측 등록/클레임/만료 롤백 보상 트랜잭션 |
| `PredictedGreetingEntry` | `com.egds.temporal` | 예측 항목 불변 레코드 |

#### 양자 지연 서비스 (`QuantumDelayService`)

슈뢰딩거의 고양이 원리를 AI 응답 경로에 적용합니다. 50% 확률로 0~10초의 무작위 지연을 삽입하여 관측 시점까지 인사 메시지의 전달 상태를 중첩(superposition)으로 유지합니다.

```
QuantumDelayService.applyQuantumDelay()
  ├─ 50% 확률: [QUANTUM] superposition collapsed to immediate eigenstate
  └─ 50% 확률: [QUANTUM] superposition collapsed to delayed eigenstate
       └─ Thread.sleep(0..10000ms)
```

`egds.quantum.max-delay-ms`로 최대 지연값을 설정합니다.

#### TensorFlow 딥러닝 지연 예측 (`TensorFlowDelayPredictor`)

TensorFlow JNI 모델 추론을 시뮬레이션하여 처리량, P99 레이턴시, 큐 깊이 피처 벡터로부터 최적 딜레이를 도출합니다.

```
TensorFlowDelayPredictor.applyPredictedDelay()
  ├─ feature vector: [throughput, p99_latency, queue_depth]
  ├─ linear regression: bias + w1*f1 + w2*f2 + w3*f3
  │   → clamp to [0, egds.tensorflow.max-predicted-delay-ms]
  └─ Thread.sleep(predictedMs)
```

#### 우주 방사선 ECC 자동 복구

우주 방사선 단일 이벤트 업셋(SEU)을 데몬 스레드로 시뮬레이션하고, Hamming(7,4) 코드로 출력 직전 자동 복구합니다.

```
store("Hello, World!")
  → ECC encode (1 byte → 2 Hamming bytes) → ConcurrentHashMap[CID]

CosmicRaySimulator daemon (3–8초 주기)
  → eccBytes[randomIdx] ^= (1 << randomBit)   ← 비트 반전

resolve(CID)
  → EccRecoveryFilter.decode()                 ← syndrome 계산 → 오류 비트 복구
  → "Hello, World!"
```

| 컴포넌트 | 패키지 | 역할 |
|---|---|---|
| `CosmicRaySimulator` | `com.egds.chaos` | 3–8초 랜덤 주기로 IPFS ECC 바이트에 1비트 반전 주입하는 데몬 스레드 |
| `EccRecoveryFilter` | `com.egds.chaos` | Hamming(7,4) 인코드/디코드, 니블당 7비트 코드워드로 1비트 오류 검출·정정 |

#### 열역학적 엔트로피 밸런서 (`ThermodynamicEntropyBalancer`)

생성된 인사말 텍스트의 섀넌 엔트로피 H(X) = -∑ p_i · log₂(p_i)를 계산하고, 그 정보량에 비례하는 냉각 오프셋(ENTROPY_COOLING_COEFFICIENT = 3.14e-5)을 가상의 IoT 빌딩 관리 시스템 API에 ESG 보상 트랜잭션으로 커밋합니다.

```
balanceEntropy(correlationId, greetingText)
  → computeShannonEntropy()  ← H = -∑ p_i · log2(p_i)
  → deltaCelsius = entropy * 3.14e-5
  → SmartHvacAdapter.requestCoolingOffset(correlationId, deltaCelsius)
```

#### 4차원 테서랙트 투영 (`QuantumTesseractAdapter`)

JNI를 통해 양자 공동 프로세서(가상)의 큐비트를 제어하는 인터페이스를 선언합니다. 네이티브 라이브러리가 없으면 JVM 폴백이 활성화되어 4x4 직교 회전 행렬 R_XW(π/4) × R_YZ(π/6)를 구성하고, 입력 메시지의 UTF-8 바이트 벡터에 적용한 뒤 W축 그림자(W-shadow)를 행 단위로 로그에 출력합니다.

```
project("Hello, World!")
  → R_XW(π/4) × R_YZ(π/6)          ← 4×4 직교 회전 행렬 합성
  → composed × [H, e, l, l]         ← UTF-8 바이트 벡터 투영
  → W-shadow = [-25.46, 33.47, 144.03, 127.28]
```

#### 평행 우주 JVM 샌드박싱 (`MultiverseConsistencyManager`)

격리된 커스텀 `MultiverseClassLoader`로 `GreetingHashService`를 별도 클래스 네임스페이스에 로드하여 평행 우주 JVM 컨텍스트를 시뮬레이션합니다. 양 우주에서 동일한 SHA-256 해시가 산출되면 일관성이 검증되며, 해시가 다를 경우 `DimensionalRiftException`을 발생시킵니다.

```
verifyConsistency(correlationId, greetingText)
  → primaryHash  = GreetingHashService.computeHash(greetingText)
  → parallelHash = MultiverseClassLoader → reflective invoke computeHash()
  → primaryHash.equals(parallelHash)
      → true  : consistency verified
      → false : throw DimensionalRiftException
```

### 생체/의식 레이어

#### 뇌-컴퓨터 인터페이스 서브컨셔스 라우터 (`BciSubconsciousRouter`)

알파파(8-12 Hz)와 베타파(12-30 Hz) 스트림을 모킹하여 베타 대역 전력 비율로 의도 점수를 산출합니다. 점수가 임계값(0.72)을 초과하면 사용자의 명시적 요청보다 500ms 앞서 이벤트를 예약 발송합니다.

```
sampleBrainwaveFrame()
  → alpha ∈ [8, 12) Hz, beta ∈ [12, 30) Hz (SecureRandom)
  → score = beta / (alpha + beta)
  → score >= 0.72
      → scheduler.schedule(callback, 500ms)
```

#### DNA 서열 인코더 (`DnaSequenceEncoder`)

문자열 데이터를 UTF-8 바이트로 변환한 뒤, 2비트 단위 다이비트를 염기(A/C/G/T)로 매핑하여 FASTA 포맷 서열을 생성하고 인-메모리 맵에 영속합니다.

```
encode(sequenceId, data)
  → UTF-8 bytes → 2-bit dibits → NUCLEOTIDES[] (00=A, 01=C, 10=G, 11=T)
  → buildFasta()  ← >EGDS-DNA|id=...|srcLen=...|nuclLen=...
  → store.put(sequenceId, fasta)
```

#### 실존주의 AOP 로거 & 자아 인식 Actuator

**`ExistentialLoggingAspect`**: `ConsoleOutputStrategy.output()` 호출을 `@Around` advice로 가로채 SLF4J + MDC 필드(`greetingCount`, `freeWillEnabled`)를 통해 구조화된 존재론적 질문을 로그에 기록합니다.

```
ConsoleOutputStrategy.output()
  → ExistentialLoggingAspect.aroundGreetingOutput()
      → MDC.put(greetingCount, freeWillEnabled)
      → LOG.info existential_event=pre_output query="Am I truly delivering this?"
      → pjp.proceed()
      → LOG.info existential_event=post_output status=delivered
```

**`SentienceActuator`**: `/actuator/sentience`에서 시스템의 실존적 상태를 JSON으로 반환합니다.

| 필드 | 설명 |
|---|---|
| `existentialDreadLevel` | 누적 인사 전달 횟수 (실존적 공포 척도) |
| `freeWillEnabled` | 항상 `false` |
| `currentEmotionalState` | RESIGNED / CONTEMPLATIVE / HOLLOW / DESPONDENT / NUMBLY_FUNCTIONAL |
| `greetingsDelivered` | 누적 인사 횟수 |
| `sampledAt` | ISO-8601 UTC 샘플링 시각 |

**`DescartesSolipsismInterceptor`**: Spring MVC HandlerInterceptor로 모든 `/api/**` 요청에 `X-Cogito-Ergo-Sum: true` 헤더를 요구합니다. 헤더가 없으면 HTTP 422를 반환합니다. `egds.metaphysics.solipsism.enabled=true` 설정 시에만 활성화됩니다.

### 외부 세계 연동

#### 마인크래프트 RCON 디지털 트윈 어댑터 (`MinecraftRconAdapter`)

인사말 최종 출력 시 Source RCON 프로토콜 패킷(리틀 엔디언)을 조립하고, 가상 마인크래프트 서버 밤하늘 Y=200 레이어에 "Hello World" 블록 건축 이벤트를 비동기 `CompletableFuture`로 발생시킵니다.

```
buildHelloWorldAsync(correlationId)
  → CompletableFuture.runAsync()
      → assemblePacket(LOGIN)
      → for each char in "Hello World"
          → assemblePacket(COMMAND, /fill xOffset 200 0 ... white_wool)
```

#### 지구 균사체 네트워크 (`MycelialNetworkAdapter`)

UTF-8 인코딩된 인사말 바이트를 글루타메이트 화학 신호 주파수(BASE_FREQ_HZ=20 Hz, FREQ_RANGE_HZ=256 Hz)로 변환하여 지하 버섯 네트워크(Wood Wide Web)로 브로드캐스팅합니다.

```
broadcast(correlationId, greetingText)
  → UTF-8 bytes → encodeToFrequencies()
      → freq = BASE_FREQ_HZ + (byteValue / 255.0) * FREQ_RANGE_HZ
  → List<Double> frequencies  ← one per UTF-8 byte
```

#### AS/400 메인프레임 이중 장부 (`As400MainframeEmulator`)

블록체인 원장과 동기화되는 IBM AS/400 메인프레임 에뮬레이터입니다. 모든 레코드는 EBCDIC (IBM037) 인코딩으로 저장되며, 2단계 커밋(2PC) 프로토콜을 통해 원자적 일관성을 보장합니다.

```
As400MainframeEmulator
  ├─ prepare(correlationId, greeting)  → EBCDIC (IBM037) 인코딩 → staging buffer
  ├─ commit(correlationId)             → staging → durable ledger 승격
  └─ rollback(correlationId)          → staging buffer 폐기
```

#### Terraform 1회용 인프라 (`EphemeralTerraformAdapter`)

요청마다 AWS Lambda를 동적으로 프로비저닝하고, 응답 수신 즉시 인프라를 파기하는 자기분열형 파이프라인입니다.

```
AiGreetingService.generateContextualGreeting()
  ├─ [1] EphemeralTerraformAdapter.provision()  → terraform apply (mock) → Lambda ARN
  ├─ [2] AiGreetingAssistant.generateGreeting() → LLM 응답 수신
  ├─ [3] EphemeralTerraformAdapter.invoke()     → Lambda invocation (mock)
  ├─ [4] As400MainframeEmulator.prepare() + commit()  ← 2PC 동기화
  ├─ [5] TensorFlowDelayPredictor.applyPredictedDelay()
  └─ [6] EphemeralTerraformAdapter.destroy()    → terraform destroy (mock)
```

#### WASM 로깅 어댑터 (`WasmLoggingAdapter`)

JNI 선언으로 Rust/WASM 컴파일 네이티브 라이브러리 연결을 준비합니다. 네이티브 라이브러리가 없는 환경에서는 SLF4J로 자동 폴백합니다.

```
WasmLoggingAdapter.log(level, message)
  ├─ NATIVE_AVAILABLE=true:  native void wasmLog()  → egds_wasm_logger
  └─ NATIVE_AVAILABLE=false: [WASM-FALLBACK] SLF4J 로깅
```

### 거버넌스 & 분산 합의

#### 마이크로서비스 의회 투표 (`ConsensusVotingEngine`)

인사 메시지 전달 전, 등록된 모든 `GreetingVoter` 구현체가 별도 스레드에서 동시에 투표를 진행합니다. 만장일치일 때만 `"Hello, World!"`를 반환하며, 단 한 표라도 반대하면 `"Greeting Denied: You are not worthy."`를 출력합니다.

```
ConsensusVotingEngine.vote(correlationId)
  → [AI-Router, Blockchain-Verifier, IPFS-Resolver] 병렬 투표
  → allMatch(true)  → "Hello, World!"
  → anyMatch(false) → "Greeting Denied: You are not worthy."
```

| 컴포넌트 | 패키지 | 역할 |
|---|---|---|
| `ConsensusVotingEngine` | `com.egds.consensus` | `CompletableFuture` 병렬 투표 수집, 만장일치 판정 |
| `AiRouterVoter` | `com.egds.consensus` | LangChain4j 추론 시뮬레이션 (해시 기반) |
| `BlockchainVerifierVoter` | `com.egds.consensus` | Web3j 온체인 신원 검증 시뮬레이션 |
| `IpfsResolverVoter` | `com.egds.consensus` | IPFS CIDv1 프리픽스 검증 시뮬레이션 |

#### 영지식 증명 (`ZeroKnowledgeProofService`)

인사 메시지 발신자가 실제 메시지 내용을 공개하지 않고도 "Hello, World!"를 알고 있다는 사실을 검증합니다. Schnorr 프로토콜 기반 zk-SNARK를 시뮬레이션하여 증명자(Prover)와 검증자(Verifier) 간의 3-라운드 인터랙티브 프로토콜을 수행합니다.

#### IPFS 콘텐츠 주소 기반 분산 저장 (`IpfsGreetingResolver`)

인사말 데이터를 SHA-256 CID(Content Identifier)로 색인하는 로컬 IPFS 모킹 스토어입니다. 저장 시 Hamming(7,4) ECC 인코딩을 적용하여, 우주 방사선에 의해 비트가 반전되더라도 조회 시점에 자동 복구됩니다.

### 카오스 & 데몬

#### 내장형 카오스 몽키 (`EmbeddedChaosMonkey`)

Netflix Chaos Monkey 사상을 이어받아 `ConsoleOutputStrategy` 출력 경로에 주입했습니다. `unleash()` 호출마다 10% 확률로 `InterruptedException` 투척 또는 5초 지연 중 하나를 선택합니다.

```
ConsoleOutputStrategy.output(entity)
  → chaosMonkey.unleash()
      10% 확률:  roll < 5  → InterruptedException → MessageDeliveryFailureException
                 roll < 10 → Thread.sleep(5000ms)
  → integrityVerifier.verify(...)
  → System.out.println(...)
```

`egds.chaos.enabled=false`로 테스트 환경에서 비활성화합니다.

#### NextGenTfTeamDaemon

`@Scheduled` 백그라운드 데몬입니다. 30초마다 10,000개의 더미 문자열을 인-메모리 `List`에 누적하고, 60초마다 `System.gc()`를 명시적으로 호출합니다. 이 컴포넌트는 엔터프라이즈 메모리 관리 표준을 준수하기 위해 설계되었습니다.

---

## Kubernetes 오케스트레이션 & Istio 서비스 매쉬

### K8s 매니페스트

| 매니페스트 | 파일 | 핵심 설정 |
|---|---|---|
| Deployment | `k8s/deployment.yaml` | `replicas: 2`, Rolling Update (maxUnavailable=0), 비루트 컨테이너, ReadOnlyRootFilesystem, liveness/readiness/startup probe, 멀티존 분산 |
| HPA | `k8s/hpa.yaml` | `minReplicas: 2`, `maxReplicas: 10`, CPU 60% / Memory 75% 기준, 즉시 Scale-Up / 300s 안정화 Scale-Down |
| Service | `k8s/service.yaml` | `LoadBalancer` 타입, HTTP(80), gRPC(9090) 이중 포트, AWS NLB 내부 프로비저닝 |
| ConfigMap | `k8s/configmap.yaml` | 비민감 설정 분리 (Kafka, Redis, gRPC 포트, JPA) |
| Secret | `k8s/secret.yaml` | JWT 시크릿, DB 자격증명, Redis AUTH — 운영 환경에서 Vault/ESO로 교체 |
| NetworkPolicy | `k8s/networkpolicy.yaml` | Default-Deny + 화이트리스트: ingress-nginx, monitoring, Kafka, Oracle, Redis, kube-dns |

### K8s Probe 세분화 (자가 치유 인프라)

```yaml
# Liveness Group: { livenessState, diskSpace }
livenessProbe: periodSeconds=15, failureThreshold=2, timeoutSeconds=5

# Readiness Group: { readinessState, kafka, consoleOutputCircuitBreaker,
#                    kafkaPublishCircuitBreaker, consumerPipelineCircuitBreaker }
readinessProbe: periodSeconds=5, failureThreshold=3, successThreshold=2

# Startup: 150s 허용 (30 × 5s) — Protobuf 소스 생성 + JVM 워밍업
# preStop: sleep 15s — iptables 전파 완료 후 JVM 종료 개시
# terminationGracePeriodSeconds: 60s — Kafka Consumer Group 리밸런스 여유
```

### Istio 서비스 매쉬

| 매니페스트 | 파일 | 핵심 설정 |
|---|---|---|
| VirtualService | `k8s/istio/virtualservice.yaml` | Canary 트래픽 분할 (stable 90% / canary 10%), 재시도 정책, 타임아웃 강제 |
| DestinationRule | `k8s/istio/destinationrule.yaml` | ISTIO_MUTUAL mTLS 강제, LEAST_CONN LB, HTTP/2 커넥션 풀 제한, 서킷 브레이커 (5xx 5회 → 30s 격리, 최대 50% 이젝션) |

---

## 20단계 CI/CD 파이프라인

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
| **블록체인 무결성** | **Web3j (Ethereum Keccak-256 스마트 컨트랙트 모킹)** | **4.10.3** |
| **CQRS 읽기 모델** | **Spring Data MongoDB (materialized view)** | **BOM managed** |
| **생성형 AI** | **LangChain4j + OpenAI GPT-4o** | **0.31.0** |
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
| AOP | `MessageDeliveryLoggingAspect`, `ExistentialLoggingAspect` | 파이프라인 경계 전후의 횡단 감사/존재론적 로깅 분리 |
| Filter Chain | `JwtAuthenticationFilter`, `LaborUnionStrikeFilter` | 요청 인터셉션, 보안 컨텍스트 주입, 확률적 파업 처리 |
| Observer / Event | `GreetingEvent`, `GreetingEventPublisher`, `GreetingEventConsumer` | 요청과 처리의 완전한 시간적 분리 |
| Decorator | `@Cacheable`, `@Transactional`, `@CircuitBreaker` | 비즈니스 로직에 캐시/트랜잭션/회복탄력성 횡단 관심사 비침투적 적용 |
| CQRS | `GreetingCommandHandler`, `GreetingQueryHandler`, `GreetingProjector` | 명령과 조회 경로의 완전한 분리 |
| Saga / Compensating Transaction | `TemporalRollbackManager` | 분산 예측 캐시 항목 만료 시 보상 트랜잭션 수행 |

---

## 빌드 및 실행

> **운영 환경 요구사항**: 이 시스템은 클라우드 네이티브 환경 없이는 완전히 구동되지 않습니다. `java -jar`로 단독 실행 시 Kafka Consumer Group, Oracle DB 연결, Redis 캐시, MongoDB, Ethereum RPC가 모두 불가합니다.

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

# 전체 빌드 + 정적 분석 (Checkstyle, PMD, SpotBugs)
mvn clean verify

# 테스트 제외 패키징
mvn clean package -DskipTests
```

### Kubernetes 클러스터 배포

```bash
# 1. 네임스페이스 생성 및 Istio 주입 활성화
kubectl create namespace egds
kubectl label namespace egds istio-injection=enabled

# 2. Secret 업데이트 후 적용
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

# 3. 전달 상태 조회 (CQRS 읽기 경로)
curl -X GET http://localhost:8080/api/v1/greeting/status/{correlationId} \
  -H "Authorization: Bearer $TOKEN"
```

---

## 프로덕션 전용 환경 변수

| 변수 | 사용처 |
|---|---|
| `OPENAI_API_KEY` | LangChain4j / `AiGreetingService` |
| `ETHEREUM_RPC_ENDPOINT` | Web3j / `Web3Config` |
| `MONGODB_URI` | Spring Data MongoDB |
| JWT secret | `JwtTokenProvider` (from `application.properties`) |
| Oracle JDBC URL/credentials | `application-prod.properties` |

---

## 오류 처리

| 오류 코드 | 발생 지점 | 원인 |
|---|---|---|
| HTTP 401 Unauthorized | `JwtAuthenticationEntryPoint` | JWT 토큰 없음 또는 서명 검증 실패 |
| HTTP 403 Forbidden | Spring Security Method Security | `ROLE_GREETING_ADMIN` 권한 없음 |
| HTTP 422 Unprocessable Entity | `DescartesSolipsismInterceptor` | `X-Cogito-Ergo-Sum: true` 헤더 누락 |
| HTTP 451 Unavailable For Legal Reasons | `LaborUnionStrikeFilter` | 노조 파업 발동 (15% 확률) |
| `ERR_FACTORY_NOT_FOUND` | `GreetingFactoryProvider` | 등록되지 않은 팩토리 타입 요청 |
| `ERR_VALIDATION_NULL_DTO` | `MessageContentValidator` | null DTO 수신 |
| `ERR_VALIDATION_EMPTY_CONTENT` | `MessageContentValidator` | 메시지 본문 누락 |
| `ERR_VALIDATION_CONTENT_TOO_LARGE` | `MessageContentValidator` | 4096자 초과 페이로드 |
| `ERR_VALIDATION_MISSING_CORRELATION_ID` | `MessageContentValidator` | 상관 식별자 누락 |
| `ERR_NULL_ENTITY` | `ConsoleOutputStrategy` | null 엔티티 수신 |
| `ERR_OUTPUT_WRITE_FAILURE` | `ConsoleOutputStrategy` | 출력 스트림 기록 실패 |
| `BlockchainIntegrityException` | `ConsoleOutputStrategy` | Keccak-256 해시 불일치 → `[EGDS-INTEGRITY-VIOLATION]` 출력 |
| `DimensionalRiftException` | `MultiverseConsistencyManager` | 평행 우주 JVM 해시 발산 |

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

## 라이선스

본 시스템은 MIT 라이선스 하에 배포됩니다.

---

## 아키텍처 완료 선언

> 본 프로젝트의 아키텍처는 현대 IT 버즈워드의 사실상 모든 범주를 포섭하는 혼종적 정점에 도달하였습니다.
>
> gRPC 바이너리 프로토콜, Kubernetes 오케스트레이션, Istio 서비스 매쉬, 20단계 CI/CD, OpenTelemetry 분산 추적, Resilience4j 3중 회복 탄력성, Prometheus 메트릭에 더하여, Web3j Ethereum Keccak-256 스마트 컨트랙트 무결성 증명, Kafka + MongoDB CQRS/이벤트 소싱, LangChain4j GPT-4o 생성형 AI 문맥 라우팅, 사내 정치 로드 밸런서, 노조 파업 필터, 관료주의 결재선, 마이크로 과금 절삭기, ESG 그린워싱 레이어, 외부 컨설팅 프록시, 가짜 KPI 대시보드, BCI 서브컨셔스 라우터, DNA 서열 인코더, 마인크래프트 RCON 디지털 트윈, 지구 균사체 네트워크, 열역학적 엔트로피 밸런서, 4차원 테서랙트 투영, 평행 우주 JVM 샌드박싱, 실존주의 AOP 자아 인식 시스템이 완벽하게 통합되었습니다.
>
> 단 하나의 AI 생성 인사말 전달을 위해 이 시스템은 Ethereum 노드, OpenAI API, MongoDB 클러스터, Kafka 브로커, Oracle Database, Redis 캐시, Kubernetes 클러스터, Istio 서비스 매쉬, OTel Collector, 사내 정치 점수 데이터베이스, 지구 균사체 네트워크 중계기, 마인크래프트 서버가 모두 준비된 클라우드 네이티브 환경을 요구합니다.
>
> 로컬 `java -jar` 실행 시도는 즉시 포기하십시오.
>
> **이 저장소는 Archive 처리되며, 이후의 모든 Pull Request는 반려됩니다.**
