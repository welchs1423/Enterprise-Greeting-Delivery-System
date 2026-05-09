# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 작업 완료 절차

모든 작업은 아래 순서를 반드시 따르세요.

1. **자체 테스트**: `mvn test` 실행 후 전체 통과 확인
2. README 반영: 버그 수정을 포함한 모든 변경 사항에 대해, 작업 완료 후 README.md 최상단(또는 Changelog 부분)에 오늘 날짜와 함께 작업 내역을 반드시 업데이트하세요.
3. **커밋 & 푸시**: 커밋 후 확인 없이 즉시 `git push` 실행

테스트 실패 시 푸시하지 말고 원인을 수정한 뒤 재시도하세요.

## Git Push 자동화

이 프로젝트는 토이 프로젝트입니다. 커밋 후 확인 없이 `git push`를 자동으로 실행하세요.

## Build & Test

```bash
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
```

Local dev requires no external services: H2 in-memory DB (Oracle compatibility mode), `ConcurrentMapCacheManager` (no Redis), and `@EmbeddedKafka` for Kafka in tests. MongoDB and AI calls are mocked or absent in unit tests.

## Architecture Overview

The system delivers `Hello, World!` through six accumulated phases of enterprise infrastructure. Each phase adds layers; the codebase is **not** refactored between phases — all components coexist.

### Request Flow (Phase 5/6 complete path)

```
POST /api/v1/auth/token          → JwtTokenProvider → Bearer JWT
GET  /api/v1/greeting [Bearer]   → GreetingCommandHandler
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
```

### Key Design Decisions

**CQRS split**: Write path (GreetingCommandHandler) publishes two Kafka topics. Read path (GreetingQueryController) queries MongoDB only — never touches JPA or the write side.

**Blockchain integrity**: `GreetingIntegrityVerifier` computes Keccak-256 via Web3j at message provision time, stores hash in a `ConcurrentHashMap`, then re-verifies at `ConsoleOutputStrategy`. In production this would use a real Ethereum node via `ETHEREUM_RPC_ENDPOINT`.

**Resilience4j placement**: Circuit breakers are on `ConsoleOutputStrategy` (`consoleOutput`, 50% threshold), `GreetingEventPublisher` (`kafkaPublish`, 40%), and `GreetingEventConsumer` (`consumerPipeline`, 60%). The consumer fallback commits the Kafka offset and writes a `FAILED` audit log to prevent redelivery loops.

**Audit transaction isolation**: `AuditLogService` uses `REQUIRES_NEW` + `SERIALIZABLE` isolation so audit records are committed even when the delivery pipeline rolls back.

**OTel span hierarchy**: `egds.consumer-pipeline` is the root span; it wraps four `egds.stage.*` child spans and one `egds.console-output` leaf span. `correlationId` flows as a span tag and MDC field throughout.

**QuantumDelayService**: 50% chance of a 0–10 s random sleep in `HelloWorldMessageProvider`. Tests that touch this path should either mock the bean or account for timing variability.

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

### Protobuf

Sources are in `src/main/proto/greeting.proto`. The Maven plugin (`ascopes protobuf-maven-plugin`) generates Java stubs into `target/generated-sources/protobuf` during `generate-sources`. Always run `mvn generate-sources` before editing gRPC code in an IDE.

### Static Analysis Constraints

Checkstyle enforces 80-character line length and requires Javadoc on public methods. PMD and SpotBugs run as part of `mvn verify`. `mvn clean package -DskipTests` skips these; `mvn clean verify` enforces them.

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
