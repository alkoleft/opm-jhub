package io.oscript.hub.api.jobs;

import io.oscript.hub.api.integration.classicopmhub.ClassicHubIntegration;
import io.oscript.hub.api.integration.github.GithubIntegration;
import io.oscript.hub.api.storage.JSONSettingsProvider;
import io.oscript.hub.api.utils.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class JobsConfiguration implements SchedulingConfigurer {

    static final Logger logger = LoggerFactory.getLogger(JobsConfiguration.class);

    @Autowired
    TaskRunner runner;

    @Autowired
    JSONSettingsProvider settingsProvider;

    @Autowired
    private ApplicationContext context;

    final Map<String, JobInfo> jobs = new LinkedHashMap<>();

    @PostConstruct
    void setUp() throws IOException {
        List.of(githubSyncJob()
                , opmhubSyncJob()
        ).forEach(job ->
                jobs.put(job.getName(), job)
        );

        String settingsName = "jobs";

        String allJobsStrings = jobs.keySet().stream().sorted().collect(Collectors.joining("; "));
        List<JobSettings> jobsSettings = getSettings();

        for (var jobSettings : jobsSettings) {
            if (!jobs.containsKey(jobSettings.jobName)) {
                logger.error("Не найдено задание {}, список доступных заданий: {}", jobSettings.jobName, allJobsStrings);
            } else {
                jobs.get(jobSettings.jobName).settings = jobSettings;
            }
        }

        if (jobsSettings.isEmpty()) {
            jobsSettings = jobs.values().stream().map(JobInfo::getSettings).collect(Collectors.toList());
            settingsProvider.saveConfiguration(settingsName, jobsSettings);
            String settingsPreview = JSON.serialize(jobsSettings);

            logger.info("Использованы настройки по умолчанию:\n{}", settingsPreview);
        }
    }

    List<JobSettings> getSettings() throws IOException {
        return settingsProvider.getConfigurationList("jobs", JobSettings.class);
    }

    public TaskContainer<Void> getTask(String name) {
        if (jobs.containsKey(name)) {
            return jobs.get(name).taskContainer;
        } else {
            return null;
        }
    }

    public JobInfo getJob(String name) {
        return jobs.getOrDefault(name, null);
    }

    @Primary
    @Bean(destroyMethod = "shutdown")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(2);
        executor.setThreadNamePrefix("bgJob-");
        executor.initialize();
        return executor;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());

        for (var job : jobs.values()) {
            if (job.settings.schedule) {
                logger.info("Регистрация задачи {} в планировщике (cron: {})", job.name, job.settings.cron);
                taskRegistrar.addTriggerTask(
                        () -> {
                            logger.info("Запуск задачи {} по расписанию", job.name);
                            runner.startTask(job.getName(), LaunchType.SCHEDULED);
                        },
                        new CronTrigger(job.settings.cron)
                );
            }
        }
    }

    List<JobSettings> defaultSettings() {
        List<JobSettings> settings = new ArrayList<>();

        for (var jobName : jobs.keySet()) {
            settings.add(new JobSettings(jobName, "* * */2 * * ?"));
        }

        return settings;
    }

    JobInfo githubSyncJob() {
        JobInfo job = new JobInfo();
        job.name = "githubSync";
        job.description = "Загрузка пакетов с github";
        job.settings = new JobSettings(job.name, "0 0 */2 * * ?");
        job.taskContainer = new TaskContainer<>(new VoidTask(context.getBean(GithubIntegration.class)));
        return job;
    }

    JobInfo opmhubSyncJob() {
        JobInfo job = new JobInfo();
        job.name = "opmhubSync";
        job.description = "Загрузка пакетов с opm hub";
        job.settings = new JobSettings(job.name, "0 0 */2 * * ?");
        job.taskContainer = new TaskContainer<>(new VoidTask(context.getBean(ClassicHubIntegration.class)));
        return job;
    }
}
