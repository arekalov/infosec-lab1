# Recipe API

REST API для хранения кулинарных рецептов. Лабораторная работа №1 по дисциплине
«Информационная безопасность», ИТМО.

Каждый пользователь работает только со своими рецептами. Доступ к данным — по JWT-токену,
выданному после входа.

Kotlin 2.3.21 · Spring Boot 4.1.1 · Spring Security 7.1.1 · Java 21 · H2 · Gradle 9.7.1

## Запуск

```bash
cp .env.example .env
echo "JWT_SECRET=$(openssl rand -base64 32)" >> .env
./scripts/run.sh
```

Нужен JDK 21. Приложение стартует на `http://localhost:8080`. Без `JWT_SECRET` запуск
прерывается намеренно, как и с ключом короче 32 байт или не в Base64.

При запуске из IDE задайте `JWT_SECRET` в переменных окружения конфигурации запуска:
IDE не читает `.env`.

## API

Публичны только `/auth/register` и `/auth/login`. Остальное требует заголовок
`Authorization: Bearer <token>`. Ошибки — в формате RFC 7807.

| Метод | Путь | Доступ | Назначение |
|---|---|---|---|
| `POST` | `/auth/register` | открыт | Регистрация: `{username, password}` |
| `POST` | `/auth/login` | открыт | Вход, выдача JWT |
| `GET` | `/api/data` | JWT | Список своих рецептов; `?q=`, `?page=`, `?size=` |
| `POST` | `/api/recipes` | JWT | Создать рецепт |
| `GET` | `/api/recipes/{id}` | JWT + владелец | Прочитать рецепт |
| `PUT` | `/api/recipes/{id}` | JWT + владелец | Обновить рецепт |
| `DELETE` | `/api/recipes/{id}` | JWT + владелец | Удалить рецепт |

```bash
curl -X POST http://localhost:8080/auth/register -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"correct-horse-battery-staple"}'

TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"correct-horse-battery-staple"}' \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["accessToken"])')

curl -X POST http://localhost:8080/api/recipes -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Борщ","description":"Классический борщ",
       "ingredients":["свёкла","капуста","говядина"],
       "instructions":"Сварить бульон, добавить овощи, тушить 40 минут.",
       "cookMinutes":120,"servings":6}'

curl -H "Authorization: Bearer $TOKEN" 'http://localhost:8080/api/data?q=борщ'
```

## Меры защиты

| Угроза | Решение | Где |
|---|---|---|
| SQL-инъекции (A03) | Derived-запросы Spring Data и JPQL с bind-параметрами; конкатенации SQL нет | [`RecipeRepository.kt`](src/main/kotlin/ru/itmo/infosec/recipes/repository/RecipeRepository.kt) |
| XSS (A03) | OWASP Java Encoder на выходе + валидация на входе + `nosniff`, `X-Frame-Options`, CSP | [`RecipeMapper.kt`](src/main/kotlin/ru/itmo/infosec/recipes/web/RecipeMapper.kt), [`SecurityConfig.kt`](src/main/kotlin/ru/itmo/infosec/recipes/config/SecurityConfig.kt) |
| Хранение паролей (A07) | bcrypt, cost 12; открытый пароль нигде не сохраняется | [`SecurityConfig.kt`](src/main/kotlin/ru/itmo/infosec/recipes/config/SecurityConfig.kt) |
| Аутентификация (A07) | JWT HS256 на 15 мин, ключ из окружения; свой фильтр поверх Nimbus `JwtDecoder`, алгоритм зафиксирован | [`JwtAuthFilter.kt`](src/main/kotlin/ru/itmo/infosec/recipes/security/JwtAuthFilter.kt), [`JwtConfig.kt`](src/main/kotlin/ru/itmo/infosec/recipes/config/JwtConfig.kt) |
| Перебор и разведка учёток | 5 попыток входа за 5 минут; ответ на неверный пароль неотличим от ответа на несуществующий логин, в том числе по времени | [`LoginRateLimiter.kt`](src/main/kotlin/ru/itmo/infosec/recipes/security/LoginRateLimiter.kt), [`AuthService.kt`](src/main/kotlin/ru/itmo/infosec/recipes/service/AuthService.kt) |
| Доступ к чужим данным (A01) | Выборка всегда ограничена владельцем; чужой рецепт отдаёт 404, а не 403 | [`RecipeRepository.kt`](src/main/kotlin/ru/itmo/infosec/recipes/repository/RecipeRepository.kt) |
| Утечка внутренних деталей | Наружу обезличенные сообщения, stacktrace только в лог | [`ApiExceptionHandler.kt`](src/main/kotlin/ru/itmo/infosec/recipes/web/ApiExceptionHandler.kt) |
| Уязвимые зависимости | `gradle.lockfile`; Tomcat 11.0.26 и Jackson 3.1.7 вместо уязвимых версий из BOM; консоль H2 отключена; SCA-проверка в CI | [`build.gradle.kts`](build.gradle.kts) |

## CI/CD

[`.github/workflows/ci.yml`](.github/workflows/ci.yml) — одна job «SAST и SCA». Она запускается
на каждый push в `main`, на каждый pull request и вручную кнопкой Run workflow во вкладке Actions.

| Шаг | Инструмент | Что проверяет |
|---|---|---|
| `SAST: Semgrep` | Semgrep 1.179.0, правила `p/kotlin` `p/java` `p/secrets` | Исходный код и захардкоженные секреты |
| `SCA: Trivy` | Trivy по `gradle.lockfile` | Известные уязвимости зависимостей, включая транзитивные |

Любая находка Semgrep и уязвимость уровня HIGH или CRITICAL у Trivy завершают job с ошибкой.
Отчёты обоих сканеров печатаются в лог job и хранятся 30 дней в артефакте `security-reports`
на странице прогона.

Сам пайплайн тоже защищён: actions закреплены по SHA коммита, у `GITHUB_TOKEN` только чтение
кода, версия Semgrep зафиксирована, время job ограничено 15 минутами.

Последний успешный прогон: [Actions → CI](https://github.com/arekalov/infosec-lab1/actions/workflows/ci.yml).

![Прогон CI: шаги Semgrep и Trivy](docs/img/ci-run.png)

![Список прогонов](docs/img/actions.png)

## Проверка API

```bash
./scripts/run.sh     # в первом терминале
./scripts/smoke.sh   # во втором: 31 проверка по живому API через curl
```

Скрипт проходит регистрацию и вход, CRUD, отказ без токена и с подделанной подписью,
лимит попыток входа, SQL-инъекции, экранирование XSS-нагрузки и доступ к чужим рецептам.

---

Рекалов Артём Олегович, группа P3409, 2026.
