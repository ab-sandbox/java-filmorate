package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class FilmDbStorage implements FilmStorage {

    private static final String FILM_SELECT = """
            SELECT f.film_id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.mpa_id,
                   m.name AS mpa_name
            FROM films f
            JOIN mpa m ON f.mpa_id = m.mpa_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> findAll() {
        String sql = FILM_SELECT + """
                ORDER BY f.film_id
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRow);
        loadGenres(films);

        return films;
    }

    @Override
    public Film findById(int id) {
        String sql = FILM_SELECT + """
                WHERE f.film_id = ?
                """;

        Film film = jdbcTemplate.query(sql, this::mapRow, id)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Фильм с id=" + id + " не найден."
                ));

        film.setGenres(findGenresByFilmId(id));

        return film;
    }

    @Override
    public Film create(Film film) {
        String sql = """
                INSERT INTO films (
                    name,
                    description,
                    release_date,
                    duration,
                    mpa_id
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS
            );

            statement.setString(1, film.getName());
            statement.setString(2, film.getDescription());
            statement.setObject(3, film.getReleaseDate());
            statement.setInt(4, film.getDuration());
            statement.setInt(5, film.getMpa().getId());

            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Не удалось получить id созданного фильма."
            );
        }

        film.setId(key.intValue());

        saveGenres(film.getId(), film.getGenres());

        return findById(film.getId());
    }

    @Override
    public Film update(Film film) {
        String sql = """
                UPDATE films
                SET name = ?,
                    description = ?,
                    release_date = ?,
                    duration = ?,
                    mpa_id = ?
                WHERE film_id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (updatedRows == 0) {
            throw new NotFoundException(
                    "Фильм с id=" + film.getId() + " не найден."
            );
        }

        deleteGenres(film.getId());
        saveGenres(film.getId(), film.getGenres());

        return findById(film.getId());
    }

    @Override
    public void delete(int id) {
        findById(id);

        deleteGenres(id);
        deleteLikes(id);

        String sql = """
            DELETE FROM films
            WHERE film_id = ?
            """;

        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        findById(filmId);

        String sql = """
                INSERT INTO film_likes (film_id, user_id)
                VALUES (?, ?)
                """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        findById(filmId);

        String sql = """
                DELETE FROM film_likes
                WHERE film_id = ?
                  AND user_id = ?
                """;

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> findPopular(int count) {
        String sql = """
                SELECT f.film_id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       m.mpa_id,
                       m.name AS mpa_name,
                       COUNT(fl.user_id) AS likes_count
                FROM films f
                JOIN mpa m
                  ON f.mpa_id = m.mpa_id
                LEFT JOIN film_likes fl
                  ON f.film_id = fl.film_id
                GROUP BY f.film_id,
                         f.name,
                         f.description,
                         f.release_date,
                         f.duration,
                         m.mpa_id,
                         m.name
                ORDER BY likes_count DESC,
                         f.film_id
                LIMIT ?
                """;

        List<Film> films = jdbcTemplate.query(
                sql,
                this::mapRow,
                count
        );

        loadGenres(films);

        return films;
    }

    private Film mapRow(
            ResultSet resultSet,
            int rowNum
    ) throws SQLException {
        Mpa mpa = new Mpa(
                resultSet.getInt("mpa_id"),
                resultSet.getString("mpa_name")
        );

        return Film.builder()
                .id(resultSet.getInt("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .mpa(mpa)
                .build();
    }

    private Set<Genre> findGenresByFilmId(int filmId) {
        String sql = """
                SELECT g.genre_id,
                       g.name
                FROM genres g
                JOIN film_genres fg
                  ON g.genre_id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.genre_id
                """;

        return new LinkedHashSet<>(
                jdbcTemplate.query(
                        sql,
                        (resultSet, rowNum) -> new Genre(
                                resultSet.getInt("genre_id"),
                                resultSet.getString("name")
                        ),
                        filmId
                )
        );
    }

    private void saveGenres(int filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO film_genres (film_id, genre_id)
                VALUES (?, ?)
                """;

        for (Genre genre : genres) {
            jdbcTemplate.update(
                    sql,
                    filmId,
                    genre.getId()
            );
        }
    }

    private void deleteGenres(int filmId) {
        String sql = """
                DELETE FROM film_genres
                WHERE film_id = ?
                """;

        jdbcTemplate.update(sql, filmId);
    }

    private void deleteLikes(int filmId) {
        String sql = """
                DELETE FROM film_likes
                WHERE film_id = ?
                """;

        jdbcTemplate.update(sql, filmId);
    }

    private void loadGenres(List<Film> films) {
        films.forEach(film ->
                film.setGenres(findGenresByFilmId(film.getId()))
        );
    }
}
