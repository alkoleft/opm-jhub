package io.oscript.hub.api.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class TaskContainer<T> {

    JobStatus status = JobStatus
            .builder()
            .status("Не запускался")
            .build();

    private Throwable taskException;
    private CompletableFuture<T> future;
    private final SimpleTask<T> task;

    private static final Logger logger = LoggerFactory.getLogger(TaskContainer.class);

    public TaskContainer(SimpleTask<T> task) {
        this.task = task;
    }

    public void run(LaunchType type) throws JobAlreadyExecuting {

        logger.info("Попытка запуска задачи. {}", type);
        if (isRunning()) {
            logger.info("Задача уже запущенна");
            throw new JobAlreadyExecuting();
        }
        beforeStart();
        status.launchType = type;
        future = task.run();
        future.handle(this::onComplete);
        logger.info("Задача запущенна. {}", type);
    }

    private void beforeStart() {
        logger.info("Запуск задачи");
        status.status = "Исполняется";
        status.lastStartDate = LocalDateTime.now();
        status.lastFinishData = null;
        taskException = null;
    }

    private T onComplete(T result, Throwable exception) {

        status.lastFinishData = LocalDateTime.now();
        taskException = exception;
        if (exception == null) {
            logger.info("Задача завершена. {}", status.launchType);
            status.status = "Завершено успешно";
        } else {
            logger.info("Задача завершена с ошибкой", exception);
            status.status = "Завершено с ошибкой: " + taskException.getMessage();
        }
        return result;
    }

    public JobStatus getStatus() {

        return status;
    }

    public boolean isRunning() {
        return future != null && !future.isDone();
    }

}
