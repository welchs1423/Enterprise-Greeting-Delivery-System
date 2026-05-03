package com.egds.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Hash;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Blockchain-backed integrity verifier for EGDS greeting payloads.
 *
 * <p>Simulates an Ethereum smart contract that maintains a
 * {@code mapping(bytes32 => bytes32)} of correlation IDs to Keccak-256
 * content fingerprints. Two operations correspond to Solidity contract
 * functions:
 * <ul>
 *   <li>{@link #register(String, String)} — equivalent to the
 *       {@code certify(correlationId, keccak256(content))} write
 *       function; called by {@link com.egds.core.mapper.MessageMapper}
 *       at content-certification time.</li>
 *   <li>{@link #verify(String, String)} — equivalent to the
 *       {@code assertIntegrity(correlationId, keccak256(content))} view
 *       function; called by
 *       {@link com.egds.core.strategy.ConsoleOutputStrategy} immediately
 *       before writing to the output channel.</li>
 * </ul>
 *
 * <p>The in-memory {@link ConcurrentHashMap} replaces on-chain storage
 * for local development and CI. In production, delegate each call to a
 * Web3j contract wrapper generated from the deployed Solidity ABI.
 */
@Component
public class GreetingIntegrityVerifier {

    /** Logger for this component. */
    private static final Logger LOG =
            LoggerFactory.getLogger(GreetingIntegrityVerifier.class);

    /**
     * In-memory representation of the smart contract's
     * {@code mapping(bytes32 correlationId => bytes32 contentHash)}.
     */
    private final ConcurrentHashMap<String, String> contractState =
            new ConcurrentHashMap<>();

    /**
     * Records the Keccak-256 fingerprint of the supplied payload in the
     * mock contract state under the given correlation identifier.
     * Equivalent to calling the Solidity {@code certify} function.
     *
     * @param correlationId unique delivery identifier used as the
     *                      mapping key
     * @param content       the certified payload; its hash is stored,
     *                      not the raw string
     */
    public void register(
            final String correlationId, final String content) {
        String hash = Hash.sha3String(content);
        contractState.put(correlationId, hash);
        LOG.debug(
                "[BLOCKCHAIN] certify correlationId={} hash={}",
                correlationId, hash);
    }

    /**
     * Verifies that the Keccak-256 fingerprint of the supplied payload
     * matches the value stored in the mock contract for the given
     * correlation ID. Equivalent to calling the Solidity
     * {@code assertIntegrity} view function.
     *
     * @param correlationId unique delivery identifier
     * @param content       the payload to verify against the stored hash
     * @throws BlockchainIntegrityException if no contract record exists
     *         for the given ID, or if the recomputed hash does not match
     *         the stored fingerprint
     */
    public void verify(
            final String correlationId, final String content) {
        String storedHash = contractState.get(correlationId);
        if (storedHash == null) {
            throw new BlockchainIntegrityException(
                    "No integrity record in contract for correlationId: "
                            + correlationId,
                    correlationId);
        }
        String recomputed = Hash.sha3String(content);
        if (!storedHash.equals(recomputed)) {
            throw new BlockchainIntegrityException(
                    "Integrity violation: stored=" + storedHash
                            + " recomputed=" + recomputed
                            + " correlationId=" + correlationId,
                    correlationId);
        }
        LOG.debug(
                "[BLOCKCHAIN] integrity verified correlationId={}",
                correlationId);
    }
}
