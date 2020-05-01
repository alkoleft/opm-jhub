package io.oscript.hub.api.jobs;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class JobStatus {
    String status;
    LocalDateTime startDate;
    LocalDateTime finishData;

    public JobStatus() {
    }

    public JobStatus(String status) {
        this.status = status;
    }
}
