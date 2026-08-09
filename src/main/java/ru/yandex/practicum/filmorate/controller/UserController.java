package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserStorage userStorage;

    @Autowired
    public UserController(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @GetMapping
    public Collection<User> getAll() {
        return userStorage.findAll();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        prepareUser(user);

        User createdUser = userStorage.create(user);

        log.info(
                "Создан пользователь: id={}, login='{}'",
                createdUser.getId(),
                createdUser.getLogin()
        );

        return createdUser;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        prepareUser(user);

        User updatedUser = userStorage.update(user);

        log.info(
                "Обновлен пользователь: id={}, login='{}'",
                updatedUser.getId(),
                updatedUser.getLogin()
        );

        return updatedUser;
    }

    private void prepareUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
