package com.egds.surveillance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit tests for {@link ProductivityScoreInterceptor}.
 */
class ProductivityScoreInterceptorTest {

    /** Subject under test with productivity enabled. */
    private ProductivityScoreInterceptor interceptor;

    /** Initialises an enabled interceptor before each test. */
    @BeforeEach
    void setUp() {
        interceptor = new ProductivityScoreInterceptor() {
            @Override
            boolean isProductivityEnabled() {
                return true;
            }
        };
    }

    /** Verifies that the productivity score header is present in responses. */
    @Test
    void postHandle_addsProductivityScoreHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.postHandle(request, response, new Object(), null);

        assertThat(response.getHeader(
                ProductivityScoreInterceptor.SCORE_HEADER))
                .isNotNull();
    }

    /** Verifies that the injected score belongs to the defined grade pool. */
    @Test
    void postHandle_scoreIsFromKnownGradePool() throws Exception {
        List<String> knownGrades = Arrays.asList(
                "D (Below Expectations)",
                "D+ (Marginal)",
                "C- (Needs Improvement)",
                "D- (Unsatisfactory)",
                "C (Approaching Minimum Standard)",
                "F (Performance Improvement Plan Required)"
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.postHandle(request, response, new Object(), null);

        String score = response.getHeader(
                ProductivityScoreInterceptor.SCORE_HEADER);
        assertThat(knownGrades).contains(score);
    }

    /** Verifies that a disabled interceptor injects no header. */
    @Test
    void postHandle_whenDisabled_doesNotInjectHeader() throws Exception {
        ProductivityScoreInterceptor disabled =
                new ProductivityScoreInterceptor() {
            @Override
            boolean isProductivityEnabled() {
                return false;
            }
        };
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        disabled.postHandle(request, response, new Object(), null);

        assertThat(response.getHeader(
                ProductivityScoreInterceptor.SCORE_HEADER))
                .isNull();
    }

    /** Verifies that all grades in the pool are below passing threshold. */
    @Test
    void allGradesAreBelowPassingThreshold() throws Exception {
        List<String> passingGrades = Arrays.asList("A", "B", "B+", "A-");
        MockHttpServletRequest request = new MockHttpServletRequest();

        for (int i = 0; i < 50; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            interceptor.postHandle(request, response, new Object(), null);
            String score = response.getHeader(
                    ProductivityScoreInterceptor.SCORE_HEADER);
            for (String passing : passingGrades) {
                assertThat(score).doesNotStartWith(passing);
            }
        }
    }

    /** Verifies that the header name constant matches the expected value. */
    @Test
    void scoreHeaderNameIsCorrect() {
        assertThat(ProductivityScoreInterceptor.SCORE_HEADER)
                .isEqualTo("X-Employee-Productivity-Score");
    }
}
