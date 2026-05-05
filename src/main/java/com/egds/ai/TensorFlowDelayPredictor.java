package com.egds.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Deep-learning-based delivery delay predictor simulating TensorFlow JNI
 * inference.
 *
 * <p>In production, this class would load a TensorFlow SavedModel via the
 * TensorFlow Java API (JNI binding), construct a {@code Tensor} from the
 * feature vector, execute a session run, and read the predicted delay from
 * the output {@code Tensor}. The model would be trained on ten years of
 * "Hello World" delivery telemetry to minimise end-to-end latency variance.
 *
 * <p>In this implementation the JNI call is mocked: inference is performed
 * by a deterministic polynomial expression over a synthetic feature vector
 * that approximates the expected output distribution of the trained
 * regression model without requiring a native TensorFlow library on the
 * classpath.
 *
 * <p>This class replaces {@link QuantumDelayService} in
 * {@link AiGreetingService}. Unlike the stochastic quantum model, which
 * applies a uniform random delay with 0.5 probability, the ML predictor
 * always produces a bounded, feature-driven delay derived from the mock
 * inference output.
 */
@Service
public class TensorFlowDelayPredictor {

    private static final Logger LOG =
            LoggerFactory.getLogger(TensorFlowDelayPredictor.class);

    /**
     * Upper bound for the predicted delay in milliseconds.
     * Configurable via {@code egds.tensorflow.max-predicted-delay-ms};
     * defaults to 5000.
     */
    @Value("${egds.tensorflow.max-predicted-delay-ms:5000}")
    private long maxPredictedDelayMs;

    /**
     * Runs the mock TensorFlow JNI inference to obtain a predicted delivery
     * delay and applies it to the calling thread by sleeping.
     *
     * <p>The mock inference computes a delay by evaluating a polynomial
     * feature transform over a synthetic feature vector representing
     * throughput, P99 latency, and queue depth drawn from a simulated
     * sensor buffer. The result is clamped to
     * [0, {@code maxPredictedDelayMs}].
     *
     * @throws InterruptedException if the thread is interrupted during
     *                              the predicted sleep window; the interrupt
     *                              flag is restored before the exception
     *                              propagates
     */
    public void applyPredictedDelay() throws InterruptedException {
        long predictedMs = runMockInference();
        LOG.info("[TF] JNI inference complete predicted_delay_ms={}",
                predictedMs);
        if (predictedMs > 0) {
            try {
                Thread.sleep(predictedMs);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw ie;
            }
        }
        LOG.info("[TF] predicted delay applied delay_ms={}", predictedMs);
    }

    /**
     * Returns a freshly inferred delay in milliseconds without applying it
     * to the calling thread.
     *
     * <p>Provided for observability and testing; each call produces an
     * independent inference result.
     *
     * @return predicted delay in milliseconds, in [0, maxPredictedDelayMs]
     */
    public long predictDelayMs() {
        return runMockInference();
    }

    /**
     * Simulates the TensorFlow SavedModel forward pass.
     *
     * <p>Constructs a synthetic three-element feature vector
     * (throughput, p99_latency, queue_depth) and applies a linear
     * regression weight vector to produce the raw output. The result is
     * clamped to [0, {@code maxPredictedDelayMs}].
     *
     * <p>Weight values were chosen to produce a realistic delay
     * distribution centred around 200 ms under typical load conditions.
     *
     * @return predicted delay in milliseconds
     */
    private long runMockInference() {
        double throughput = ThreadLocalRandom.current().nextDouble(0.1, 1.0);
        double p99Latency = ThreadLocalRandom.current().nextDouble(10.0, 500.0);
        double queueDepth = ThreadLocalRandom.current().nextDouble(0.0, 50.0);

        // Linear regression: bias + w1*throughput + w2*p99 + w3*queue
        double raw = 120.0
                + (-200.0 * throughput)
                + (0.8 * p99Latency)
                + (3.5 * queueDepth);

        long predicted = Math.max(0L,
                Math.min(maxPredictedDelayMs, Math.round(raw)));

        LOG.debug("[TF] inference throughput={} p99={} queue={}"
                + " raw={} clamped_ms={}",
                throughput, p99Latency, queueDepth, raw, predicted);
        return predicted;
    }
}
