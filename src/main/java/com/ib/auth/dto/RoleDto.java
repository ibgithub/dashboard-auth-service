package com.ib.auth.dto;

import java.util.List;

public class RoleDto {
    private Long id;
    private String roleName;
    private String description;
    private List<MenuDto> menus;
    private List<Long> menuIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MenuDto> getMenus() {
        return menus;
    }

    public void setMenus(List<MenuDto> menus) {
        this.menus = menus;
    }

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}
