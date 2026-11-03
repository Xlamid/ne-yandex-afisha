# Ne yandex afisha

Не афишируем афиши.


Сервис афиш, работающий по первому правилу бойцовского клуба: никому не рассказывать об афишах. Особенно внутри 
бойцовского клуба. Здесь не кричат о событиях, когда хотят развесить баннеры. Все тихо, строго и только для своих.

Платформа позволяет регистрировать локации, организовывать мероприятия и бронировать на них места без лишней огласки. 
Если планы внезапно меняются - события молниеносно улетают в Kafka, а сервис нотификаций без лишнего шума и пыли
доставляет личные уведомления прямо в руки зарегистрированным участникам.

Нам ~~не~~ все равно.

## Структура проекта

- `event-manager-service/` - сервис управления событиями, местами проведения и регистрациями.
- `event-notificator-service/` - сервис уведомлений пользователей об изменениях событий через Kafka.
- `event-common/` - общая библиотека (DTO, события Kafka, модели исключений).
- `infra/` - compose-файлы для локального подъема PostgreSQL, Kafka и Redis.
- `event-manager-service/src/main/resources/static/event-manager-openapi.yaml` - контракт API сервиса `event-manager-service`.
- `event-notificator-service/src/main/resources/static/event-notificator-openapi.yaml` - контракт API сервиса `event-notificator-service`.

## Стек проекта

- Java 25
- Spring Boot
- Spring MVC
- Spring Data JPA
- Spring Security + JWT
- Liquibase
- PostgreSQL
- Kafka
- Redis
- Gradle
- Docker Compose
- OpenAPI / Swagger

## Сборка и запуск

Сборка всего проекта:

```bash
./gradlew clean build
```

Запуск `event-manager-service`:

```bash
./gradlew :event-manager-service:bootRun
```

Запуск `event-notificator-service`:

```bash
./gradlew :event-notificator-service:bootRun
```

Запуск тестов:

```bash
# Все тесты
./gradlew test

# Тесты event-manager-service
./gradlew :event-manager-service:test

# Тесты event-notificator-service
./gradlew :event-notificator-service:test
```

Локальный запуск инфраструктуры:

```bash
# PostgreSQL
docker compose -f infra/docker-compose.base.yaml up -d

# Kafka
docker compose -f infra/docker-compose.kafka.yaml up -d

# Redis
docker compose -f infra/docker-compose.redis.yaml up -d
```

## Доступ к сервисам и документации (dev-профиль)

- **`event-manager-service`**: порт `8080`
  - Swagger UI: `http://localhost:8080/swagger-ui.html`
  - Дефолтный администратор: логин `admin`, пароль `password`
- **`event-notificator-service`**: порт `8081`
  - Swagger UI: `http://localhost:8081/swagger-ui.html`

Порты локальной инфраструктуры:
- PostgreSQL (`event-manager`): `localhost:5434` (БД `postgres-event-manager`, `postgres` / `postgres`)
- PostgreSQL (`event-notificator`): `localhost:5435` (БД `postgres-event-notificator`, `postgres` / `postgres`)
- Kafka: `localhost:9093` (Zookeeper: `localhost:2182`)
- Redis: `localhost:6380`

## Кэширование и фоновые задачи

- **Режимы работы с кэшем (`cacheMode`)**: эндпоинты `/events` и `/locations` в `event-manager-service` поддерживают query-параметр `cacheMode`:
  - `SPRING` (по умолчанию) - работа через кэш Redis (Spring Cache) с настроенным TTL.
  - `DB` - прямое обращение к базе данных в обход кэша.
- **Отказоустойчивость (Fallback)**: при недоступности Redis сервисы логируют ошибку и автоматически переключаются на работу с базой данных без прерывания запросов.
- **Счетчик уведомлений**: `event-notificator-service` хранит в Redis актуальное количество непрочитанных уведомлений пользователя по ключу `notif:unread:{userId}`.
- **Фоновые задачи (Schedulers)**:
  - В `event-manager-service` каждую минуту (`scheduled.fixed-rate.event-status`) автоматически обновляются статусы начавшихся (`STARTED`) и завершившихся (`FINISHED`) событий со сбросом кэша и отправкой уведомлений в Kafka.
  - В `event-notificator-service` раз в сутки (`scheduled.fixed-rate.notifications`) автоматически удаляются прочитанные уведомления старше 7 дней и неиспользуемые payload-записи.

## Переменные окружения (prod-профиль)

Для запуска с профилем `prod` (`SPRING_PROFILES_ACTIVE=prod`) необходимо задать следующие переменные окружения:

| Переменная           | Описание                                  | Сервис                                               |
|----------------------|-------------------------------------------|------------------------------------------------------|
| `DB_URL`             | JDBC URL подключения к PostgreSQL         | `event-manager-service`, `event-notificator-service` |
| `DB_USERNAME`        | Имя пользователя БД                       | `event-manager-service`, `event-notificator-service` |
| `DB_PASSWORD`        | Пароль пользователя БД                    | `event-manager-service`, `event-notificator-service` |
| `KAFKA_HOST`         | Адрес брокера Kafka (`host:port`)         | `event-manager-service`, `event-notificator-service` |
| `NOTIFICATION_TOPIC` | Название топика уведомлений в Kafka       | `event-manager-service`, `event-notificator-service` |
| `REDIS_HOST`         | Хост Redis                                | `event-manager-service`, `event-notificator-service` |
| `REDIS_PORT`         | Порт Redis                                | `event-manager-service`, `event-notificator-service` |
| `JWT_SECRET_KEY`     | Ключ для подписи и проверки JWT           | `event-manager-service`, `event-notificator-service` |
| `DEFAULT_TTL`        | Время жизни кэша по умолчанию (в минутах) | `event-manager-service`                              |
| `LOCATION_TTL`       | Время жизни кэша локаций (в минутах)      | `event-manager-service`                              |
| `EVENT_TTL`          | Время жизни кэша событий (в минутах)      | `event-manager-service`                              |