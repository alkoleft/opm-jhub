package io.oscript.hub.api.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class TaskContainer<T> {
    private LocalDateTime startDate;
    private LocalDateTime finishData;
    private Throwable taskException;
    private CompletableFuture<T> future;
    private final SimpleTask<T> task;

    private static final Logger logger = LoggerFactory.getLogger(TaskContainer.class);

    public TaskContainer(SimpleTask<T> task) {
        this.task = task;
    }

    public void run() throws JobAlreadyExecuting {

        logger.info("Попытка запуска задачи");
        if (isRunning()) {
            logger.info("Задача уже запущенна");
            throw new JobAlreadyExecuting();
        }
        beforeStart();
        future = task.run();
        future.handle(this::onComplete);
        logger.info("Задача запущенна");

    }

    private void beforeStart() {
        logger.info("Запуск задачи");
        startDate = LocalDateTime.now();
        finishData = null;
        taskException = null;
    }

    private T onComplete(T result, Throwable exception) {

        finishData = LocalDateTime.now();
        taskException = exception;
        if (exception == null) {
            logger.info("Задача завершена");
        } else {
            logger.info("Задача завершена с ошибкой", exception);
        }
        return result;
    }

    public JobStatus getStatus() {
        JobStatus status = new JobStatus();
        status.startDate = startDate;
        status.finishData = finishData;
        if (future == null) {
            status.status = "Не запускался";
        } else if (isRunning()) {
            status.status = "Исполняется";
        } else if (taskException == null) {
            status.status = "Завершено успешно";
        } else {
            status.status = "Завершено с ошибкой: " + taskException.getMessage();
        }

        return status;
    }

    public boolean isRunning() {
        return future != null && !future.isDone();
    }

}
