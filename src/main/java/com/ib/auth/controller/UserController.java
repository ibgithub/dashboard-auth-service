package com.ib.auth.controller;

import com.ib.auth.common.ApiResponse;
import com.ib.auth.common.PageResult;
import com.ib.auth.dto.ChangePasswordDto;
import com.ib.auth.dto.MenuDto;
import com.ib.auth.dto.RoleDto;
import com.ib.auth.dto.UserDto;
import com.ib.auth.security.JwtUser;
import com.ib.auth.service.RoleService;
import com.ib.auth.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
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
    public ResponseEntity<ApiResponse<PageResult<UserDto>>> users(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        PageResult<UserDto> result;

        result = userService.findPaged(page, size, keyword);

        ApiResponse<PageResult<UserDto>> response =
                new ApiResponse<>(
                        true,
                        "SUCCESS",
                        "Users fetched successfully",
                        result
                );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/byRole/{role}")
    public List<UserDto> getUsersByRole(@PathVariable String role) {
        return userService.getUsersByRole(role);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/me/password")
    public void changePasswordSelf(
            @RequestBody ChangePasswordDto request
    ) {
        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        userService.changePasswordSelf(jwtUser.getUserId(), request);
    }

    @PutMapping("/{id}/password")
    public void changePasswordByAdmin(
            @PathVariable Long id,
            @RequestBody ChangePasswordDto request
    ) {
        userService.changePasswordByAdmin(id, request);
    }

    // GET /api/users/me/menus - menu yang bisa diakses user yang login (tree structure)
    @GetMapping("/me/menus")
    public List<MenuDto> getMyMenus() {
        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return roleService.getUserMenus(jwtUser.getUserId());
    }

    // GET /api/users/me/roles - daftar role user yang login
    @GetMapping("/me/roles")
    public List<RoleDto> getMyRoles() {
        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return roleService.getUserRoles(jwtUser.getUserId());
    }

    // PUT /api/users/{id}/roles - set roles untuk user (admin only)
    @PutMapping("/{id}/roles")
    public List<RoleDto> setUserRoles(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> body
    ) {
        List<Long> roleIds = body.getOrDefault("roleIds", List.of());
        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return roleService.setUserRoles(id, roleIds, jwtUser.getUsername());
    }
}
