package com.egds.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtTokenProvider}.
 * No Spring context is loaded; the provider is instantiated directly to isolate
 * all tests to the JWT library behavior and the provider's own logic.
 */
class JwtTokenProviderTest {

    private static final String TEST_SECRET =
            "test-secret-key-for-junit-egds-system-minimum-256-bits";
    private static final long EXPIRATION_MS = 3_600_000L;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateToken produces a three-segment signed JWT string")
    void generateToken_producesSignedToken() {
        Authentication auth = buildAuthentication("greeting.admin", "ROLE_GREETING_ADMIN");

        String token = jwtTokenProvider.generateToken(auth);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extractUsername returns the subject embedded in the token")
    void extractUsername_returnsCorrectSubject() {
        Authentication auth = buildAuthentication("greeting.admin", "ROLE_GREETING_ADMIN");
        String token = jwtTokenProvider.generateToken(auth);

        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("greeting.admin");
    }

    @Test
    @DisplayName("extractRoles returns the authority list embedded in the token")
    void extractRoles_returnsCorrectRoles() {
        Authentication auth = buildAuthentication("greeting.admin", "ROLE_GREETING_ADMIN");
        String token = jwtTokenProvider.generateToken(auth);

        List<String> roles = jwtTokenProvider.extractRoles(token);

        assertThat(roles).containsExactly("ROLE_GREETING_ADMIN");
    }

    @Test
    @DisplayName("validateToken returns true for a freshly generated, unmodified token")
    void validateToken_returnsTrueForValidToken() {
        Authentication auth = buildAuthentication("greeting.admin", "ROLE_GREETING_ADMIN");
        String token = jwtTokenProvider.generateToken(auth);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken returns false when the signature segment is tampered")
    void validateToken_returnsFalseForTamperedSignature() {
        Authentication auth = buildAuthentication("greeting.admin", "ROLE_GREETING_ADMIN");
        String token = jwtTokenProvider.generateToken(auth);
        String tampered = token.substring(0, token.length() - 4) + "XXXX";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for a token with negative expiration (already expired)")
    void validateToken_returnsFalseForExpiredToken() {
        JwtTokenProvider shortLived = new JwtTokenProvider(TEST_SECRET, -1L);
        Authentication auth = buildAuthentication("greeting.admin", "ROLE_GREETING_ADMIN");
        String token = shortLived.generateToken(auth);

        assertThat(shortLived.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("validateToken returns false for a blank string")
    void validateToken_returnsFalseForBlankToken() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    private Authentication buildAuthentication(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
