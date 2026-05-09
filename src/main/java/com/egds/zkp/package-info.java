/**
 * Mock zero-knowledge proof layer for EGDS greeting payloads.
 *
 * <p>Provides a simulated ZKP commitment scheme that generates a
 * cryptographic proof that the server knows the greeting message
 * without revealing its content. The commitment function is
 * {@code SHA-256(message || nonce)} and the verification key is
 * {@code SHA-256(commitment || EGDS_VK_SALT)}.
 *
 * <p>Key components:
 * <ul>
 *   <li>{@link com.egds.zkp.ZkpGreetingEncoder} — encodes a greeting
 *       into a commitment and verification key pair.</li>
 *   <li>{@link com.egds.zkp.ZkpGreetingProof} — immutable proof
 *       record returned by the encoder.</li>
 * </ul>
 */
package com.egds.zkp;
