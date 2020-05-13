package io.oscript.hub.api.jobs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class JobStatus {
    String status;
    LocalDateTime lastStartDate;
    LocalDateTime lastFinishData;
    LaunchType launchType;

    public JobStatus copy() {
        return toBuilder().build();
    }
}
