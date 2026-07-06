package com.ib.auth.service;

import com.ib.auth.common.PageResult;
import com.ib.auth.dto.ChangePasswordDto;
import com.ib.auth.dto.RoleDto;
import com.ib.auth.dto.UserDto;
import com.ib.auth.repository.RoleRepository;
import com.ib.auth.repository.UserRepository;
import com.ib.auth.security.JwtUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void createUser(UserDto request, JwtUser loginUser) {

        // 🔐 bcrypt password
        request.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        request.setCreatedBy(loginUser.getUsername());

        // Default bahasa Indonesia kalau tidak diisi
        if (request.getAppLang() == null || request.getAppLang().isBlank()) {
            request.setAppLang("ID");
        }

        userRepository.insert(request);
    }
    public void updateUser(
            UserDto request,
            JwtUser loginUser
    ) {
        request.setUpdatedBy(loginUser.getUsername());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            request.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        } else {
            request.setPassword(null); // tanda: tidak update
        }

        userRepository.update(request);

        // Update roles kalau roleIds dikirim
        if (request.getRoleIds() != null) {
            roleRepository.deleteUserRoles(request.getId());
            for (Long roleId : request.getRoleIds()) {
                roleRepository.insertUserRole(request.getId(), roleId, loginUser.getUsername());
            }
        }
    }
    public UserDto getMyProfile(JwtUser loginUser) {
        UserDto user = userRepository.findProfileById(loginUser.getUserId());
        if (user != null) {
            user.setRoles(roleRepository.findRolesByUserId(user.getId()));
        }
        return user;
    }

    public void updateMyProfile(UserDto request) {
        request.setPassword(null);
        userRepository.update(request);
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll();
    }

    public PageResult<UserDto> findPaged(int page, int size, String keyword) {

        int offset = page * size;

        List<UserDto> users = userRepository.findAll(size, offset, keyword);
        int total = userRepository.countAll(keyword);

        // Populate roles untuk setiap user
        for (UserDto user : users) {
            List<RoleDto> roles = roleRepository.findRolesByUserId(user.getId());
            user.setRoles(roles);
        }

        return new PageResult<>(users, page, size, total);
    }

    public UserDto getById(Long id) {
        UserDto user = userRepository.findProfileById(id);
        if (user != null) {
            user.setRoles(roleRepository.findRolesByUserId(user.getId()));
        }
        return user;
    }
    public void changePasswordSelf(Long userId, ChangePasswordDto dto) {

        UserDto user = getById(userId);

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Password lama salah");
        }

        validateNewPassword(dto);

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.update(user);
    }

    public void changePasswordByAdmin(Long userId, ChangePasswordDto dto) {

        validateNewPassword(dto);

        UserDto user = getById(userId);
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.update(user);
    }
    private void validateNewPassword(ChangePasswordDto dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Password tidak sama");
        }
    }

    public void deleteUser(Long id) {
        UserDto user = userRepository.findProfileById(id);
        if (user == null) {
            throw new RuntimeException("User tidak ditemukan");
        }
        userRepository.deleteById(id);
    }
}
