package com.egds.ipfs;

import static org.assertj.core.api.Assertions.assertThat;

import com.egds.chaos.EccRecoveryFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link IpfsGreetingResolver} content-addressed storage.
 */
class IpfsGreetingResolverTest {

    private IpfsGreetingResolver resolver;

    /** Creates a fresh resolver backed by a real ECC filter. */
    @BeforeEach
    void setUp() {
        resolver = new IpfsGreetingResolver(new EccRecoveryFilter());
    }

    /** Verifies that stored content is resolved correctly. */
    @Test
    void storeAndResolve() {
        String cid = resolver.store("Hello, World!");
        assertThat(cid).isNotBlank();
        assertThat(resolver.resolve(cid))
            .isPresent()
            .hasValue("Hello, World!");
    }

    /** Verifies that an unknown CID returns an empty Optional. */
    @Test
    void unknownCidReturnsEmpty() {
        assertThat(resolver.resolve("nonexistent")).isNotPresent();
    }

    /** Verifies that a bit flip in ECC bytes is corrected on resolve. */
    @Test
    void resolveRecoversBitFlip() {
        String cid = resolver.store("Hello");
        byte[] eccBytes = resolver.getRawEccBytes(cid);
        eccBytes[0] ^= 0x01;
        resolver.putRawEccBytes(cid, eccBytes);
        assertThat(resolver.resolve(cid))
            .isPresent()
            .hasValue("Hello");
    }

    /** Verifies that the CID is deterministic for identical content. */
    @Test
    void cidIsDeterministic() {
        String cid1 = resolver.store("Hello");
        IpfsGreetingResolver other =
            new IpfsGreetingResolver(new EccRecoveryFilter());
        String cid2 = other.store("Hello");
        assertThat(cid1).isEqualTo(cid2);
    }

    /** Verifies that listCids returns all stored identifiers. */
    @Test
    void listCidsReturnsStoredEntries() {
        String cid1 = resolver.store("Alpha");
        String cid2 = resolver.store("Beta");
        assertThat(resolver.listCids()).contains(cid1, cid2);
    }
}
