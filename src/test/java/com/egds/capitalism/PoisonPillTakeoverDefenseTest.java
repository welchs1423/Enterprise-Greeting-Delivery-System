package com.egds.capitalism;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit tests for {@link PoisonPillTakeoverDefense}.
 */
class PoisonPillTakeoverDefenseTest {

    /**
     * Verifies that the filter forwards the request when disabled.
     * In unit test context, {@code @Value} is not injected so
     * {@code poisonPillEnabled} defaults to {@code false}.
     */
    @Test
    void disabledFilterForwardsRequest() throws Exception {
        PoisonPillTakeoverDefense filter =
                new PoisonPillTakeoverDefense();
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
    }

    /**
     * Verifies that the poison heap remains empty before threshold is
     * breached, using a safe override that prevents real allocation.
     */
    @Test
    void poisonHeapEmptyBeforeThresholdBreached() throws Exception {
        PoisonPillTakeoverDefense filter =
                new PoisonPillTakeoverDefense() {
            @Override
            boolean isPoisonPillEnabled() {
                return true;
            }
        };
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertThat(filter.getPoisonHeapSize()).isEqualTo(0);
    }
}
