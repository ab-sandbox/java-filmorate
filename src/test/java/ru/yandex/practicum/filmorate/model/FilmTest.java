package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilmTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldPassValidationForValidFilm() {
        Film film = createValidFilm();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectBlankName() {
        Film film = createValidFilm().toBuilder()
                .name("")
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldAcceptDescriptionWith200Characters() {
        Film film = createValidFilm().toBuilder()
                .description("a".repeat(200))
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectDescriptionLongerThan200Characters() {
        Film film = createValidFilm().toBuilder()
                .description("a".repeat(201))
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldRejectZeroDuration() {
        Film film = createValidFilm().toBuilder()
                .duration(0)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldRejectNegativeDuration() {
        Film film = createValidFilm().toBuilder()
                .duration(-1)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldAcceptCinemaBirthday() {
        Film film = createValidFilm().toBuilder()
                .releaseDate(LocalDate.of(1895, 12, 28))
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectReleaseDateBeforeCinemaBirthday() {
        Film film = createValidFilm().toBuilder()
                .releaseDate(LocalDate.of(1895, 12, 27))
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    private Film createValidFilm() {
        return Film.builder()
                .name("Interstellar")
                .description("Science fiction")
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .build();
    }
}
