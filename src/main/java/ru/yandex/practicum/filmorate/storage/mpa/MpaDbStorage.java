package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@Component
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Mpa> findAll() {
        String sql = """
                SELECT mpa_id, name
                FROM mpa
                ORDER BY mpa_id
                """;

        return jdbcTemplate.query(sql, this::mapRow);
    }

    @Override
    public Mpa findById(int id) {
        String sql = """
                SELECT mpa_id, name
                FROM mpa
                WHERE mpa_id = ?
                """;

        return jdbcTemplate.query(sql, this::mapRow, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Рейтинг MPA с id=" + id + " не найден."
                ));
    }

    private Mpa mapRow(
            java.sql.ResultSet resultSet,
            int rowNum
    ) throws java.sql.SQLException {
        return new Mpa(
                resultSet.getInt("mpa_id"),
                resultSet.getString("name")
        );
    }
}
