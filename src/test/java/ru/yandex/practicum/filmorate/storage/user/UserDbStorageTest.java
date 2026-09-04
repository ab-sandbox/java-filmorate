package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import({
        UserDbStorage.class,
        FilmDbStorage.class
})
class UserDbStorageTest {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;

    @Autowired
    UserDbStorageTest(
            UserDbStorage userStorage,
            FilmDbStorage filmStorage
    ) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Test
    void shouldCreateAndFindUserById() {
        User user = createUser("user@example.com", "user");

        User created = userStorage.create(user);
        User found = userStorage.findById(created.getId());

        assertThat(found)
                .usingRecursiveComparison()
                .isEqualTo(created);
    }

    @Test
    void shouldFindAllUsers() {
        userStorage.create(createUser("first@example.com", "first"));
        userStorage.create(createUser("second@example.com", "second"));

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void shouldUpdateUser() {
        User user = userStorage.create(
                createUser("old@example.com", "old")
        );

        user.setEmail("new@example.com");
        user.setLogin("new");
        user.setName("New name");

        User updated = userStorage.update(user);
        User found = userStorage.findById(updated.getId());

        assertThat(found.getEmail()).isEqualTo("new@example.com");
        assertThat(found.getLogin()).isEqualTo("new");
        assertThat(found.getName()).isEqualTo("New name");
    }

    @Test
    void shouldDeleteUser() {
        User user = userStorage.create(
                createUser("user@example.com", "user")
        );

        userStorage.delete(user.getId());

        assertThat(userStorage.existsById(user.getId())).isFalse();
    }

    @Test
    void shouldDeleteUserWithLikesAndFriendships() {
        User user = userStorage.create(
                createUser("user@example.com", "user")
        );

        User friend = userStorage.create(
                createUser("friend@example.com", "friend")
        );

        User anotherUser = userStorage.create(
                createUser("another@example.com", "another")
        );

        Film film = filmStorage.create(createFilm("Film"));

        userStorage.addFriend(user.getId(), friend.getId());
        userStorage.addFriend(anotherUser.getId(), user.getId());

        filmStorage.addLike(film.getId(), user.getId());

        userStorage.delete(user.getId());

        assertThat(userStorage.existsById(user.getId()))
                .isFalse();

        assertThat(userStorage.findFriends(anotherUser.getId()))
                .isEmpty();

        assertThat(userStorage.existsById(friend.getId()))
                .isTrue();

        assertThat(userStorage.existsById(anotherUser.getId()))
                .isTrue();
    }

    @Test
    void shouldCheckUserExists() {
        User user = userStorage.create(
                createUser("user@example.com", "user")
        );

        assertThat(userStorage.existsById(user.getId())).isTrue();
        assertThat(userStorage.existsById(999999)).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        assertThatThrownBy(() -> userStorage.findById(999999))
                .isInstanceOf(
                        ru.yandex.practicum.filmorate.exception.NotFoundException.class
                );
    }

    @Test
    void shouldAddFriend() {
        User first = userStorage.create(
                createUser("first@example.com", "first")
        );

        User second = userStorage.create(
                createUser("second@example.com", "second")
        );

        userStorage.addFriend(first.getId(), second.getId());

        assertThat(userStorage.findFriends(first.getId()))
                .extracting(User::getId)
                .containsExactly(second.getId());
    }

    @Test
    void friendshipShouldBeOneWay() {
        User first = userStorage.create(
                createUser("first@example.com", "first")
        );

        User second = userStorage.create(
                createUser("second@example.com", "second")
        );

        userStorage.addFriend(first.getId(), second.getId());

        assertThat(userStorage.findFriends(first.getId()))
                .extracting(User::getId)
                .containsExactly(second.getId());

        assertThat(userStorage.findFriends(second.getId()))
                .isEmpty();
    }

    @Test
    void shouldRemoveFriend() {
        User first = userStorage.create(
                createUser("first@example.com", "first")
        );

        User second = userStorage.create(
                createUser("second@example.com", "second")
        );

        userStorage.addFriend(first.getId(), second.getId());
        userStorage.removeFriend(first.getId(), second.getId());

        assertThat(userStorage.findFriends(first.getId()))
                .isEmpty();
    }

    @Test
    void shouldFindCommonFriends() {
        User first = userStorage.create(
                createUser("first@example.com", "first")
        );

        User second = userStorage.create(
                createUser("second@example.com", "second")
        );

        User common = userStorage.create(
                createUser("common@example.com", "common")
        );

        userStorage.addFriend(first.getId(), common.getId());
        userStorage.addFriend(second.getId(), common.getId());

        assertThat(
                userStorage.findCommonFriends(
                        first.getId(),
                        second.getId()
                )
        )
                .extracting(User::getId)
                .containsExactly(common.getId());
    }

    @Test
    void shouldAllowAddingSameFriendTwice() {
        User first = userStorage.create(
                createUser("first@example.com", "first")
        );
        User second = userStorage.create(
                createUser("second@example.com", "second")
        );

        userStorage.addFriend(first.getId(), second.getId());
        userStorage.addFriend(first.getId(), second.getId());

        assertThat(userStorage.findFriends(first.getId()))
                .extracting(User::getId)
                .containsExactly(second.getId());
    }

    private User createUser(String email, String login) {
        return User.builder()
                .email(email)
                .login(login)
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    private Film createFilm(String name) {
        return Film.builder()
                .name(name)
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new Mpa(1, null))
                .build();
    }
}
