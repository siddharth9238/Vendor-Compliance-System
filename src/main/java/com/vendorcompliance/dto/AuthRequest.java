package com.vendorcompliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 80, message = "Username must be between 1 and 80 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 120, message = "Password must be between 1 and 120 characters")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
