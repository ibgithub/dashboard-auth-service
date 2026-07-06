package com.ib.auth.controller;

import com.ib.auth.common.ApiResponse;
import com.ib.auth.dto.JwtResponse;
import com.ib.auth.entity.LoginRequest;
import com.ib.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@RequestBody LoginRequest request) {
        String token = authService.login(
                request.getUsername(),
                request.getPassword()
        );
        JwtResponse jwt = new JwtResponse(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "SUCCESS", "auth.login.success", jwt));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(Authentication authentication) {
        Map<String, Object> data = Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "SUCCESS", "auth.me.success", data));
    }
}
