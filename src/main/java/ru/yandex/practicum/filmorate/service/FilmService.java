package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(
            FilmStorage filmStorage,
            UserStorage userStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getAll() {
        return filmStorage.findAll();
    }

    public Film getById(int id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public void delete(int id) {
        filmStorage.delete(id);
    }

    public void addLike(int filmId, int userId) {
        Film film = filmStorage.findById(filmId);
        User user = userStorage.findById(userId);

        film.getLikes().add(user.getId());
        filmStorage.update(film);
    }

    public void removeLike(int filmId, int userId) {
        Film film = filmStorage.findById(filmId);
        User user = userStorage.findById(userId);

        film.getLikes().remove(user.getId());
        filmStorage.update(film);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted(
                        Comparator.comparingInt(
                                (Film film) -> film.getLikes().size()
                        ).reversed()
                )
                .limit(count)
                .toList();
    }
}
