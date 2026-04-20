package com.enterprise.greeting.exception;

/**
 * 메시지 전달 파이프라인의 임의 계층에서 복구 불가능한 장애가 발생하였을 때
 * 해당 컨텍스트를 캡슐화하여 상위 호출 스택으로 전파하는 비검사(Unchecked) 예외.
 *
 * <p>이 예외는 메시지 전달 생명주기(Message Delivery Lifecycle) 내에서
 * 발생 가능한 모든 치명적 오류의 최상위 루트 원인(Root Cause)으로 취급된다.
 * 하위 시스템에서 발생한 {@link Exception}은 반드시 이 클래스로 래핑(wrapping)되어
 * 계층 간 결합도를 최소화해야 한다.</p>
 *
 * <p><b>사용 지침 (Usage Guideline):</b></p>
 * <ul>
 *   <li>메시지 생성 팩토리에서 인스턴스화 실패 시</li>
 *   <li>메시지 매핑(DTO → Entity) 변환 실패 시</li>
 *   <li>출력 전략(Output Strategy) 실행 중 I/O 장애 발생 시</li>
 * </ul>
 *
 * @author Enterprise Architecture Team
 * @version 1.0.0-RELEASE
 * @since 2026-04-21
 * @see MessageProviderInitializationException
 */
public class MessageDeliveryFailureException extends RuntimeException {

    private static final long serialVersionUID = 8675309L;

    /**
     * 오류 코드 체계(Error Code Schema)에 따른 표준 식별자.
     * 형식: {@code EGDS-[도메인코드]-[순번]}
     */
    private final String errorCode;

    /**
     * 장애가 발생한 파이프라인 계층 식별자.
     * (예: {@code FACTORY_LAYER}, {@code MAPPER_LAYER}, {@code STRATEGY_LAYER})
     */
    private final String failedLayer;

    /**
     * 지정된 오류 코드, 장애 계층, 상세 메시지로 예외를 생성한다.
     *
     * @param errorCode   표준 오류 코드 식별자 (non-null)
     * @param failedLayer 장애가 발생한 아키텍처 계층명 (non-null)
     * @param message     사람이 읽을 수 있는 오류 상세 설명
     */
    public MessageDeliveryFailureException(String errorCode, String failedLayer, String message) {
        super(message);
        this.errorCode = errorCode;
        this.failedLayer = failedLayer;
    }

    /**
     * 지정된 오류 코드, 장애 계층, 상세 메시지, 그리고 근본 원인(Cause)으로 예외를 생성한다.
     *
     * @param errorCode   표준 오류 코드 식별자 (non-null)
     * @param failedLayer 장애가 발생한 아키텍처 계층명 (non-null)
     * @param message     사람이 읽을 수 있는 오류 상세 설명
     * @param cause       이 예외를 유발한 근본 원인 {@link Throwable}
     */
    public MessageDeliveryFailureException(String errorCode, String failedLayer, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.failedLayer = failedLayer;
    }

    /**
     * 이 예외에 할당된 표준 오류 코드를 반환한다.
     *
     * @return 표준 오류 코드 식별자
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 장애가 발생한 아키텍처 계층의 식별자를 반환한다.
     *
     * @return 파이프라인 계층명
     */
    public String getFailedLayer() {
        return failedLayer;
    }

    @Override
    public String toString() {
        return String.format("MessageDeliveryFailureException{errorCode='%s', failedLayer='%s', message='%s'}",
                errorCode, failedLayer, getMessage());
    }
}
