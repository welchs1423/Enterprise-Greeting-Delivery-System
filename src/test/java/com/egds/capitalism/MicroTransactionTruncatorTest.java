package com.egds.capitalism;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit tests for {@link MicroTransactionTruncator}.
 */
class MicroTransactionTruncatorTest {

    /** Filter instance under test. */
    private MicroTransactionTruncator filter;

    /** Initialises a fresh filter before each test. */
    @BeforeEach
    void setUp() {
        filter = new MicroTransactionTruncator();
    }

    /** Verifies that requests without a budget header pass through. */
    @Test
    void noBudgetHeaderForwardsRequestUnchanged() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setServletPath("/api/v1/greeting");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
        assertThat(res.getStatus()).isEqualTo(200);
    }

    /** Verifies that a non-positive budget returns HTTP 402. */
    @Test
    void nonPositiveBudgetReturns402() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Client-Budget", "0");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(402);
    }

    /** Verifies that a negative budget returns HTTP 402. */
    @Test
    void negativeBudgetReturns402() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Client-Budget", "-5");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertThat(res.getStatus()).isEqualTo(402);
    }

    /** Verifies that a malformed budget header passes through. */
    @Test
    void malformedBudgetHeaderForwardsRequest() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Client-Budget", "not-a-number");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        verify(chain, times(1)).doFilter(req, res);
    }

    /** Verifies that budget below content length truncates with "...". */
    @Test
    void budgetBelowContentLengthTruncatesWithEllipsis() {
        String result = filter.applyBudget("Hello, World!", 5);
        assertThat(result).isEqualTo("Hello...");
    }

    /** Verifies that budget equal to content length returns full content. */
    @Test
    void budgetEqualToContentLengthReturnsFullContent() {
        String result = filter.applyBudget("Hello", 5);
        assertThat(result).isEqualTo("Hello");
    }

    /** Verifies that budget exceeding content length returns full content. */
    @Test
    void budgetExceedingContentLengthReturnsFullContent() {
        String result = filter.applyBudget("Hi", 100);
        assertThat(result).isEqualTo("Hi");
    }
}
