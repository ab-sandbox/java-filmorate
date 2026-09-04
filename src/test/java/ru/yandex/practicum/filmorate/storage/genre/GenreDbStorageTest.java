package ru.yandex.practicum.filmorate.storage.genre;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(GenreDbStorage.class)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Autowired
    GenreDbStorageTest(GenreDbStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    @Test
    void shouldFindAllGenres() {
        Collection<Genre> genres = genreStorage.findAll();

        assertThat(genres)
                .containsExactly(
                        new Genre(1, "Комедия"),
                        new Genre(2, "Драма"),
                        new Genre(3, "Мультфильм"),
                        new Genre(4, "Триллер"),
                        new Genre(5, "Документальный"),
                        new Genre(6, "Боевик")
                );
    }

    @Test
    void shouldFindGenreById() {
        Genre genre = genreStorage.findById(2);

        assertThat(genre)
                .isEqualTo(new Genre(2, "Драма"));
    }

    @Test
    void shouldThrowExceptionWhenGenreNotFound() {
        assertThatThrownBy(() -> genreStorage.findById(999999))
                .isInstanceOf(NotFoundException.class);
    }
}
