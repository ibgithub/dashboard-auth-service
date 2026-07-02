package com.ib.auth.service;

import com.ib.auth.common.PageResult;
import com.ib.auth.dto.ChangePasswordDto;
import com.ib.auth.dto.UserDto;
import com.ib.auth.repository.UserRepository;
import com.ib.auth.security.JwtUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public void createUser(UserDto request, JwtUser loginUser) {

        // 🔐 bcrypt password
        request.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        request.setCreatedBy(loginUser.getUsername());

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
    }
    public UserDto getMyProfile(JwtUser loginUser) {
        return userRepository.findProfileById(loginUser.getUserId());
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll();
    }

    public PageResult<UserDto> findPaged(int page, int size, String keyword) {

        int offset = page * size;

        List<UserDto> users = userRepository.findAll(size, offset, keyword);
        int total = userRepository.countAll(keyword);

        return new PageResult<>(users, page, size, total);
    }

    public List<UserDto> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
    public UserDto getById(Long id) {
        return userRepository.findProfileById(id);
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
}
