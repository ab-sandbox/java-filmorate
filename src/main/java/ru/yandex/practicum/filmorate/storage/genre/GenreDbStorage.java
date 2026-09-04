package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Component
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Genre> findAll() {
        String sql = """
                SELECT genre_id, name
                FROM genres
                ORDER BY genre_id
                """;

        return jdbcTemplate.query(sql, this::mapRow);
    }

    @Override
    public Genre findById(int id) {
        String sql = """
                SELECT genre_id, name
                FROM genres
                WHERE genre_id = ?
                """;

        return jdbcTemplate.query(sql, this::mapRow, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Жанр с id=" + id + " не найден."
                ));
    }

    private Genre mapRow(
            ResultSet resultSet,
            int rowNum
    ) throws SQLException {
        return new Genre(
                resultSet.getInt("genre_id"),
                resultSet.getString("name")
        );
    }
}
