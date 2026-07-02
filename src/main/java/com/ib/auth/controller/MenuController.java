package com.ib.auth.controller;

import com.ib.auth.common.ApiResponse;
import com.ib.auth.dto.MenuDto;
import com.ib.auth.service.RoleService;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuDto>>> getAllMenus() {
        List<MenuDto> menus = roleService.getAllMenus();
        return ResponseEntity.ok(new ApiResponse<>(true, "SUCCESS", "Menus fetched successfully", menus));
    }
}
