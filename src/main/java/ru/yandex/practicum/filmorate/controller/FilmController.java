package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable int id) {
        return filmService.getById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        Film createdFilm = filmService.create(film);

        log.info(
                "Создан фильм: id={}, name='{}'",
                createdFilm.getId(),
                createdFilm.getName()
        );

        return createdFilm;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        Film updatedFilm = filmService.update(film);

        log.info(
                "Обновлен фильм: id={}, name='{}'",
                updatedFilm.getId(),
                updatedFilm.getName()
        );

        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(
            @PathVariable int id,
            @PathVariable int userId
    ) {
        filmService.addLike(id, userId);

        log.info(
                "Пользователь id={} поставил лайк фильму id={}",
                userId,
                id
        );
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(
            @PathVariable int id,
            @PathVariable int userId
    ) {
        filmService.removeLike(id, userId);

        log.info(
                "Пользователь id={} удалил лайк у фильма id={}",
                userId,
                id
        );
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count
    ) {
        return filmService.getPopularFilms(count);
    }
}
