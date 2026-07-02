package com.ib.auth.service;

import com.ib.auth.dto.RoleDto;
import com.ib.auth.dto.UserDto;
import com.ib.auth.exception.AuthenticationException;
import com.ib.auth.repository.RoleRepository;
import com.ib.auth.repository.UserRepository;
import com.ib.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String username, String password) {

        UserDto user = userRepository.findByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Username atau password salah");
        }

        // Ambil roles dari tabel user_roles
        List<RoleDto> userRoles = roleRepository.findRolesByUserId(user.getId());
        List<String> roleNames = userRoles.stream()
                .map(RoleDto::getRoleName)
                .toList();

        return jwtUtil.generateToken(user, roleNames);
    }
}
