/**
 * Web3j-based blockchain integrity verification layer.
 *
 * <p>Simulates an Ethereum smart contract that stores Keccak-256
 * fingerprints of certified greeting payloads. The contract registry
 * is backed by an in-memory {@link java.util.concurrent.ConcurrentHashMap}
 * for local development and CI; production deployments replace this with
 * a Web3j-generated wrapper calling a deployed Solidity contract.
 *
 * <p>Key components:
 * <ul>
 *   <li>{@link com.egds.blockchain.Web3Config} — Web3j client bean</li>
 *   <li>{@link com.egds.blockchain.GreetingIntegrityVerifier} — register /
 *       verify operations against the mock contract</li>
 *   <li>{@link com.egds.blockchain.BlockchainIntegrityException} — thrown
 *       on hash mismatch or missing contract record</li>
 * </ul>
 */
package com.egds.blockchain;
