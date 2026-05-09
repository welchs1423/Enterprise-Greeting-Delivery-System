package com.egds.quantum;

import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Quantum tesseract output adapter (4D hyperplane projection).
 *
 * <p>Declares a native interface to a hypothetical quantum
 * co-processor via JNI. When the native library is absent (all
 * standard JVM environments), a static initialiser catches the
 * {@link UnsatisfiedLinkError} and activates the JVM fallback,
 * which computes the 4D tesseract projection matrix and logs
 * the result.
 *
 * <p>The projection is modelled as the composition of two 4x4
 * orthogonal rotation matrices: R_XW (rotation in the XW plane)
 * and R_YZ (rotation in the YZ plane). The resulting matrix is
 * applied to the UTF-8 byte vector of the input message, and
 * the W-axis shadow is logged row by row.
 */
@Component
public class QuantumTesseractAdapter {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(
                    QuantumTesseractAdapter.class);

    /** Whether the native quantum co-processor library was loaded. */
    private static final boolean NATIVE_AVAILABLE;

    /** Spatial dimension of the tesseract (4D). */
    private static final int DIM = 4;

    /** Divisor for the XW rotation angle (yields PI/4 = 45 degrees). */
    private static final int THETA_XW_DIVISOR = 4;

    /** Divisor for the YZ rotation angle (yields PI/6 = 30 degrees). */
    private static final int THETA_YZ_DIVISOR = 6;

    /** Precomputed XW rotation angle in radians. */
    private static final double THETA_XW =
            Math.PI / THETA_XW_DIVISOR;

    /** Precomputed YZ rotation angle in radians. */
    private static final double THETA_YZ =
            Math.PI / THETA_YZ_DIVISOR;

    static {
        boolean loaded = false;
        try {
            System.loadLibrary("egds-quantum-tesseract");
            loaded = true;
        } catch (UnsatisfiedLinkError ignored) {
            // native library absent; JVM simulation active
        }
        NATIVE_AVAILABLE = loaded;
    }

    /**
     * Native entry point: projects a message onto the W-axis of
     * a 4D tesseract using the quantum co-processor.
     *
     * @param message  the input string to project
     * @param thetaXW  rotation angle (radians) in the XW plane
     * @param thetaYZ  rotation angle (radians) in the YZ plane
     * @return serialised projected vector as a double array
     */
    public native double[] projectNative(
            String message, double thetaXW, double thetaYZ);

    /**
     * Projects the supplied message through a 4D tesseract and
     * emits the resulting matrix operations to the log.
     *
     * <p>Delegates to {@link #projectNative} when the native
     * quantum library is available. Otherwise executes the JVM
     * simulation: computes R_XW x R_YZ, applies it to the
     * message byte vector, and logs the W-shadow row by row.
     *
     * @param message the greeting content to project
     */
    public void project(final String message) {
        if (NATIVE_AVAILABLE) {
            double[] projected =
                    projectNative(message, THETA_XW, THETA_YZ);
            LOG.info(
                    "Tesseract [native] W-shadow len={}",
                    projected.length);
        } else {
            simulateProjection(message);
        }
    }

    private void simulateProjection(final String message) {
        double[][] rXw = rotationXW(THETA_XW);
        double[][] rYz = rotationYZ(THETA_YZ);
        double[][] composed = multiply4x4(rXw, rYz);
        if (LOG.isInfoEnabled()) {
            LOG.info(
                    "Tesseract [JVM] projecting \"{}\" "
                            + "thetaXW={} thetaYZ={}",
                    message,
                    String.format("%.4f", THETA_XW),
                    String.format("%.4f", THETA_YZ));
            for (int row = 0; row < DIM; row++) {
                LOG.info(
                        "  R[{}] = [{}, {}, {}, {}]",
                        row,
                        String.format("%.6f", composed[row][0]),
                        String.format("%.6f", composed[row][1]),
                        String.format("%.6f", composed[row][2]),
                        String.format("%.6f",
                                composed[row][DIM - 1]));
            }
        }
        byte[] bytes =
                message.getBytes(StandardCharsets.UTF_8);
        double[] vector = new double[DIM];
        for (int i = 0;
                i < Math.min(bytes.length, DIM); i++) {
            vector[i] = bytes[i];
        }
        double[] projected =
                multiplyMatVec(composed, vector);
        LOG.info(
                "  W-shadow = [{}, {}, {}, {}]",
                String.format("%.4f", projected[0]),
                String.format("%.4f", projected[1]),
                String.format("%.4f", projected[2]),
                String.format("%.4f",
                        projected[DIM - 1]));
    }

    private static double[][] rotationXW(final double theta) {
        double c = Math.cos(theta);
        double s = Math.sin(theta);
        return new double[][] {
            {c,   0.0, 0.0, -s},
            {0.0, 1.0, 0.0, 0.0},
            {0.0, 0.0, 1.0, 0.0},
            {s,   0.0, 0.0, c}
        };
    }

    private static double[][] rotationYZ(final double theta) {
        double c = Math.cos(theta);
        double s = Math.sin(theta);
        return new double[][] {
            {1.0, 0.0, 0.0, 0.0},
            {0.0,  c,  -s,  0.0},
            {0.0,  s,   c,  0.0},
            {0.0, 0.0, 0.0, 1.0}
        };
    }

    private static double[][] multiply4x4(
            final double[][] a,
            final double[][] b) {
        double[][] result = new double[DIM][DIM];
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                for (int k = 0; k < DIM; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    private static double[] multiplyMatVec(
            final double[][] m,
            final double[] v) {
        double[] result = new double[DIM];
        for (int i = 0; i < DIM; i++) {
            for (int j = 0; j < DIM; j++) {
                result[i] += m[i][j] * v[j];
            }
        }
        return result;
    }

    /**
     * Returns whether the native quantum library was loaded.
     *
     * @return {@code true} if native mode is active
     */
    public static boolean isNativeAvailable() {
        return NATIVE_AVAILABLE;
    }
}
