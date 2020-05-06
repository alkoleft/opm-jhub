# opm-jHub

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=alert_status)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=security_rating)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=coverage)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=ncloc)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)

Java версия хаба для пакетов One Script

## Основные цели разработки

* Свой внутренний репозиторий пакетов, в который можно складывать приватные пакеты
* Синхронизация (зеркалирование) публичного репозитория
* Сбор пакетов из исходников с гит сервера
* Продвинутый поиск пакетов

## Реализованная функциональность

* Совместимость с публичным opm хабом
* Зеркалирование публичного хаба
* Интеграция с github (загрузка релизов, при наличии ospx артефакта)
* REST API для управления репозиторием
  * Просмотр сущностей
  * Запуск и получение состояний фоновых заданий (синхронизаций)
* Хранение метаданных и пакетов в файловой системе

## Запуск и установка

Скачиваем исполняемые из [GitHub Releases](https://github.com/alkoleft/opm-jhub/releases)
*WinService.zip - архив для запуска в роли службы Windows

Запускаем файл `opm-jHub-boot.jar` - репозиторий запущен и готов к работе

### Для запуска в качестве службы

#### Windows

* Скачиваем из [GitHub Releases](https://github.com/alkoleft/opm-jhub/releases) архив opm-jHub-[версия]-WinService.zip
* Распаковываем в понравившийся каталог
* Запускам командную строку и переходим в каталог архива
* `opm-jHub.exe install` - устанавливаем службу ([Список команд]([opm-jHub.exe](https://github.com/winsw/winsw#usage)))

#### Linux

[Инструкция](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment-service)

## Настройка

* `application.properties` - базовые настройки приложения, настройки веб сервера и тд, [подробнее](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html)
  
  Основные настройки

    ``` properties
    application.properties

    logging.config=.\\logback.xml
    server.port=80
    hub.workpath=.\\data
    ```

  * `hub.workpath=.\\data` - рабочий каталог приложения (пакеты, метаданные, настройки)
  * `server.port=80` - порт веб-сервера
  * `logging.config=file:.\\logback.xml` - путь к файлу настроек логирования

* `[рабочий каталог]\settings\opm-hub-mirror.json` - настройки скачивания пакетов с opm хаба

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

* `[рабочий каталог]\settings\github.json` - настройки интеграции с github

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
