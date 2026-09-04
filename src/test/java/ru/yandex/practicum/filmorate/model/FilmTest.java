package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldPassValidationForValidFilm() {
        assertTrue(isValid(createValidFilm()));
    }

    @Test
    void shouldRejectBlankName() {
        Film film = createValidFilm().toBuilder()
                .name("")
                .build();

        assertFalse(isValid(film));
    }

    @Test
    void shouldAcceptDescriptionWith200Characters() {
        Film film = createValidFilm().toBuilder()
                .description("a".repeat(200))
                .build();

        assertTrue(isValid(film));
    }

    @Test
    void shouldRejectDescriptionLongerThan200Characters() {
        Film film = createValidFilm().toBuilder()
                .description("a".repeat(201))
                .build();

        assertFalse(isValid(film));
    }

    @Test
    void shouldRejectZeroDuration() {
        Film film = createValidFilm().toBuilder()
                .duration(0)
                .build();

        assertFalse(isValid(film));
    }

    @Test
    void shouldRejectNegativeDuration() {
        Film film = createValidFilm().toBuilder()
                .duration(-1)
                .build();

        assertFalse(isValid(film));
    }

    @Test
    void shouldAcceptCinemaBirthday() {
        Film film = createValidFilm().toBuilder()
                .releaseDate(LocalDate.of(1895, 12, 28))
                .build();

        assertTrue(isValid(film));
    }

    @Test
    void shouldRejectReleaseDateBeforeCinemaBirthday() {
        Film film = createValidFilm().toBuilder()
                .releaseDate(LocalDate.of(1895, 12, 27))
                .build();

        assertFalse(isValid(film));
    }

    private boolean isValid(Film film) {
        return validator.validate(film).isEmpty();
    }

    private Film createValidFilm() {
        return Film.builder()
                .name("Interstellar")
                .description("Science fiction")
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .mpa(new Mpa(3, "PG-13"))
                .build();
    }
}
