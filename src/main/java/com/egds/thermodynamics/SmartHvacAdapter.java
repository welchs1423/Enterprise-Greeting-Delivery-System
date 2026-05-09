package com.egds.thermodynamics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simulates an IoT smart HVAC API for server-room temperature adjustment.
 *
 * <p>In production this adapter would issue authenticated HTTP requests
 * to the building management system's REST endpoint. In the current
 * implementation all calls are simulated in-process; the computed delta
 * is emitted to the structured log only.
 */
@Component
public class SmartHvacAdapter {

    private static final Logger LOG =
            LoggerFactory.getLogger(SmartHvacAdapter.class);

    /**
     * Requests a temperature reduction proportional to the supplied delta.
     *
     * @param correlationId request identifier for audit tracing
     * @param deltaCelsius  requested cooling in degrees Celsius;
     *                      expected to be a small positive value
     */
    public void requestCoolingOffset(
            String correlationId, double deltaCelsius) {
        LOG.info("[HVAC] cooling offset requested"
                + " correlationId={} deltaCelsius={}",
                correlationId, deltaCelsius);
        LOG.debug("[HVAC] ESG transaction committed"
                + " correlationId={}", correlationId);
    }
}
