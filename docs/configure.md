# Настройка

## Базовые настройки

`application.properties` - базовые настройки приложения, настройки веб сервера и тд, [подробнее](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html)
  
``` properties
application.properties

logging.config=.\\logback.xml
server.port=80
hub.workpath=.\\data
```

* `hub.workpath=data` - рабочий каталог приложения (пакеты, метаданные, настройки)
* `server.port=80` - порт веб-сервера
* `logging.config=logback.xml` - путь к файлу настроек логирования

## Настройка синхронизации с хабами opm

`[рабочий каталог]\settings\opm-hub-mirror.json` - настройки скачивания пакетов с opm хаба

``` json
{
    "servers":[
        "http://hub.oscript.io",
        "http://hub.oscript.ru"
    ],
    "channel": "stable"
}
```

Содержит список серверов и канал, в который необходимо складывать пакеты. Для того, чтоб скачанные пакеты были доступны при `opm install` необходимо указать `stable`

## Настройка фоновых заданий

`[рабочий каталог]\settings\jobs.json` - настройки фоновых заданий

```json
[ {
    "jobName" : "githubSync",
    "schedule" : false,
    "cron" : "0 10 */2 * * ?"
}, {
    "jobName" : "opmhubSync",
    "schedule" : true,
    "cron" : "0 0 */2 * * ?"
} ]
```

* `jobName` - имя задания
  * `opmhubSync` - синхронизация с хабами opm
  * `githubSync` - синхронизация с github, по умолчанию отключена
* `schedule` - признак запуска по рассписанию
* `cron` - рассписание ([Подробнее](https://www.baeldung.com/cron-expressions#cron-expression))

## Настройка синхронизации с github

`[рабочий каталог]\settings\github.json` - настройки интеграции с github

``` json
{
    "token": "token",
    "collectPreReleases": true,
    "ifFork": "BOTH",
    "organizations": [
        "oscript-library"
    ],
    "users": [],
    "repositories": [],
    "channel": "stable"
}
```

* `token` - токен для доступа к API github, [управление токенами](https://github.com/settings/tokens)
* `collectPreReleases` - признак обработки `Pre-release`, релиз кандидатов
* `organizations` - Список организаций github, репозитории которых, необходимо анализировать
* `repositories` - Список репозиториев, за которыми нужно следить
* `users` - Список пользователей github, репозитории которых, необходимо анализировать
* `ifFork` - Поведение, когда анализируемый репозиторий это форк.
  * `NOTHING` - Никаких дополнительный действий, добавляем репозиторий к списку обрабатываемых
  * `SOURCE` - Текущий репозиторий игнорируется, обрабатываем базовый репозиторий
  * `BOTH` - Анализируем оба репозитория, и текущий, и базовый
* `channel` - Канал, в который буду складываться найденные релизы
