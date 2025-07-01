package org.example.expert.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.user.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void batchInsert(List<User> userList) {
        String sql = "insert into users (email, nickname, password, user_role) values (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql,
                userList,
                userList.size(),
                (PreparedStatement ps, User user) -> {
                    ps.setString(1, user.getEmail());
                    ps.setString(2, user.getNickname());
                    ps.setString(3, user.getPassword());
                    ps.setString(4, user.getUserRole().name());
                });
    }
}
