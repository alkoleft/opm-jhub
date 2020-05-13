# Установка

Приложение построено на фреймворке **Spring Boot**, содержит **встроенный веб сервер** (apache tomcat), а также в jar файл внедрен скрипт запуска.

А если проще, скачал запустил и оно работает.

Исполняемые файла вы найдете в [GitHub Releases](https://github.com/alkoleft/opm-jhub/releases)

* `opm-jHub-[версия]-WinService.zip` - приложение и файлы для запуска как службы Windows
* `opm-jHub-[версия]-boot.jar` - готовое приложение для запуска в Linux (или Windows, если установлен bash)


Ниже я опишу, что необходимо для запуска в виде сервиса/демона

## Требования

* Java 11, скачать можно [здесь](https://libericajdk.ru/java11.html)

### Запуск службы Windows

1. Распаковываем архив `opm-jHub-[версия]-WinService.zip` в понравившийся каталог
2. Запускам командную строку и переходим в каталог архива
3. `opm-jHub.exe install` - регистрируем службу ([Список возможных команд]([opm-jHub.exe](https://github.com/winsw/winsw#usage)))
4. `sc.exe start opm-jhub` - стартуем службу

Сервис установлен и запущен, по умолчанию он слушает порт 80

[Тут](http://localhost/packages) вы увидите увидите пустой список пакетов и это норма) Спустя некоторое время выполниться синхронизация с хабами opm. Для ручного запуска синхронизации необходимо выполнить [запрос](http://localhost/service/tasks/opmhubSync/start). Мониторить можно [тут](http://localhost/service/tasks/opmhubSync/status)

### Запуск службы Linux

[*Инструкция установки Spring Boot приложения в качестве службы*](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment-service)

1. Копируем файл `opm-jHub-[версия]-boot.jar` в `/var/opm-hub`

   \* *Можно использвать любой каталог*
2. Переходим в каталог `cd /var/opm-hub`
3. Создаем пользователя `adduser opm-jhub`
4. Если служба будет висеть на 80 порту необходимы root, для этого добавим пользователя в необходимую группу
   * `usermod -aG wheel opm-jhub` - CentOS
   * `usermod -aG sudo opm-jhub` - Ubuntu
5. Создаем файл настроек службы `opm-jhub.service`

    ```properties
    [Unit]
    Description=opm-jhub
    After=syslog.target

    [Service]
    User=opm-jhub
    ExecStart=/var/opm-hub/opm-jHub-1.0.0-boot.jar.jar
    SuccessExitStatus=143

    [Install]
    WantedBy=multi-user.target
    ```

6. Регистрируем службу `sudo ln -s /var/opm-hub/opm-jhub.service /etc/systemd/system/opm-jhub.service`
7. Автозапуск службы `sudo systemctl enable opm-jhub.service`

`curl http://localhost/packages`

выведет пустой список пакетов, спустя некоторое время выполниться синхронизация с хабами opm. Для запуска немедленной синхронизации:

`curl http://localhost/service/tasks/opmhubSync/start`
