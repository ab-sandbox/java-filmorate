package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage
    ) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAll() {
        return userStorage.findAll();
    }

    public User getById(int id) {
        return userStorage.findById(id);
    }

    public User create(User user) {
        prepareUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        prepareUser(user);
        return userStorage.update(user);
    }

    public void delete(int id) {
        userStorage.delete(id);
    }

    public void addFriend(int userId, int friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        return userStorage.findFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        return userStorage.findCommonFriends(userId, otherId);
    }

    private void prepareUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}
