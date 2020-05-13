package io.oscript.hub.api.jobs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class JobInfo {
    String name;
    String description;
    JobStatus status;
    JobSettings settings;

    @JsonIgnore
    TaskContainer<Void> taskContainer;
}
