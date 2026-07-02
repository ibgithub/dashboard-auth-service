package com.ib.auth.repository;

import com.ib.auth.dto.MenuDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MenuRepository {

    private final JdbcTemplate jdbcTemplate;

    public MenuRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<MenuDto> menuMapper = (rs, rowNum) -> {
        MenuDto m = new MenuDto();
        m.setId(rs.getLong("id"));
        m.setCode(rs.getString("code"));
        m.setParentCode(rs.getString("parent_code"));
        m.setMenuKey(rs.getString("menu_key"));
        m.setPath(rs.getString("path"));
        m.setIcon(rs.getString("icon"));
        m.setSortOrder(rs.getInt("sort_order"));
        return m;
    };

    // Semua menu (untuk admin manage)
    public List<MenuDto> findAll() {
        return jdbcTemplate.query(
                "SELECT id, code, parent_code, menu_key, path, icon, sort_order " +
                        "FROM auth.menu ORDER BY sort_order, code",
                menuMapper
        );
    }

    // Menu yang dimiliki role tertentu
    public List<MenuDto> findByRoleId(Long roleId) {
        return jdbcTemplate.query(
                "SELECT m.id, m.code, m.parent_code, m.menu_key, m.path, m.icon, m.sort_order " +
                        "FROM auth.menu m " +
                        "JOIN auth.role_menus rm ON rm.menu_id = m.id " +
                        "WHERE rm.role_id = ? " +
                        "ORDER BY m.sort_order, m.code",
                menuMapper, roleId
        );
    }

    // Menu yang dimiliki user (gabungan semua role, distinct)
    public List<MenuDto> findByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT DISTINCT m.id, m.code, m.parent_code, m.menu_key, m.path, m.icon, m.sort_order " +
                        "FROM auth.menu m " +
                        "JOIN auth.role_menus rm ON rm.menu_id = m.id " +
                        "JOIN auth.user_roles ur ON ur.role_id = rm.role_id " +
                        "WHERE ur.user_id = ? " +
                        "ORDER BY m.sort_order, m.code",
                menuMapper, userId
        );
    }

    // Set menus untuk role (replace strategy)
    public void deleteRoleMenus(Long roleId) {
        jdbcTemplate.update("DELETE FROM auth.role_menus WHERE role_id = ?", roleId);
    }

    public void insertRoleMenu(Long roleId, Long menuId, String createdBy) {
        jdbcTemplate.update(
                "INSERT INTO auth.role_menus (role_id, menu_id, created_by) VALUES (?, ?, ?)",
                roleId, menuId, createdBy
        );
    }

    public MenuDto findById(Long id) {
        List<MenuDto> list = jdbcTemplate.query(
                "SELECT id, code, parent_code, menu_key, path, icon, sort_order " +
                        "FROM auth.menu WHERE id = ?",
                menuMapper, id
        );
        return list.isEmpty() ? null : list.get(0);
    }
}
