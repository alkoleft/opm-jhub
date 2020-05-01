package io.oscript.hub.api.jobs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Configuration
public class TaskRunner {

    Map<String, TaskContainer<Void>> tasks = new LinkedHashMap<>();

    @Autowired
    ApplicationContext context;

    @PostConstruct
    public void createTasks() {
        var tasksNames = context.getBeanNamesForType(VoidTask.class);
        for (String taskName : tasksNames) {
            VoidTask task = context.getBean(taskName, VoidTask.class);
            tasks.put(taskName, new TaskContainer<>(task));
        }
    }

    public JobStatus startTask(String taskName) {
        if (!tasks.containsKey(taskName)) {
            return new JobStatus("Задание с таким именем не обнаружено");
        }

        var task = tasks.get(taskName);

        boolean alreadyRunning = task.isRunning();
        try {
            task.run();
        } catch (JobAlreadyExecuting ignored) {
            alreadyRunning = true;
        }
        var status = task.getStatus();

        if (alreadyRunning) {
            status.status = "Уже запущенна";
        }

        return status;

    }

    public JobStatus statusTask(String taskName) {
        if (!tasks.containsKey(taskName)) {
            return new JobStatus("Задание с таким именем не обнаружено");
        }

        return tasks.get(taskName).getStatus();
    }

}
