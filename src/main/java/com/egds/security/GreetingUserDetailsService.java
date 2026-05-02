package com.egds.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * {@link UserDetailsService} implementation for EGDS authentication.
 * Maintains a single statically-defined administrator account carrying
 * the {@code ROLE_GREETING_ADMIN} authority required for all greeting
 * delivery operations.
 *
 * <p>The administrator credential is encoded once at bean initialization
 * to avoid BCrypt cost per authentication request. Production deployments
 * must replace this in-memory implementation with a database-backed store.
 */
@Service
public class GreetingUserDetailsService implements UserDetailsService {

    /** The canonical login name for the EGDS administrator account. */
    static final String ADMIN_USERNAME = "greeting.admin";

    /** Plain-text credential for the admin account (local/CI only). */
    static final String ADMIN_RAW_PASSWORD = "egds-admin-pass";

    /** Pre-encoded BCrypt hash of the administrator password. */
    private final String encodedAdminPassword;

    /**
     * Initializes the service and pre-encodes the administrator credential.
     * BCrypt encoding is performed once here; {@code loadUserByUsername}
     * returns the pre-encoded value without re-encoding on each call.
     *
     * @param passwordEncoder the configured password encoder (BCrypt)
     */
    public GreetingUserDetailsService(
            final PasswordEncoder passwordEncoder) {
        this.encodedAdminPassword =
                passwordEncoder.encode(ADMIN_RAW_PASSWORD);
    }

    /**
     * Loads the {@link UserDetails} for the supplied username.
     * The EGDS administrator account carries the
     * {@code ROLE_GREETING_ADMIN} authority.
     *
     * @param username the login identifier; case-sensitive
     * @return the matching {@link UserDetails} instance
     * @throws UsernameNotFoundException if the username is not registered
     */
    @Override
    public UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException {
        if (!ADMIN_USERNAME.equals(username)) {
            throw new UsernameNotFoundException(
                    "No account registered for username: " + username);
        }
        return User.withUsername(ADMIN_USERNAME)
                .password(encodedAdminPassword)
                .roles("GREETING_ADMIN")
                .build();
    }
}
