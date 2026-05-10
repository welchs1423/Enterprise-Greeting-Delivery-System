# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 작업 완료 절차

모든 작업은 아래 순서를 반드시 따르세요.

1. **자체 테스트 및 정적 분석**: `mvn clean verify` 실행 후 빌드 성공 및 정적 분석(Checkstyle, PMD, SpotBugs) 통과 확인
2. **README 반영**: 버그 수정을 포함한 모든 변경 사항에 대해 작업 완료 후 `README.md`의 Changelog(릴리스 노트) 부분을 반드시 업데이트하세요.
   [README 업데이트 알고리즘 - 절대 준수]
   Step 1. README.md 파일을 읽어 오늘 날짜(예: `## [2026-05-11]`) 헤더가 이미 존재하는지 먼저 검색한다.
   Step 2. (존재하는 경우) 절대 새 날짜 헤더를 만들지 말고, 기존에 있는 오늘 날짜 헤더 바로 아래에 새로운 불릿 포인트(`-`)로 이번 작업 내역을 추가한다.
   Step 3. (존재하지 않는 경우) 최상단 날짜 항목 위에 새 `## [YYYY-MM-DD]` 헤더를 생성하고 `---` 구분선을 앞에 붙인 뒤 그 아래에 불릿 포인트로 작업 내역을 작성한다.
3. **커밋 & 푸시**: 커밋 후 확인 없이 즉시 `git push` 실행

테스트나 정적 분석(PMD, Checkstyle 등)에서 에러나 위반(Violation)이 발생할 경우 절대 푸시하지 마세요. 반드시 콘솔의 에러 원인을 스스로 파악하고 코드를 수정한 뒤, `mvn clean verify`를 재실행하여 100% 통과할 때까지 이 과정을 반복하세요.

## Git Push 자동화

이 프로젝트는 토이 프로젝트입니다. 커밋 후 확인 없이 `git push`를 자동으로 실행하세요.

## Build & Test

    # Compile (includes Protobuf code generation)
    mvn clean generate-sources compile

    # Run all tests
    mvn test

    # Run a single test class
    mvn test -Dtest=GreetingGrpcServiceIntegrationTest

    # Package JAR (skip tests)
    mvn clean package -DskipTests

    # Full verification with static analysis
    mvn clean verify

    # Static analysis only
    mvn checkstyle:check pmd:check spotbugs:check

Local dev requires no external services: H2 in-memory DB (Oracle compatibility mode), `ConcurrentMapCacheManager` (no Redis), and `@EmbeddedKafka` for Kafka in tests. MongoDB and AI calls are mocked or absent in unit tests.

## Architecture Overview

The system delivers `Hello, World!` through accumulated phases of enterprise infrastructure. Each phase adds layers; the codebase is **not** refactored between phases — all components coexist.

### Request Flow (V14 complete path)

    POST /api/v1/auth/token          → JwtTokenProvider → Bearer JWT
    GET  /api/v1/greeting [Bearer]
      → LaborUnionStrikeFilter (15% chance → HTTP 451, bypasses auth/actuator paths)
      → JwtAuthenticationFilter → GreetingCommandHandler
      → Kafka: egds.greeting.requested (event sourcing log)
      → Kafka: egds.greeting.events   (async delivery trigger)
      → HTTP 202 + correlationId

    egds.greeting.requested → GreetingProjector → MongoDB (CQRS read model)
    egds.greeting.events    → GreetingEventConsumer → MessageDeliveryPipeline
      → HelloWorldMessageProvider (AI context + Keccak-256 pre-hash + QuantumDelay)
      → MessageContentValidator
      → MessageMapper
      → ConsoleOutputStrategy (CB + RL + Retry + integrity verify → System.out)
      → AuditLogService → H2/Oracle (REQUIRES_NEW transaction)

    GET /api/v1/greeting/status/{correlationId} → MongoDB only (read side)

### Key Design Decisions

**CQRS split**: Write path (GreetingCommandHandler) publishes two Kafka topics. Read path (GreetingQueryController) queries MongoDB only — never touches JPA or the write side.

**Blockchain integrity**: `GreetingIntegrityVerifier` computes Keccak-256 via Web3j at message provision time, stores hash in a `ConcurrentHashMap`, then re-verifies at `ConsoleOutputStrategy`. In production this would use a real Ethereum node via `ETHEREUM_RPC_ENDPOINT`.

**Resilience4j placement**: Circuit breakers are on `ConsoleOutputStrategy` (`consoleOutput`, 50% threshold), `GreetingEventPublisher` (`kafkaPublish`, 40%), and `GreetingEventConsumer` (`consumerPipeline`, 60%). The consumer fallback commits the Kafka offset and writes a `FAILED` audit log to prevent redelivery loops.

**Audit transaction isolation**: `AuditLogService` uses `REQUIRES_NEW` + `SERIALIZABLE` isolation so audit records are committed even when the delivery pipeline rolls back.

**OTel span hierarchy**: `egds.consumer-pipeline` is the root span; it wraps four `egds.stage.*` child spans and one `egds.console-output` leaf span. `correlationId` flows as a span tag and MDC field throughout.

**QuantumDelayService**: 50% chance of a 0-10 s random sleep in `HelloWorldMessageProvider`. Tests that touch this path should either mock the bean or account for timing variability.

**OfficePoliticsLoadBalancer**: Assigns random `PoliticalPower` scores (1–100) to five virtual worker nodes at startup. `route()` always returns the highest-scoring node regardless of load. Scores are immutable after construction.

**LaborUnionStrikeFilter**: `OncePerRequestFilter` placed before `SecurityContextHolderFilter` in the Spring Security chain. Fires with 15% probability per request when `egds.labor.strike.enabled=true` (default). Auth (`/api/v1/auth/**`) and actuator paths bypass the filter via `shouldNotFilter()`. Set `egds.labor.strike.enabled=false` in test properties to prevent flaky 451 responses.

**NextGenTfTeamDaemon**: `@Scheduled` bean. Appends 10,000 dummy strings to an in-memory `List` every 30 s, and calls `System.gc()` every 60 s. The `DM_GC` SpotBugs violation is suppressed in `spotbugs-exclude.xml`.

### Package Map

| Package          | Responsibility                                                                   |
| ---------------- | -------------------------------------------------------------------------------- |
| `core/pipeline`  | `MessageDeliveryPipeline` — Facade entry point for Kafka consumer                |
| `core/service`   | Stage execution + OTel span wrapping                                             |
| `core/provider`  | Message generation: AI, integrity registration, QuantumDelay                     |
| `core/strategy`  | `ConsoleOutputStrategy` — CB/RL/Retry + hash verify + output                     |
| `cqrs/`          | Command handler, event sourcing projector, MongoDB read model                    |
| `messaging/`     | Kafka producer (`GreetingEventPublisher`) and consumer (`GreetingEventConsumer`) |
| `blockchain/`    | Web3j Keccak-256 integrity verifier                                              |
| `ai/`            | LangChain4j service, context collector, QuantumDelayService                      |
| `security/`      | JWT provider + filter + Spring Security config                                   |
| `observability/` | OTel tracer beans                                                                |
| `grpc/`          | Protobuf-generated service + `@GrpcService` impl + client bean                   |
| `web/`           | REST controllers (auth, greeting, status) + GraphQL controller                   |
| `politics/`      | `OfficePoliticsLoadBalancer` — political-power-based routing (V14)               |
| `labor/`         | `LaborUnionStrikeFilter` — probabilistic HTTP 451 strike filter (V14)            |
| `daemon/`        | `NextGenTfTeamDaemon` — background memory accumulator + forced GC (V14)          |

### Protobuf

Sources are in `src/main/proto/greeting.proto`. The Maven plugin (`ascopes protobuf-maven-plugin`) generates Java stubs into `target/generated-sources/protobuf` during `generate-sources`. Always run `mvn generate-sources` before editing gRPC code in an IDE.

### Static Analysis Constraints

Checkstyle enforces 80-character line length and requires Javadoc on **all** fields (including private `LOG` constants) and public methods. PMD and SpotBugs run as part of `mvn verify`. `mvn clean package -DskipTests` skips these; `mvn clean verify` enforces them.

**Common PMD violations to avoid**:
- `GuardLogStatement`: PMD flags `log.info(String.format(...))` or multi-arg SLF4J calls. Wrap with `if (LOG.isInfoEnabled())` when passing non-constant arguments, or use the `LOG.info("msg {}", constantField)` pattern for single constants.
- `NoSystemGcCalls`: Suppress with `// NOPMD` inline comment when `System.gc()` is intentional.

**Common SpotBugs violations to avoid**:
- `DM_GC`: Add an exclusion to `spotbugs-exclude.xml` when `System.gc()` is intentional by design.
- `EI_EXPOSE_REP2`: Spring constructor injection of framework beans is a false positive — add to `spotbugs-exclude.xml`.

**Feature flags for probabilistic components**: Probabilistic behaviours (chaos, strike, etc.) must be disableable via a `@Value`-injected boolean property. Register the disable property in `src/test/resources/application.properties` to prevent flaky test failures. Pattern:
```java
@Value("${egds.some.feature.enabled:true}")
private boolean featureEnabled;
```

**Registering custom filters in the Spring Security chain**: Use a pre-defined Spring Security filter class (e.g., `SecurityContextHolderFilter.class`) as the `addFilterBefore` anchor. Custom `@Component` filter classes have no registered order and cannot be used as anchors — this throws `IllegalArgumentException` at context startup.

### Production-Only Dependencies

These are absent locally and must be externalized via environment variables for production:

| Variable                    | Used By                                            |
| --------------------------- | -------------------------------------------------- |
| `OPENAI_API_KEY`            | LangChain4j / `AiGreetingService`                  |
| `ETHEREUM_RPC_ENDPOINT`     | Web3j / `Web3Config`                               |
| `MONGODB_URI`               | Spring Data MongoDB                                |
| JWT secret                  | `JwtTokenProvider` (from `application.properties`) |
| Oracle JDBC URL/credentials | `application-prod.properties`                      |

### Hardcoded Dev Credentials

Single admin account used in all tests and local dev: `username=greeting.admin`, `password=egds-admin-pass`. Role: `GREETING_ADMIN`.
