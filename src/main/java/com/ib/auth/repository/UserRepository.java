package com.ib.auth.repository;

import com.ib.auth.dto.UserDto;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    String sql = "select u.id, u.username, u.password, u.email, u.role, " +
            "u.first_name, u.last_name, u.phone_number, u.app_lang, u.app_row_per_page " +
            "from auth.users u ";
    String sqlCount = "select count(1) " +
            "from auth.users u ";

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
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setPhoneNumber(rs.getString("phone_number"));
        u.setAppLang(rs.getString("app_lang"));
        u.setAppRowPerPage(rs.getString("app_row_per_page"));
        u.setFullName(u.getFirstName() + " " + u.getLastName());
        return u;
    };

    private final RowMapper<UserDto> userWithoutPasswordMapper = (rs, rowNum) -> {
        UserDto u = new UserDto();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setPhoneNumber(rs.getString("phone_number"));
        u.setAppLang(rs.getString("app_lang"));
        u.setAppRowPerPage(rs.getString("app_row_per_page"));
        u.setFullName(u.getFirstName() + " " + u.getLastName());
        return u;
    };

    public List<UserDto> findAll() {
        return jdbcTemplate.query(
                "SELECT id, username, email, role FROM auth.users order by id",
                userWithoutPasswordMapper
        );
    }

    public List<UserDto> findByRole(String role) {
        String sql = "SELECT id, username, email, role FROM auth.users " +
                "where role = ? order by id";
        return jdbcTemplate.query(
                sql,
                userWithoutPasswordMapper,
                role
        );
    }

    public List<UserDto> findAll(int limit, int offset, String keyword) {
        String sqlSelect = sql;
        if (keyword != null && !keyword.equals("")) {
            keyword = keyword.toUpperCase();
            sqlSelect += " where upper(u.username) like CONCAT('%', ?, '%') or upper(u.first_name) like CONCAT('%', ?, '%') or upper(u.last_name) like CONCAT('%', ?, '%') " +
                    " LIMIT ? OFFSET ? ";
            return jdbcTemplate.query(sqlSelect,
                    new BeanPropertyRowMapper<>(UserDto.class),
                    keyword, keyword,
                    limit, offset);
        }
        sqlSelect += " LIMIT ? OFFSET ? ";

        return jdbcTemplate.query(sqlSelect,
                new BeanPropertyRowMapper<>(UserDto.class),
                limit, offset);
    }

    public int countAll(String keyword) {
        String sqlSelectCount = sqlCount;
        if (keyword != null && !keyword.equals("")) {
            keyword = keyword.toUpperCase();
            sqlSelectCount += " where upper(u.username) like CONCAT('%" + keyword + "%') or upper(u.first_name) like CONCAT('%" + keyword + "%') or upper(u.last_name) like CONCAT('%" + keyword + "%') ";
            return jdbcTemplate.queryForObject(sqlSelectCount, Integer.class);
        }
        return jdbcTemplate.queryForObject(sqlSelectCount, Integer.class);
    }

    public UserDto findProfileById(Long id) {
        String sql = "select id, username, password, email, role, first_name, last_name, phone_number, app_lang, app_row_per_page " +
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
                "SELECT id, username, email, password, role, first_name, last_name, phone_number, app_lang, app_row_per_page " +
                    "FROM auth.users WHERE username = ?",
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
                    "set email = ?, password = ?, updated_by = ?, updated_at = now() " +
                    "where id = ? ";
            return jdbcTemplate.update(
                    sql,
                    user.getEmail(),
                    user.getPassword(),
                    user.getUpdatedBy(),
                    user.getId()
            );
        } else {
            String sql = "update auth.users set email = ?, updated_by = ?, updated_at = now() " +
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
