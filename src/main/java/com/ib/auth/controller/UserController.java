package com.ib.auth.controller;

import com.ib.auth.dto.UserDto;
import com.ib.auth.security.JwtUser;
import com.ib.auth.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public void createUser(@RequestBody UserDto request) {

        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        userService.createUser(request, jwtUser);
    }

    @PutMapping("/{id}")
    public void updateUser(
            @PathVariable Long id,
            @RequestBody UserDto request
    ) {
        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        request.setId(id);
        userService.updateUser(request, jwtUser);
    }

    @GetMapping("/me")
    public UserDto getMyProfile() {

        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.getMyProfile(jwtUser);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        return userService.getUsers(jwtUser);
    }
}
