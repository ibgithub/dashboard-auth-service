package com.ib.auth.repository;

import com.ib.auth.dto.UserDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<UserDto> userWithPasswordMapper = (rs, rowNum) -> {
        UserDto u = new UserDto();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setRole(rs.getString("role"));
        return u;
    };

    private final RowMapper<UserDto> userWithoutPasswordMapper = (rs, rowNum) -> {
        UserDto u = new UserDto();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        return u;
    };

    public List<UserDto> findAll() {
        return jdbcTemplate.query(
                "SELECT id, username, email, role FROM auth.users order by id",
                userWithoutPasswordMapper
        );
    }

    public UserDto findProfileById(Long id) {
        String sql = "select id, username, password, email, role " +
                "from auth.users where id = ? ";
        UserDto userDto = jdbcTemplate.queryForObject(
                sql,
                userWithPasswordMapper,
                id
        );

        return userDto;
    }

    public UserDto findByUsername(String username) {
        return jdbcTemplate.queryForObject(
                "SELECT id, username, email, password, role FROM auth.users WHERE username = ?",
                userWithPasswordMapper,
                username
        );
    }

    public int insert(UserDto user) {
        String sqlInsert = "INSERT INTO auth.users (username, email, password, role, created_by) " +
                "VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(
                sqlInsert,
                user.getUsername(),
                user.getEmail(),
                user.getPassword(), // sudah bcrypt
                user.getRole(),
                user.getCreatedBy()
        );
    }

    public int update(UserDto user) {
        if (user.getPassword() != null) {
            String sql = "update auth.users " +
                    "set email = ?, password = ?, updated_by = ?, updated_date = now() " +
                    "where id = ? ";
            return jdbcTemplate.update(
                    sql,
                    user.getEmail(),
                    user.getPassword(),
                    user.getUpdatedBy(),
                    user.getId()
            );
        } else {
            String sql = "update auth.users set email = ?, updated_by = ?, updated_date = now() " +
                    "where id = ? ";
            return jdbcTemplate.update(
                    sql,
                    user.getEmail(),
                    user.getUpdatedBy(),
                    user.getId()
            );
        }
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update(
                "DELETE FROM auth.users WHERE id = ?",
                id
        );
    }
}
