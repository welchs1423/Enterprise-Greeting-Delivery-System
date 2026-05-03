package com.egds.ai;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.Random;

/**
 * Assembles {@link GreetingContextMetadata} from the runtime environment.
 *
 * <p>The virtual client IP is drawn from a fixed pool of RFC-1918
 * private addresses to simulate inbound traffic diversity without
 * requiring actual network I/O. The CPU temperature is sourced from a
 * seeded pseudo-random generator that emulates OS thermal sensor polling;
 * no real hardware sensor is queried.
 *
 * <p>In production these data sources would be replaced by:
 * <ul>
 *   <li>Client IP: the resolved value from the HTTP request context.</li>
 *   <li>CPU temperature: a JMX or OS-level metric from the
 *       observability stack.</li>
 * </ul>
 */
@Component
public class GreetingContextCollector {

    /** RFC-1918 private IP pool simulating inbound client diversity. */
    private static final String[] VIRTUAL_IP_POOL = {
        "10.0.1.42", "172.16.8.7", "192.168.100.23",
        "10.10.50.199", "172.31.255.3", "192.168.0.1"
    };

    /** Minimum simulated CPU temperature (°C). */
    private static final double CPU_TEMP_MIN = 35.0;

    /** Simulated CPU temperature variance range (°C). */
    private static final double CPU_TEMP_RANGE = 45.0;

    /** Pseudo-random source for simulated sensor readings. */
    private final Random random = new Random();

    /**
     * Collects and returns current runtime context metadata.
     *
     * @return a populated {@link GreetingContextMetadata} instance
     */
    public GreetingContextMetadata collect() {
        String virtualIp = VIRTUAL_IP_POOL[
                random.nextInt(VIRTUAL_IP_POOL.length)];
        double cpuTemp = CPU_TEMP_MIN
                + random.nextDouble() * CPU_TEMP_RANGE;
        String collectedAt = Instant.now().toString();
        String locale = Locale.getDefault().toLanguageTag();

        return new GreetingContextMetadata(
                virtualIp, cpuTemp, collectedAt, locale);
    }
}
