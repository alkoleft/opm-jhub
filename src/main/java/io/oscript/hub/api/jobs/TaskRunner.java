package io.oscript.hub.api.jobs;

import io.oscript.hub.api.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
@Configuration
@EnableScheduling
public class TaskRunner {

    @Autowired
    JobsConfiguration jobsConfiguration;


    public JobStatus startTask(String name, LaunchType launchType) {
        var job = getJob(name);
        var task = job.taskContainer;

        boolean alreadyRunning = task.isRunning();
        if (!alreadyRunning) {
            try {
                task.run(launchType);
            } catch (JobAlreadyExecuting e) {
                alreadyRunning = true;
            }
        }
        var status = task.getStatus();

        if (alreadyRunning) {
            status = status.copy();
            status.status = "Уже запущенна";
        }

        return status;

    }

    public JobStatus statusTask(String name) {
        var job = getJob(name);

        return job.taskContainer.getStatus();
    }

    public JobInfo jobInfo(String name) {
        var job = getJob(name);

        job.status = statusTask(name);
        return job;
    }

    JobInfo getJob(String name) {
        var job = jobsConfiguration.getJob(name);

        if (job == null) {
            throw new EntityNotFoundException(String.format("Задание с именем %s не обнаружено", name));
        }

        return job;
    }

}
