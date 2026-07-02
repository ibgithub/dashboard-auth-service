package com.ib.auth.controller;

import com.ib.auth.dto.MenuDto;
import com.ib.auth.dto.RoleDto;
import com.ib.auth.security.JwtUser;
import com.ib.auth.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    // GET /api/roles - daftar semua role
    @GetMapping
    public List<RoleDto> getAllRoles() {
        return roleService.getAllRoles();
    }

    // GET /api/roles/{id} - detail role
    @GetMapping("/{id}")
    public RoleDto getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id);
    }

    // POST /api/roles - buat role baru
    @PostMapping
    public RoleDto createRole(@RequestBody RoleDto request) {
        String username = getCurrentUsername();
        return roleService.createRole(request, username);
    }

    // PUT /api/roles/{id} - update role
    @PutMapping("/{id}")
    public RoleDto updateRole(@PathVariable Long id, @RequestBody RoleDto request) {
        String username = getCurrentUsername();
        return roleService.updateRole(id, request, username);
    }

    // DELETE /api/roles/{id} - hapus role
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/roles/{id}/menus - daftar menu yang dimiliki role
    @GetMapping("/{id}/menus")
    public List<MenuDto> getRoleMenus(@PathVariable Long id) {
        return roleService.getRoleMenus(id);
    }

    // PUT /api/roles/{id}/menus - set menus untuk role (replace strategy)
    @PutMapping("/{id}/menus")
    public List<MenuDto> setRoleMenus(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> body
    ) {
        List<Long> menuIds = body.getOrDefault("menuIds", List.of());
        String username = getCurrentUsername();
        return roleService.setRoleMenus(id, menuIds, username);
    }

    private String getCurrentUsername() {
        JwtUser jwtUser = (JwtUser) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return jwtUser.getUsername();
    }
}
