# opm-jHub

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=alert_status)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=security_rating)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=coverage)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=alkoleft_opm-jhub&metric=ncloc)](https://sonarcloud.io/dashboard?id=alkoleft_opm-jhub)

Java версия хаба для пакетов One Script

## Реализованная функциональность

* Self-hosted (установка в локальной сети) репозиторий пакетов
* Совместимость с публичным opm хабом
* Зеркалирование публичного хаба
* Интеграция с github (загрузка релизов, при наличии ospx артефакта)
* REST API для управления репозиторием
  * Просмотр сущностей
  * Запуск и получение состояний фоновых заданий (синхронизаций)
* Хранение метаданных и пакетов в файловой системе

## Запуск и установка

Для запуска достаточно, загрузить исполняемый файл из [GitHub Releases](https://github.com/alkoleft/opm-jhub/releases)

И запустить 
`opm-jHub-1.0.0-boot.jar`
или
`java -jar opm-jHub-1.0.0-boot.jar`

Подробнее в [документации](https://alkoleft.github.io/opm-jhub/)