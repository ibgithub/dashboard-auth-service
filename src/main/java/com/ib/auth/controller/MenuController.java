package com.ib.auth.controller;

import com.ib.auth.common.ApiResponse;
import com.ib.auth.common.PageResult;
import com.ib.auth.dto.MenuDto;
import com.ib.auth.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final RoleService roleService;

    public MenuController(RoleService roleService) {
        this.roleService = roleService;
    }

    // GET /api/menus?page=0&size=10&keyword= (paged, untuk halaman menu management)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<MenuDto>>> getMenusPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        PageResult<MenuDto> result = roleService.findMenusPaged(page, size, keyword);
        return ResponseEntity.ok(new ApiResponse<>(true, "SUCCESS", "Menus fetched successfully", result));
    }

    // GET /api/menus/all (tanpa paging, untuk checklist di role management)
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MenuDto>>> getAllMenus() {
        List<MenuDto> menus = roleService.getAllMenus();
        return ResponseEntity.ok(new ApiResponse<>(true, "SUCCESS", "Menus fetched successfully", menus));
    }
}
