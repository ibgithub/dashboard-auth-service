package com.ib.auth.service;

import com.ib.auth.common.PageResult;
import com.ib.auth.dto.MenuDto;
import com.ib.auth.dto.RoleDto;
import com.ib.auth.repository.MenuRepository;
import com.ib.auth.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final MenuRepository menuRepository;

    public RoleService(RoleRepository roleRepository, MenuRepository menuRepository) {
        this.roleRepository = roleRepository;
        this.menuRepository = menuRepository;
    }

    // ==================== CRUD Role ====================

    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll();
    }

    public PageResult<RoleDto> findPaged(int page, int size, String keyword) {
        int offset = page * size;
        List<RoleDto> roles = roleRepository.findAll(size, offset, keyword);
        int total = roleRepository.countAll(keyword);

        // Populate menus untuk setiap role
        for (RoleDto role : roles) {
            role.setMenus(menuRepository.findByRoleId(role.getId()));
        }

        return new PageResult<>(roles, page, size, total);
    }

    public RoleDto getRoleById(Long id) {
        RoleDto role = roleRepository.findById(id);
        if (role == null) {
            throw new RuntimeException("Role tidak ditemukan");
        }
        role.setMenus(menuRepository.findByRoleId(id));
        return role;
    }

    public RoleDto createRole(RoleDto request, String createdBy) {
        if (roleRepository.findByRoleName(request.getRoleName()) != null) {
            throw new RuntimeException("Role sudah ada");
        }
        roleRepository.insert(request, createdBy);
        RoleDto created = roleRepository.findByRoleName(request.getRoleName());

        // Simpan menus kalau menuIds dikirim
        if (request.getMenuIds() != null && !request.getMenuIds().isEmpty()) {
            for (Long menuId : request.getMenuIds()) {
                menuRepository.insertRoleMenu(created.getId(), menuId, createdBy);
            }
        }
        created.setMenus(menuRepository.findByRoleId(created.getId()));
        return created;
    }

    public RoleDto updateRole(Long id, RoleDto request, String updatedBy) {
        RoleDto existing = roleRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("Role tidak ditemukan");
        }
        RoleDto duplicate = roleRepository.findByRoleName(request.getRoleName());
        if (duplicate != null && !duplicate.getId().equals(id)) {
            throw new RuntimeException("Role sudah ada");
        }
        request.setId(id);
        roleRepository.update(request, updatedBy);

        // Update menus kalau menuIds dikirim
        if (request.getMenuIds() != null) {
            menuRepository.deleteRoleMenus(id);
            for (Long menuId : request.getMenuIds()) {
                menuRepository.insertRoleMenu(id, menuId, updatedBy);
            }
        }

        RoleDto updated = roleRepository.findById(id);
        updated.setMenus(menuRepository.findByRoleId(id));
        return updated;
    }

    public void deleteRole(Long id) {
        RoleDto existing = roleRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("Role tidak ditemukan");
        }
        if (roleRepository.countUsersByRoleId(id) > 0) {
            throw new RuntimeException("Role masih digunakan oleh user");
        }
        roleRepository.deleteById(id);
    }

    // ==================== Assign Menus ke Role ====================

    public List<MenuDto> setRoleMenus(Long roleId, List<Long> menuIds, String createdBy) {
        RoleDto role = roleRepository.findById(roleId);
        if (role == null) {
            throw new RuntimeException("Role tidak ditemukan");
        }

        // Validasi menu_id ada di database
        for (Long menuId : menuIds) {
            if (menuRepository.findById(menuId) == null) {
                throw new RuntimeException("Menu tidak ditemukan: id=" + menuId);
            }
        }

        // Replace strategy
        menuRepository.deleteRoleMenus(roleId);
        for (Long menuId : menuIds) {
            menuRepository.insertRoleMenu(roleId, menuId, createdBy);
        }

        return menuRepository.findByRoleId(roleId);
    }

    // Get menus untuk role tertentu (flat list)
    public List<MenuDto> getRoleMenus(Long roleId) {
        return menuRepository.findByRoleId(roleId);
    }

    // ==================== Assign Roles ke User ====================

    public List<RoleDto> setUserRoles(Long userId, List<Long> roleIds, String createdBy) {
        for (Long roleId : roleIds) {
            if (roleRepository.findById(roleId) == null) {
                throw new RuntimeException("Role tidak ditemukan: id=" + roleId);
            }
        }
        roleRepository.deleteUserRoles(userId);
        for (Long roleId : roleIds) {
            roleRepository.insertUserRole(userId, roleId, createdBy);
        }
        return roleRepository.findRolesByUserId(userId);
    }

    // ==================== Get User Menus (untuk frontend) ====================

    /**
     * Ambil menu user (gabungan semua role, distinct).
     * Return dalam struktur tree: parent → children.
     */
    public List<MenuDto> getUserMenus(Long userId) {
        List<MenuDto> flatMenus = menuRepository.findByUserId(userId);
        return buildMenuTree(flatMenus);
    }

    public List<RoleDto> getUserRoles(Long userId) {
        return roleRepository.findRolesByUserId(userId);
    }

    // ==================== Get All Menus ====================

    public List<MenuDto> getAllMenus() {
        return menuRepository.findAll();
    }

    public PageResult<MenuDto> findMenusPaged(int page, int size, String keyword) {
        int offset = page * size;
        List<MenuDto> menus = menuRepository.findAll(size, offset, keyword);
        int total = menuRepository.countAll(keyword);
        return new PageResult<>(menus, page, size, total);
    }

    public MenuDto createMenu(MenuDto request) {
        if (menuRepository.findByCode(request.getCode()) != null) {
            throw new RuntimeException("Menu code sudah ada");
        }
        menuRepository.insert(request);
        return menuRepository.findByCode(request.getCode());
    }

    public MenuDto updateMenu(Long id, MenuDto request) {
        MenuDto existing = menuRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("Menu tidak ditemukan");
        }
        MenuDto duplicate = menuRepository.findByCode(request.getCode());
        if (duplicate != null && !duplicate.getId().equals(id)) {
            throw new RuntimeException("Menu code sudah ada");
        }
        request.setId(id);
        menuRepository.update(request);
        return menuRepository.findById(id);
    }

    public void deleteMenu(Long id) {
        MenuDto existing = menuRepository.findById(id);
        if (existing == null) {
            throw new RuntimeException("Menu tidak ditemukan");
        }
        // Kalau parent menu, cek apakah masih punya children
        if (existing.getParentCode() == null && menuRepository.countByParentCode(existing.getCode()) > 0) {
            throw new RuntimeException("Menu masih memiliki sub-menu");
        }
        menuRepository.deleteById(id);
    }

    public MenuDto getMenuById(Long id) {
        MenuDto menu = menuRepository.findById(id);
        if (menu == null) {
            throw new RuntimeException("Menu tidak ditemukan");
        }
        return menu;
    }

    // ==================== Helper: Build Tree ====================

    private List<MenuDto> buildMenuTree(List<MenuDto> flatMenus) {
        // Pisahkan parent dan children
        Map<String, MenuDto> parentMap = new LinkedHashMap<>();
        List<MenuDto> children = new ArrayList<>();

        for (MenuDto menu : flatMenus) {
            if (menu.getParentCode() == null) {
                menu.setChildren(new ArrayList<>());
                parentMap.put(menu.getCode(), menu);
            } else {
                children.add(menu);
            }
        }

        // Masukkan children ke parent masing-masing
        for (MenuDto child : children) {
            MenuDto parent = parentMap.get(child.getParentCode());
            if (parent != null) {
                parent.getChildren().add(child);
            }
        }

        // Hanya return parent yang punya children (atau parent yang menu_id-nya sendiri di-assign)
        return new ArrayList<>(parentMap.values());
    }
}
