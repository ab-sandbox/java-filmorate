package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private static final LocalDate CINEMA_BIRTHDAY =
            LocalDate.of(1895, 12, 28);

    private final Map<Integer, Film> films = new LinkedHashMap<>();

    private int nextId = 1;

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        validateFilm(film);

        film.setId(generateId());
        films.put(film.getId(), film);

        log.info(
                "Создан фильм: id={}, name='{}'",
                film.getId(),
                film.getName()
        );

        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        validateFilm(film);

        if (!films.containsKey(film.getId())) {
            throw new ValidationException(
                    "Фильм с id=" + film.getId() + " не найден."
            );
        }

        films.put(film.getId(), film);

        log.info(
                "Обновлен фильм: id={}, name='{}'",
                film.getId(),
                film.getName()
        );

        return film;
    }

    private void validateFilm(Film film) {
        validateReleaseDate(film.getReleaseDate());
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate.isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException(
                    "Дата релиза фильма не может быть раньше 28 декабря 1895 года."
            );
        }
    }

    private int generateId() {
        return nextId++;
    }
}
