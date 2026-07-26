package ru.yandex.practicum.filmorate.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldPassValidationForValidUser() {
        assertTrue(isValid(createValidUser()));
    }

    @Test
    void shouldRejectBlankEmail() {
        User user = createValidUser().toBuilder()
                .email("")
                .build();

        assertFalse(isValid(user));
    }

    @Test
    void shouldRejectInvalidEmail() {
        User user = createValidUser().toBuilder()
                .email("not-an-email")
                .build();

        assertFalse(isValid(user));
    }

    @Test
    void shouldRejectBlankLogin() {
        User user = createValidUser().toBuilder()
                .login("")
                .build();

        assertFalse(isValid(user));
    }

    @Test
    void shouldAcceptBirthdayToday() {
        User user = createValidUser().toBuilder()
                .birthday(LocalDate.now())
                .build();

        assertTrue(isValid(user));
    }

    @Test
    void shouldRejectFutureBirthday() {
        User user = createValidUser().toBuilder()
                .birthday(LocalDate.now().plusDays(1))
                .build();

        assertFalse(isValid(user));
    }

    private boolean isValid(User user) {
        return validator.validate(user).isEmpty();
    }

    private User createValidUser() {
        return User.builder()
                .email("alex@example.com")
                .login("alex")
                .name("Alexey")
                .birthday(LocalDate.of(1985, 5, 10))
                .build();
    }
}
