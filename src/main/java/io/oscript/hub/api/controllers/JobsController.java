package io.oscript.hub.api.controllers;

import io.oscript.hub.api.jobs.JobStatus;
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

    @GetMapping("{name}/status")
    public ResponseEntity<JobStatus> getStatus(@PathVariable("name") String name) {
        return ResponseEntity.ok(taskRunner.statusTask(name));
    }

    @GetMapping("{name}/start")
    public ResponseEntity<JobStatus> startTask(@PathVariable("name") String name) {
        return ResponseEntity.ok(taskRunner.startTask(name));
    }
}
