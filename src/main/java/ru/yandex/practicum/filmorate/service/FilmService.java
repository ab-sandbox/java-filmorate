package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    public FilmService(
            FilmStorage filmStorage,
            UserStorage userStorage,
            MpaStorage mpaStorage,
            GenreStorage genreStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    public Collection<Film> getAll() {
        return filmStorage.findAll();
    }

    public Film getById(int id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        validateReferences(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateReferences(film);
        return filmStorage.update(film);
    }

    public void delete(int id) {
        filmStorage.delete(id);
    }

    public void addLike(int filmId, int userId) {
        userStorage.findById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        userStorage.findById(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findPopular(count);
    }

    private void validateReferences(Film film) {
        mpaStorage.findById(film.getMpa().getId());

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre ->
                    genreStorage.findById(genre.getId())
            );
        }
    }
}
