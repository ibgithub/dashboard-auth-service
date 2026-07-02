package com.ib.auth.controller;

import com.ib.auth.dto.MenuDto;
import com.ib.auth.service.RoleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final RoleService roleService;

    public MenuController(RoleService roleService) {
        this.roleService = roleService;
    }

    // GET /api/menus - daftar semua menu (untuk admin di halaman role management)
    @GetMapping
    public List<MenuDto> getAllMenus() {
        return roleService.getAllMenus();
    }
}
