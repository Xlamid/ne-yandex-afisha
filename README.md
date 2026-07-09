# Ne yandex afisha

Не афишируем афиши.

## Структура проекта

- `event-manager-service/` - стартовый модуль сервиса на Spring Boot.
- `infra/` - compose-файлы для локального подъема PostgreSQL, Kafka и Redis.
- `docs/openapi/event-manager-openapi.yaml` - контракт API сервиса `event-manager-service`.
- `docs/openapi/event-notificator-openapi.yaml` - контракт API сервиса `event-notificator-service`.

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

Запуск тестов только модуля `event-manager-service`:

```bash
./gradlew :event-manager-service:test
```