package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

@Component
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> findAll() {
        String sql = """
                SELECT user_id, email, login, name, birthday
                FROM users
                ORDER BY user_id
                """;

        return jdbcTemplate.query(sql, this::mapRow);
    }

    @Override
    public User findById(int id) {
        String sql = """
                SELECT user_id, email, login, name, birthday
                FROM users
                WHERE user_id = ?
                """;

        return jdbcTemplate.query(sql, this::mapRow, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id=" + id + " не найден."
                ));
    }

    @Override
    public boolean existsById(int id) {
        String sql = """
                SELECT COUNT(*)
                FROM users
                WHERE user_id = ?
                """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                id
        );

        return count != null && count > 0;
    }

    @Override
    public User create(User user) {
        String sql = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, user.getEmail());
            statement.setString(2, user.getLogin());
            statement.setString(3, user.getName());
            statement.setObject(4, user.getBirthday());

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Не удалось получить id созданного пользователя."
            );
        }

        user.setId(key.intValue());

        return user;
    }

    @Override
    public User update(User user) {
        String sql = """
                UPDATE users
                SET email = ?,
                    login = ?,
                    name = ?,
                    birthday = ?
                WHERE user_id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException(
                    "Пользователь с id=" + user.getId() + " не найден."
            );
        }

        return user;
    }

    @Override
    public void delete(int id) {
        findById(id);

        deleteLikes(id);
        deleteFriendships(id);

        String sql = """
                DELETE FROM users
                WHERE user_id = ?
                """;

        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        findById(userId);
        findById(friendId);

        String sql = """
                INSERT INTO friendships (
                    requester_id,
                    receiver_id
                )
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        findById(userId);
        findById(friendId);

        String sql = """
                DELETE FROM friendships
                WHERE requester_id = ?
                  AND receiver_id = ?
                """;

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> findFriends(int userId) {
        findById(userId);

        String sql = """
                SELECT u.user_id,
                       u.email,
                       u.login,
                       u.name,
                       u.birthday
                FROM users u
                JOIN friendships f
                  ON u.user_id = f.receiver_id
                WHERE f.requester_id = ?
                ORDER BY u.user_id
                """;

        return jdbcTemplate.query(
                sql,
                this::mapRow,
                userId
        );
    }

    @Override
    public List<User> findCommonFriends(int userId, int otherId) {
        findById(userId);
        findById(otherId);

        String sql = """
                SELECT u.user_id,
                       u.email,
                       u.login,
                       u.name,
                       u.birthday
                FROM friendships f1
                JOIN friendships f2
                  ON f1.receiver_id = f2.receiver_id
                JOIN users u
                  ON u.user_id = f1.receiver_id
                WHERE f1.requester_id = ?
                  AND f2.requester_id = ?
                ORDER BY u.user_id
                """;

        return jdbcTemplate.query(
                sql,
                this::mapRow,
                userId,
                otherId
        );
    }

    private User mapRow(
            ResultSet resultSet,
            int rowNum
    ) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("user_id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }

    private void deleteLikes(int userId) {
        String sql = """
                DELETE FROM film_likes
                WHERE user_id = ?
                """;

        jdbcTemplate.update(sql, userId);
    }

    private void deleteFriendships(int userId) {
        String sql = """
                DELETE FROM friendships
                WHERE requester_id = ?
                   OR receiver_id = ?
                """;

        jdbcTemplate.update(sql, userId, userId);
    }
}
