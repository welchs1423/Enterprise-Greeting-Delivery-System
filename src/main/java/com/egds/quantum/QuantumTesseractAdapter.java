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

    private static final Logger log =
            LoggerFactory.getLogger(
                    QuantumTesseractAdapter.class);

    private static final boolean NATIVE_AVAILABLE;

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
        double thetaXW = Math.PI / 4.0;
        double thetaYZ = Math.PI / 6.0;
        if (NATIVE_AVAILABLE) {
            double[] projected =
                    projectNative(message, thetaXW, thetaYZ);
            log.info(
                    "Tesseract [native] W-shadow len={}",
                    projected.length);
        } else {
            simulateProjection(message, thetaXW, thetaYZ);
        }
    }

    private void simulateProjection(
            final String message,
            final double thetaXW,
            final double thetaYZ) {
        double[][] rXw = rotationXW(thetaXW);
        double[][] rYz = rotationYZ(thetaYZ);
        double[][] composed = multiply4x4(rXw, rYz);
        log.info(
                "Tesseract [JVM] projecting \"{}\" "
                        + "thetaXW={} thetaYZ={}",
                message,
                String.format("%.4f", thetaXW),
                String.format("%.4f", thetaYZ));
        for (int row = 0; row < 4; row++) {
            log.info(
                    "  R[{}] = [{}, {}, {}, {}]",
                    row,
                    String.format("%.6f", composed[row][0]),
                    String.format("%.6f", composed[row][1]),
                    String.format("%.6f", composed[row][2]),
                    String.format("%.6f", composed[row][3]));
        }
        byte[] bytes =
                message.getBytes(StandardCharsets.UTF_8);
        double[] vector = new double[4];
        for (int i = 0;
                i < Math.min(bytes.length, 4); i++) {
            vector[i] = bytes[i];
        }
        double[] projected =
                multiplyMatVec(composed, vector);
        log.info(
                "  W-shadow = [{}, {}, {}, {}]",
                String.format("%.4f", projected[0]),
                String.format("%.4f", projected[1]),
                String.format("%.4f", projected[2]),
                String.format("%.4f", projected[3]));
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
        double[][] result = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    private static double[] multiplyMatVec(
            final double[][] m,
            final double[] v) {
        double[] result = new double[4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
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
