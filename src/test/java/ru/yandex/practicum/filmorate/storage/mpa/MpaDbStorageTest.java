package ru.yandex.practicum.filmorate.storage.mpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(MpaDbStorage.class)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Autowired
    MpaDbStorageTest(MpaDbStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    @Test
    void shouldFindAllMpa() {
        Collection<Mpa> mpaRatings = mpaStorage.findAll();

        assertThat(mpaRatings)
                .containsExactly(
                        new Mpa(1, "G"),
                        new Mpa(2, "PG"),
                        new Mpa(3, "PG-13"),
                        new Mpa(4, "R"),
                        new Mpa(5, "NC-17")
                );
    }

    @Test
    void shouldFindMpaById() {
        Mpa mpa = mpaStorage.findById(3);

        assertThat(mpa)
                .isEqualTo(new Mpa(3, "PG-13"));
    }

    @Test
    void shouldThrowExceptionWhenMpaNotFound() {
        assertThatThrownBy(() -> mpaStorage.findById(999999))
                .isInstanceOf(NotFoundException.class);
    }
}
