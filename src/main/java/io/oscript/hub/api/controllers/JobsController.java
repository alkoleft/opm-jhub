package io.oscript.hub.api.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import io.oscript.hub.api.config.View;
import io.oscript.hub.api.jobs.JobInfo;
import io.oscript.hub.api.jobs.JobStatus;
import io.oscript.hub.api.jobs.LaunchType;
import io.oscript.hub.api.jobs.TaskRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("service/tasks")
public class JobsController {

    @Autowired
    TaskRunner taskRunner;

    @JsonView(View.Public.class)
    @GetMapping("{name}")
    public ResponseEntity<JobInfo> getJob(@PathVariable("name") String name) {
        return ResponseEntity.ok(taskRunner.jobInfo(name));
    }

    @GetMapping("{name}/status")
    public ResponseEntity<JobStatus> getStatus(@PathVariable("name") String name) {
        return ResponseEntity.ok(taskRunner.statusTask(name));
    }

    @GetMapping("{name}/start")
    public ResponseEntity<JobStatus> startTask(@PathVariable("name") String name) {
        return ResponseEntity.ok(taskRunner.startTask(name, LaunchType.MANUAL));
    }
}
