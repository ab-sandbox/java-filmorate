package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Film {

    private static final LocalDate CINEMA_BIRTHDAY =
            LocalDate.of(1895, 12, 28);

    private int id;

    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;

    @NotNull
    private LocalDate releaseDate;

    @Positive
    private int duration;

    @NotNull
    private Mpa mpa;

    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @Builder.Default
    private Set<Integer> likes = new HashSet<>();

    @AssertTrue(message = "Дата релиза фильма не может быть раньше 28 декабря 1895 года")
    public boolean isReleaseDateValid() {
        return releaseDate == null || !releaseDate.isBefore(CINEMA_BIRTHDAY);
    }
}
