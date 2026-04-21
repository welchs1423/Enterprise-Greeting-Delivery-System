package com.egds.web;

import com.egds.security.JwtTokenProvider;
import com.egds.web.dto.TokenRequest;
import com.egds.web.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for JWT token issuance.
 * Accepts username and password credentials and returns a signed JWT bearer token
 * valid for the configured expiration period ({@code egds.security.jwt.expiration}).
 *
 * <p>This endpoint is explicitly excluded from JWT authentication requirements
 * in {@link com.egds.config.SecurityConfig}; no bearer token is required to call it.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Authenticates the supplied credentials and issues a JWT bearer token.
     * Delegates authentication to the Spring Security {@link AuthenticationManager};
     * throws {@code AuthenticationException} on invalid credentials (HTTP 401).
     *
     * @param request the login credentials; both username and password must not be blank
     * @return HTTP 200 with a {@link TokenResponse} containing the signed JWT
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateToken(@RequestBody @Valid TokenRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new TokenResponse(token, jwtTokenProvider.getExpiration()));
    }
}
