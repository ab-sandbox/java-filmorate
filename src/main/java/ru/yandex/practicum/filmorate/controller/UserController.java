package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Integer, User> users = new LinkedHashMap<>();

    private int nextId = 1;

    @GetMapping
    public Collection<User> getAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        prepareUser(user);

        user.setId(generateId());
        users.put(user.getId(), user);

        log.info(
                "Создан пользователь: id={}, login='{}'",
                user.getId(),
                user.getLogin()
        );

        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        prepareUser(user);

        if (!users.containsKey(user.getId())) {
            log.warn(
                    "Попытка обновить несуществующего пользователя с id={}",
                    user.getId()
            );

            throw new ValidationException(
                    "Пользователь с id=" + user.getId() + " не найден."
            );
        }

        users.put(user.getId(), user);

        log.info(
                "Обновлен пользователь: id={}, login='{}'",
                user.getId(),
                user.getLogin()
        );

        return user;
    }

    private void prepareUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private int generateId() {
        return nextId++;
    }
}
