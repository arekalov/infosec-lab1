# Recipe API

REST API для хранения кулинарных рецептов. Лабораторная работа №1 по дисциплине
«Информационная безопасность», ИТМО.

Kotlin · Spring Boot 4 · Spring Security · H2 · Gradle

## Запуск

```bash
cp .env.example .env
echo "JWT_SECRET=$(openssl rand -base64 32)" >> .env
./gradlew bootRun
```

Нужен JDK 21. Приложение читает `.env` из корня проекта и слушает `http://localhost:8080`.

## Структура

Код разложен по слоям Clean Architecture, зависимости направлены внутрь, к домену.

| Слой | Содержимое |
|---|---|
| `domain` | Сущности, порты репозиториев, доменные исключения |
| `application` | Сценарии регистрации, входа и работы с рецептами, порты для токенов и хеширования |
| `infrastructure` | JPA, JWT, bcrypt, Spring Security |
| `presentation` | REST-контроллеры, DTO, мапперы, обработка ошибок |

## API

Все пути, кроме `/auth/*`, требуют заголовок `Authorization: Bearer <token>`.

| Метод | Путь | Назначение |
|---|---|---|
| `POST` | `/auth/register` | Регистрация |
| `POST` | `/auth/login` | Вход, выдача JWT |
| `GET` | `/api/data` | Свои рецепты, поиск `?q=` и пагинация `?page=&size=` |
| `POST` | `/api/recipes` | Создать рецепт |
| `GET` `PUT` `DELETE` | `/api/recipes/{id}` | Прочитать, изменить, удалить свой рецепт |

```bash
curl -X POST localhost:8080/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"correct-horse-battery"}'

TOKEN=$(curl -s -X POST localhost:8080/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"correct-horse-battery"}' \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["accessToken"])')

curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/data
```

## Меры защиты

**SQL-инъекции.** Запросы строятся только через Spring Data: derived-методы и JPQL
с именованными параметрами. Ввод пользователя передаётся как bind-параметр и не попадает
в текст SQL.

**XSS.** Текстовые поля экранируются OWASP Java Encoder при формировании ответа. Вход
проверяется по длине и формату, а заголовки `nosniff`, `X-Frame-Options: DENY` и CSP
`default-src 'none'` не дают браузеру исполнить ответ как страницу.

**Аутентификация.** Пароли хранятся только как bcrypt-хеш с cost 12. После входа выдаётся
JWT HS256 на 15 минут, ключ подписи берётся из окружения. Фильтр `JwtAuthFilter` проверяет
подпись, алгоритм и срок токена на каждом защищённом запросе и отвечает 401 при ошибке.

**Подбор пароля.** После 5 неудачных попыток вход блокируется на 5 минут. Неверный пароль
и несуществующий логин дают одинаковый ответ за одинаковое время.

**Чужие данные.** Каждый запрос к рецептам ограничен владельцем, на чужой рецепт API
отвечает 404.

**Зависимости.** Версии зафиксированы в `gradle.lockfile`, Tomcat и Jackson подняты выше
уязвимых версий из BOM Spring Boot.

## CI/CD

[`ci.yml`](.github/workflows/ci.yml) запускается на push в `main`, на pull request и вручную.
Одна job выполняет два сканера:

- **SAST: Semgrep** с правилами `p/kotlin`, `p/java` и `p/secrets`.
- **SCA: Trivy** по зависимостям из `gradle.lockfile`.

Любая находка Semgrep и уязвимость HIGH или CRITICAL валят сборку. Отчёты выводятся в сводку
прогона и сохраняются артефактом `security-reports`.
[Все прогоны](https://github.com/arekalov/infosec-lab1/actions/workflows/ci.yml).

![Прогон CI](docs/img/ci-run.png)

![Шаги SAST и SCA](docs/img/ci-steps.png)

![Список прогонов](docs/img/actions.png)

## Демонстрация

Коллекция [`docs/insomnia.json`](docs/insomnia.json) импортируется в Insomnia через
Import → File. Папки запускаются по порядку: аутентификация, работа с рецептами, проверки
защиты и удаление. Токен и id рецепта подставляются из ответов автоматически, ожидаемый
ответ описан у каждого запроса.

---

Рекалов Артём Олегович, группа P3409, 2026.
