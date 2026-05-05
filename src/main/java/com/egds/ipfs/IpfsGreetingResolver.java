package com.egds.ipfs;

import com.egds.chaos.EccRecoveryFilter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Content-addressed storage mock simulating IPFS behaviour.
 *
 * <p>Content is stored as Hamming(7,4) ECC-encoded bytes, indexed by a
 * SHA-256 content identifier (CID). The ECC encoding allows
 * {@link com.egds.chaos.CosmicRaySimulator} to inject single-bit errors
 * into the stored bytes while {@link EccRecoveryFilter#decode} corrects
 * them transparently on every {@link #resolve} call.
 */
@Component
public class IpfsGreetingResolver {

    private static final String HASH_ALGORITHM = "SHA-256";

    private final ConcurrentHashMap<String, byte[]> store =
        new ConcurrentHashMap<>();
    private final EccRecoveryFilter eccFilter;

    /**
     * Constructs the resolver with the given ECC filter.
     *
     * @param eccFilter Hamming(7,4) codec for encode and decode operations
     */
    public IpfsGreetingResolver(final EccRecoveryFilter eccFilter) {
        this.eccFilter = eccFilter;
    }

    /**
     * ECC-encodes the content and stores it under its SHA-256 CID.
     *
     * @param content greeting string to persist
     * @return SHA-256 hex CID for the stored content
     */
    public String store(final String content) {
        byte[] raw = content.getBytes(StandardCharsets.UTF_8);
        String cid = sha256Hex(raw);
        store.put(cid, eccFilter.encode(raw));
        return cid;
    }

    /**
     * Resolves and ECC-corrects content by CID.
     *
     * @param cid content identifier returned by {@link #store}
     * @return corrected content, or empty if the CID is not found
     */
    public Optional<String> resolve(final String cid) {
        byte[] eccBytes = store.get(cid);
        if (eccBytes == null) {
            return Optional.empty();
        }
        byte[] decoded = eccFilter.decode(eccBytes.clone());
        return Optional.of(
            new String(decoded, StandardCharsets.UTF_8));
    }

    /**
     * Returns a copy of the raw ECC bytes for a CID.
     * Intended for use by {@link com.egds.chaos.CosmicRaySimulator}.
     *
     * @param cid content identifier
     * @return ECC bytes, or {@code null} if the CID is not stored
     */
    public byte[] getRawEccBytes(final String cid) {
        byte[] bytes = store.get(cid);
        return bytes == null ? null : bytes.clone();
    }

    /**
     * Replaces the raw ECC bytes for an existing CID.
     * Used by {@link com.egds.chaos.CosmicRaySimulator} after bit injection.
     *
     * @param cid  content identifier
     * @param data replacement ECC bytes
     */
    public void putRawEccBytes(final String cid, final byte[] data) {
        if (store.containsKey(cid)) {
            store.put(cid, data.clone());
        }
    }

    /**
     * Returns all CIDs currently held in the store.
     *
     * @return array of CID strings (snapshot, not a live view)
     */
    public String[] listCids() {
        return store.keySet().toArray(new String[0]);
    }

    private String sha256Hex(final byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            return HexFormat.of().formatHex(md.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                HASH_ALGORITHM + " unavailable", e);
        }
    }
}
