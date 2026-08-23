## Схема базы данных

Для хранения данных приложения спроектирована реляционная база данных, отражающая существующую бизнес-логику `Filmorate`.

![ER-диаграмма базы данных](docs/database.png)

### Основные сущности

* `users` — пользователи приложения.
* `films` — фильмы.
* `mpa` — справочник возрастных рейтингов MPA.
* `genres` — справочник жанров.
* `film_genres` — связь фильмов и жанров (многие ко многим).
* `film_likes` — лайки пользователей фильмам.
* `friendships` — отношения дружбы между пользователями.
* `friendship_statuses` — справочник статусов дружбы.

### Особенности модели

Все таблицы имеют собственные первичные ключи.

Связи «многие ко многим» реализованы через отдельные таблицы:

* фильм ↔ жанр — `film_genres`;
* пользователь ↔ фильм — `film_likes`.

Для дружбы используется отдельная сущность `friendships`, которая хранит:

* инициатора запроса (`requester_id`);
* получателя запроса (`receiver_id`);
* текущий статус дружбы.

Статусы вынесены в отдельный справочник `friendship_statuses`, что позволяет хранить только допустимые значения состояний.

Для предотвращения дублирования связей используются уникальные ограничения на пары внешних ключей.

## Примеры основных запросов

### Получить всех пользователей

```sql
SELECT *
FROM users;
```

### Получить все фильмы

```sql
SELECT *
FROM films;
```

### Получить фильм вместе с рейтингом MPA

```sql
SELECT f.film_id,
       f.name,
       f.description,
       f.release_date,
       f.duration,
       m.name AS mpa_rating
FROM films AS f
JOIN mpa AS m ON f.mpa_id = m.mpa_id;
```

### Получить жанры фильма

```sql
SELECT g.name
FROM genres AS g
JOIN film_genres AS fg ON g.genre_id = fg.genre_id
WHERE fg.film_id = :filmId;
```

### Получить популярные фильмы

```sql
SELECT f.film_id,
       f.name,
       COUNT(fl.like_id) AS likes_count
FROM films AS f
LEFT JOIN film_likes AS fl ON f.film_id = fl.film_id
GROUP BY f.film_id,
         f.name
ORDER BY likes_count DESC
LIMIT :count;
```

### Получить друзей пользователя

```sql
SELECT u.*
FROM friendships AS fs
JOIN users AS u
    ON u.user_id =
       CASE
           WHEN fs.requester_id = :userId
               THEN fs.receiver_id
           ELSE fs.requester_id
       END
JOIN friendship_statuses AS fss
    ON fs.friendship_status_id = fss.friendship_status_id
WHERE (fs.requester_id = :userId
       OR fs.receiver_id = :userId)
  AND fss.name = 'CONFIRMED';
```

### Получить общих друзей двух пользователей

```sql
SELECT DISTINCT u.*
FROM users AS u
WHERE u.user_id IN (
    SELECT CASE
               WHEN requester_id = :userId
                   THEN receiver_id
               ELSE requester_id
           END
    FROM friendships AS fs
    JOIN friendship_statuses AS fss
        ON fs.friendship_status_id = fss.friendship_status_id
    WHERE (requester_id = :userId OR receiver_id = :userId)
      AND fss.name = 'CONFIRMED'
)
AND u.user_id IN (
    SELECT CASE
               WHEN requester_id = :otherId
                   THEN receiver_id
               ELSE requester_id
           END
    FROM friendships AS fs
    JOIN friendship_statuses AS fss
        ON fs.friendship_status_id = fss.friendship_status_id
    WHERE (requester_id = :otherId OR receiver_id = :otherId)
      AND fss.name = 'CONFIRMED'
);
```
