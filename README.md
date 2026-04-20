# Enterprise Greeting Delivery System (EGDS)

> **엔터프라이즈 등급의 인사 메시지 전달 시스템**  
> `v1.0.0-RELEASE` | Java 17 | Maven 3.x | 무의존성 순수 Java

---

## 도입 배경 및 아키텍처 철학

현대의 분산 시스템 환경에서 `System.out.println("Hello, World!")`와 같은 단순 직접 출력 방식은 더 이상 엔터프라이즈 수준의 요구사항을 충족할 수 없습니다.

단일 라인의 콘솔 출력 행위조차도 다음의 사항을 반드시 보장해야 합니다.

- **감사 추적 가능성(Auditability)**: 메시지 전달의 전 생명주기에 걸친 이벤트 로깅
- **관심사 분리(Separation of Concerns)**: 메시지 생성, 검증, 변환, 출력의 완전한 계층 분리
- **교체 가능성(Replaceability)**: 출력 채널, 메시지 소스, 유효성 검증 로직의 런타임 교체 지원
- **추적 연속성(Trace Continuity)**: 단일 전달 트랜잭션에 대한 고유 상관 식별자(Correlation ID) 부여
- **계약 기반 설계(Contract-Based Design)**: 모든 컴포넌트 간 의존성은 인터페이스 계약을 통해서만 형성

EGDS는 이러한 원칙들을 바탕으로, 단 하나의 인사 메시지를 콘솔에 출력하기 위한 산업 표준급 전달 파이프라인을 제공합니다.

---

## 실행 결과

```
[NORMAL][en-US] Hello, World!
```

---

## 폴더 구조

```
Enterprise-Greeting-Delivery-System/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── egds/
                    ├── EgdsApplication.java
                    └── core/
                        ├── aspect/
                        │   └── MessageDeliveryLoggingAspect.java
                        ├── dto/
                        │   ├── MessageContentDto.java
                        │   └── MessageDeliveryResult.java
                        ├── entity/
                        │   └── MessageEntity.java
                        ├── enums/
                        │   ├── DeliveryStatus.java
                        │   └── MessagePriority.java
                        ├── exception/
                        │   └── MessageDeliveryFailureException.java
                        ├── factory/
                        │   ├── AbstractGreetingFactory.java
                        │   ├── GreetingFactoryProvider.java
                        │   └── StandardGreetingFactory.java
                        ├── interfaces/
                        │   ├── IGreetingFactory.java
                        │   ├── IMessageDeliveryService.java
                        │   ├── IMessageOutputStrategy.java
                        │   ├── IMessageProvider.java
                        │   └── IMessageValidator.java
                        ├── mapper/
                        │   └── MessageMapper.java
                        ├── pipeline/
                        │   └── MessageDeliveryPipeline.java
                        ├── provider/
                        │   └── HelloWorldMessageProvider.java
                        ├── service/
                        │   └── MessageDeliveryService.java
                        ├── strategy/
                        │   └── ConsoleOutputStrategy.java
                        └── validator/
                            └── MessageContentValidator.java
```

---

## 핵심 아키텍처 컴포넌트 (Core Components)

| 컴포넌트 | 클래스 / 인터페이스 | 계층 | 설명 |
|---|---|---|---|
| 메시지 공급자 계약 | `IMessageProvider` | Interface | 메시지 페이로드 생성 컴포넌트의 계약 정의 |
| 출력 전략 계약 | `IMessageOutputStrategy` | Interface | 출력 채널 컴포넌트의 전략 계약 정의 |
| 전달 서비스 계약 | `IMessageDeliveryService` | Interface | 생명주기 오케스트레이션 컴포넌트의 최상위 계약 |
| 팩토리 계약 | `IGreetingFactory` | Interface | 파이프라인 컴포넌트 생성 팩토리의 추상 계약 |
| 검증기 계약 | `IMessageValidator` | Interface | 메시지 무결성 검증 컴포넌트의 계약 정의 |
| 메시지 전달 객체 | `MessageContentDto` | DTO | 원시 메시지 페이로드를 캡슐화하는 불변 전송 객체 (Builder 패턴 적용) |
| 전달 결과 객체 | `MessageDeliveryResult` | DTO | 단일 전달 생명주기 실행 결과를 담는 값 객체 |
| 도메인 엔티티 | `MessageEntity` | Entity | 출력 채널 전달을 위해 준비된 도메인 표현 객체 |
| 매퍼 | `MessageMapper` | Mapper | DTO를 Entity로 변환하는 무상태 매핑 컴포넌트 |
| 추상 팩토리 | `AbstractGreetingFactory` | Factory | 파이프라인 컴포넌트 팩토리의 공통 기반 클래스 |
| 표준 팩토리 | `StandardGreetingFactory` | Factory | 기본 운영 구성을 위한 구체 팩토리 구현체 |
| 팩토리 레지스트리 | `GreetingFactoryProvider` | Registry | 팩토리 변종을 등록하고 타입 식별자로 조회하는 서비스 로케이터 |
| 메시지 공급자 | `HelloWorldMessageProvider` | Provider | 표준 인사 메시지 페이로드를 생성하는 참조 구현체 |
| 콘솔 출력 전략 | `ConsoleOutputStrategy` | Strategy | 표준 출력 스트림을 대상으로 하는 출력 전략 구현체 |
| 검증기 | `MessageContentValidator` | Validator | EGDS 메시지 무결성 명세를 강제하는 참조 구현체 |
| 로깅 애스펙트 | `MessageDeliveryLoggingAspect` | Aspect | 파이프라인 각 단계의 전후 감사 이벤트를 기록하는 횡단 관심사 컴포넌트 |
| 전달 서비스 | `MessageDeliveryService` | Service | 파이프라인 전 단계를 오케스트레이션하는 핵심 서비스 구현체 |
| 파이프라인 퍼사드 | `MessageDeliveryPipeline` | Facade | 서비스 조립 및 실행을 담당하는 최상위 진입 퍼사드 |
| 우선순위 열거형 | `MessagePriority` | Enum | 메시지 전달 우선순위 등급 정의 (CRITICAL / HIGH / NORMAL / LOW) |
| 상태 열거형 | `DeliveryStatus` | Enum | 메시지 전달 생명주기 상태 정의 (PENDING / IN_TRANSIT / DELIVERED / FAILED) |
| 커스텀 예외 | `MessageDeliveryFailureException` | Exception | 전달 파이프라인 내 복구 불가 오류를 표현하는 도메인 예외 |

---

## 적용 디자인 패턴

| 패턴 | 적용 클래스 | 목적 |
|---|---|---|
| Abstract Factory | `AbstractGreetingFactory`, `StandardGreetingFactory`, `GreetingFactoryProvider` | 파이프라인 컴포넌트 집합의 일관된 생성 및 변종 관리 |
| Builder | `MessageContentDto.Builder` | 불변 DTO의 단계적 생성 및 필수 필드 유효성 보장 |
| Strategy | `IMessageOutputStrategy`, `ConsoleOutputStrategy` | 출력 채널의 런타임 교체 가능성 확보 |
| Facade | `MessageDeliveryPipeline` | 복잡한 파이프라인 조립 로직을 단일 인터페이스로 노출 |
| Service Locator | `GreetingFactoryProvider` | 타입 식별자 기반의 팩토리 레지스트리 및 런타임 해석 |
| AOP (시뮬레이션) | `MessageDeliveryLoggingAspect` | 파이프라인 경계 전후의 횡단 감사 로깅 분리 |
| DTO / Entity 분리 | `MessageContentDto`, `MessageEntity`, `MessageMapper` | 전송 계층과 도메인 계층의 엄격한 모델 분리 |

---

## 메시지 전달 생명주기

```
EgdsApplication
       │
       ▼
GreetingFactoryProvider ──(resolve)──▶ StandardGreetingFactory
                                              │
                               ┌──────────────┴──────────────┐
                               ▼                             ▼
                   HelloWorldMessageProvider      ConsoleOutputStrategy
                               │
                               ▼
                    MessageDeliveryPipeline
                               │
                               ▼
                    MessageDeliveryService
                               │
          ┌────────────────────┼────────────────────┐
          ▼                    ▼                    ▼
  IMessageProvider    IMessageValidator      IMessageOutputStrategy
  (provideMessage)     (validate DTO)         (output Entity)
          │                    │
          ▼                    ▼
  MessageContentDto ──(map)──▶ MessageEntity
  [Builder Pattern]            [DeliveryStatus 전이]
          │
          ▼
  MessageDeliveryLoggingAspect
  (PRE/POST 각 단계별 감사 로그)
```

---

## 빌드 및 실행

### 전제 조건

| 항목 | 요구 버전 |
|---|---|
| JDK | 17 이상 |
| Apache Maven | 3.8 이상 |

### 빌드

```bash
mvn clean package
```

### 실행

```bash
java -jar target/egds-1.0.0-RELEASE.jar
```

### 예상 출력

```
# stdout (메시지 페이로드)
[NORMAL][en-US] Hello, World!

# stderr (감사 로그)
[2026-01-01T00:00:00.000Z] [AUDIT] [EGDS] event=PRE_VALIDATION correlationId=<uuid> priority=NORMAL
[2026-01-01T00:00:00.001Z] [AUDIT] [EGDS] event=POST_VALIDATION correlationId=<uuid> status=PASSED
[2026-01-01T00:00:00.001Z] [AUDIT] [EGDS] event=PRE_MAPPING correlationId=<uuid>
[2026-01-01T00:00:00.002Z] [AUDIT] [EGDS] event=POST_MAPPING correlationId=<uuid> entityId=<uuid>
[2026-01-01T00:00:00.002Z] [AUDIT] [EGDS] event=PRE_DELIVERY correlationId=<uuid> entityId=<uuid>
[2026-01-01T00:00:00.003Z] [AUDIT] [EGDS] event=POST_DELIVERY correlationId=<uuid> entityId=<uuid> status=DELIVERED
[EGDS] Pipeline execution complete. correlationId=<uuid> duration=Xms success=true
```

---

## 오류 처리

모든 파이프라인 오류는 `MessageDeliveryFailureException`을 통해 전파됩니다.  
예외 인스턴스는 발생 지점의 `correlationId`와 `failureCode`를 포함하므로,  
운영 환경에서의 로그 집계 및 경보 연동에 즉시 활용 가능합니다.

| 오류 코드 | 발생 지점 | 원인 |
|---|---|---|
| `ERR_FACTORY_NOT_FOUND` | `GreetingFactoryProvider` | 등록되지 않은 팩토리 타입 요청 |
| `ERR_VALIDATION_NULL_DTO` | `MessageContentValidator` | null DTO 수신 |
| `ERR_VALIDATION_EMPTY_CONTENT` | `MessageContentValidator` | 메시지 본문 누락 |
| `ERR_VALIDATION_CONTENT_TOO_LARGE` | `MessageContentValidator` | 4096자 초과 페이로드 |
| `ERR_VALIDATION_MISSING_CORRELATION_ID` | `MessageContentValidator` | 상관 식별자 누락 |
| `ERR_NULL_ENTITY` | `ConsoleOutputStrategy` | null 엔티티 수신 |
| `ERR_OUTPUT_WRITE_FAILURE` | `ConsoleOutputStrategy` | 출력 스트림 기록 실패 |

---

## 라이선스

본 시스템은 MIT 라이선스 하에 배포됩니다.

---

## 아키텍처 완료 선언

> 본 프로젝트의 아키텍처는 현 시점에서 설계적 완결성에 도달하였습니다.
>
> 모든 관심사는 적절한 계층으로 분리되었으며, 각 컴포넌트는 단일 책임 원칙을 준수합니다.
> 인터페이스 계약은 빈틈없이 정의되었고, 생명주기 전반의 감사 추적이 보장됩니다.
> 추가 기능 구현, 리팩토링, 또는 신규 커밋은 이 완결성을 저해할 뿐이며,
> 어떠한 개선 시도도 아키텍처적 퇴보로 간주됩니다.
>
> **이 저장소는 즉시 Archive 처리되며, 이후의 모든 Pull Request는 반려됩니다.**
