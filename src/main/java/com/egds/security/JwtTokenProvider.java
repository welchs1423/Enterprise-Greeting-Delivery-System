package com.egds.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT token lifecycle manager for the EGDS security layer.
 * Handles token generation, claims extraction, and signature validation
 * using HMAC-SHA256 with a configurable secret key.
 *
 * <p>Token payload structure:
 * <ul>
 *   <li>subject: authenticated username</li>
 *   <li>roles: list of granted authority strings</li>
 *   <li>iat / exp: issued-at and expiration timestamps</li>
 * </ul>
 */
@Component
public class JwtTokenProvider {

    /** JWT claim key for the list of granted authority strings. */
    public static final String CLAIM_ROLES = "roles";

    /** HMAC secret key value resolved from application properties. */
    private final String secret;

    /** Token expiration duration in milliseconds. */
    private final long expiration;

    /**
     * @param secretValue     the HMAC signing secret
     * @param expirationValue the token expiration duration in milliseconds
     */
    public JwtTokenProvider(
            @Value("${egds.security.jwt.secret}") final String secretValue,
            @Value("${egds.security.jwt.expiration}")
            final long expirationValue) {
        this.secret = secretValue;
        this.expiration = expirationValue;
    }

    /**
     * Generates a signed JWT token from the supplied
     * {@link Authentication} principal. Encodes the username as the
     * subject and the granted authorities as a role list.
     *
     * @param authentication the authenticated principal; must not be null
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(final Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expiration);

        return Jwts.builder()
                .subject(authentication.getName())
                .claim(CLAIM_ROLES, roles)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extracts the subject (username) from a validated JWT token.
     *
     * @param token the compact JWT string
     * @return the username embedded in the subject claim
     */
    public String extractUsername(final String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the role list from a validated JWT token.
     *
     * @param token the compact JWT string
     * @return list of authority strings in the {@value #CLAIM_ROLES} claim
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(final String token) {
        return (List<String>) parseClaims(token).get(CLAIM_ROLES);
    }

    /**
     * Validates the token signature and expiration.
     *
     * @param token the compact JWT string
     * @return {@code true} if well-formed, correctly signed, and not expired
     */
    public boolean validateToken(final String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns the configured token expiration duration in milliseconds.
     *
     * @return expiration duration
     */
    public long getExpiration() {
        return expiration;
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
    }
}
