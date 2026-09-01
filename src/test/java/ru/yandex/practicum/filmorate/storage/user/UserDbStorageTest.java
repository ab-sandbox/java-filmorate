package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(UserDbStorage.class)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Autowired
    UserDbStorageTest(UserDbStorage userStorage) {
        this.userStorage = userStorage;
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

    private User createUser(String email, String login) {
        return User.builder()
                .email(email)
                .login(login)
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }
}
