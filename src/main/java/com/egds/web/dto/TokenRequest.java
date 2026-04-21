package com.egds.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the JWT token issuance endpoint ({@code POST /api/v1/auth/token}).
 * Both fields are mandatory; blank values are rejected by the validation layer.
 */
public class TokenRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    public TokenRequest() {
    }

    public TokenRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
