package com.ib.auth.service;

import com.ib.auth.dto.UserDto;
import com.ib.auth.exception.AuthenticationException;
import com.ib.auth.repository.UserRepository;
import com.ib.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(            UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String username, String password) {

        UserDto user = userRepository.findByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Username atau password salah");
        }

        return jwtUtil.generateToken(user);
    }
}
