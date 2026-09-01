package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({
        FilmDbStorage.class,
        UserDbStorage.class
})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Autowired
    FilmDbStorageTest(
            FilmDbStorage filmStorage,
            UserDbStorage userStorage
    ) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @Test
    void shouldCreateAndFindFilmById() {
        Film created = filmStorage.create(createFilm("Film"));

        Film found = filmStorage.findById(created.getId());

        assertThat(found)
                .usingRecursiveComparison()
                .isEqualTo(created);
    }

    @Test
    void shouldCreateFilmWithMpaAndGenres() {
        Film film = createFilm("Film");

        film.setMpa(new Mpa(3, null));
        film.setGenres(new LinkedHashSet<>(List.of(
                new Genre(1, null),
                new Genre(2, null)
        )));

        Film created = filmStorage.create(film);

        assertThat(created.getMpa())
                .isEqualTo(new Mpa(3, "PG-13"));

        assertThat(created.getGenres())
                .containsExactly(
                        new Genre(1, "Комедия"),
                        new Genre(2, "Драма")
                );
    }

    @Test
    void shouldFindAllFilms() {
        filmStorage.create(createFilm("First"));
        filmStorage.create(createFilm("Second"));

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void shouldUpdateFilm() {
        Film film = filmStorage.create(createFilm("Old name"));

        film.setName("New name");
        film.setDescription("New description");
        film.setDuration(200);
        film.setMpa(new Mpa(4, null));
        film.setGenres(new LinkedHashSet<>(List.of(
                new Genre(4, null),
                new Genre(6, null)
        )));

        Film updated = filmStorage.update(film);

        assertThat(updated.getName()).isEqualTo("New name");
        assertThat(updated.getDescription()).isEqualTo("New description");
        assertThat(updated.getDuration()).isEqualTo(200);
        assertThat(updated.getMpa())
                .isEqualTo(new Mpa(4, "R"));

        assertThat(updated.getGenres())
                .containsExactly(
                        new Genre(4, "Триллер"),
                        new Genre(6, "Боевик")
                );
    }

    @Test
    void shouldDeleteFilm() {
        Film film = filmStorage.create(createFilm("Film"));

        filmStorage.delete(film.getId());

        assertThatThrownBy(() -> filmStorage.findById(film.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenFilmNotFound() {
        assertThatThrownBy(() -> filmStorage.findById(999999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFindPopularFilmsByLikes() {
        User firstUser = createUser("first");
        User secondUser = createUser("second");

        Film firstFilm = filmStorage.create(createFilm("First"));
        Film secondFilm = filmStorage.create(createFilm("Second"));
        Film thirdFilm = filmStorage.create(createFilm("Third"));

        filmStorage.addLike(firstFilm.getId(), firstUser.getId());

        filmStorage.addLike(secondFilm.getId(), firstUser.getId());
        filmStorage.addLike(secondFilm.getId(), secondUser.getId());

        List<Film> popular = filmStorage.findPopular(3);

        assertThat(popular)
                .extracting(Film::getId)
                .containsExactly(
                        secondFilm.getId(),
                        firstFilm.getId(),
                        thirdFilm.getId()
                );
    }

    @Test
    void shouldRemoveLike() {
        User user = createUser("user");

        Film firstFilm = filmStorage.create(createFilm("First"));
        Film secondFilm = filmStorage.create(createFilm("Second"));

        filmStorage.addLike(secondFilm.getId(), user.getId());

        assertThat(filmStorage.findPopular(2))
                .extracting(Film::getId)
                .containsExactly(
                        secondFilm.getId(),
                        firstFilm.getId()
                );

        filmStorage.removeLike(secondFilm.getId(), user.getId());

        assertThat(filmStorage.findPopular(2))
                .extracting(Film::getId)
                .containsExactly(
                        firstFilm.getId(),
                        secondFilm.getId()
                );
    }

    @Test
    void shouldLimitPopularFilms() {
        filmStorage.create(createFilm("First"));
        filmStorage.create(createFilm("Second"));
        filmStorage.create(createFilm("Third"));

        List<Film> popular = filmStorage.findPopular(2);

        assertThat(popular).hasSize(2);
    }

    private Film createFilm(String name) {
        return Film.builder()
                .name(name)
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new Mpa(1, null))
                .genres(new LinkedHashSet<>(List.of(
                        new Genre(1, null)
                )))
                .build();
    }

    private User createUser(String login) {
        return userStorage.create(
                User.builder()
                        .email(login + "@example.com")
                        .login(login)
                        .name(login)
                        .birthday(LocalDate.of(1990, 1, 1))
                        .build()
        );
    }
}
