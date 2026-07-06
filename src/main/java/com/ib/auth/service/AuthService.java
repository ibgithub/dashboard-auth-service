package com.ib.auth.service;

import com.ib.auth.dto.RoleDto;
import com.ib.auth.dto.SettingParameterDto;
import com.ib.auth.dto.UserDto;
import com.ib.auth.exception.AuthenticationException;
import com.ib.auth.repository.RoleRepository;
import com.ib.auth.repository.SettingParameterRepository;
import com.ib.auth.repository.UserRepository;
import com.ib.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SettingParameterRepository settingRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       SettingParameterRepository settingRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.settingRepository = settingRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(String username, String password) {

        UserDto user = userRepository.findByUsername(username);

        if (user == null) {
            throw new AuthenticationException("Username atau password salah");
        }

        // Cek apakah akun di-block (status = 2)
        if (user.getStatus() != null && user.getStatus() == 2) {
            throw new AuthenticationException("Akun Anda diblokir. Hubungi administrator.");
        }

        // Cek password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // Increment failed count
            userRepository.incrementLoginFailedCount(user.getId());

            // Cek apakah sudah melebihi batas
            int maxWrongPassword = getMaxWrongPassword();
            int currentFailed = (user.getLoginFailedCount() != null ? user.getLoginFailedCount() : 0) + 1;

            if (currentFailed >= maxWrongPassword) {
                // Block akun
                userRepository.updateStatus(user.getId(), 2);
                throw new AuthenticationException("Akun Anda diblokir karena terlalu banyak percobaan login gagal. Hubungi administrator.");
            }

            throw new AuthenticationException("Username atau password salah. Sisa percobaan: " + (maxWrongPassword - currentFailed));
        }

        // Login sukses — reset failed count
        if (user.getLoginFailedCount() != null && user.getLoginFailedCount() > 0) {
            userRepository.resetLoginFailedCount(user.getId());
        }

        // Ambil roles dari tabel user_roles
        List<RoleDto> userRoles = roleRepository.findRolesByUserId(user.getId());
        List<String> roleNames = userRoles.stream()
                .map(RoleDto::getRoleName)
                .toList();

        return jwtUtil.generateToken(user, roleNames);
    }

    private int getMaxWrongPassword() {
        try {
            SettingParameterDto param = settingRepository.findByName("MAX_WRONG_PASSWORD");
            if (param != null) {
                return Integer.parseInt(param.getValue());
            }
        } catch (Exception e) {
            // fallback
        }
        return 5; // default
    }
}
