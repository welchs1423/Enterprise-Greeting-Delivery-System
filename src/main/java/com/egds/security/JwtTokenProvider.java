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

    private final String secret;
    private final long expiration;

    public JwtTokenProvider(
            @Value("${egds.security.jwt.secret}") String secret,
            @Value("${egds.security.jwt.expiration}") long expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    /**
     * Generates a signed JWT token from the supplied {@link Authentication} principal.
     * The token encodes the username as the subject and the granted authorities as a role list.
     *
     * @param authentication the authenticated principal; must not be null
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(Authentication authentication) {
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
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the role list from a validated JWT token.
     *
     * @param token the compact JWT string
     * @return list of authority strings embedded in the {@value #CLAIM_ROLES} claim
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) parseClaims(token).get(CLAIM_ROLES);
    }

    /**
     * Validates the token signature and expiration.
     *
     * @param token the compact JWT string
     * @return {@code true} if the token is well-formed, correctly signed, and not expired
     */
    public boolean validateToken(String token) {
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

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
