package com.ib.auth.security;

public class JwtUser {

    private Long userId;
    private String username;
    private String role;

    public JwtUser(Long userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
