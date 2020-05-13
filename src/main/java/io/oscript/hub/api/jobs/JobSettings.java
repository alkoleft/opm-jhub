package io.oscript.hub.api.jobs;

import com.fasterxml.jackson.annotation.JsonView;
import io.oscript.hub.api.config.View;
import lombok.Data;

@Data
public class JobSettings {
    @JsonView(View.Internal.class)
    String jobName;
    boolean schedule;
    String cron;

    public JobSettings() {
    }

    public JobSettings(String jobName, String cron) {
        this.jobName = jobName;
        this.cron = cron;
        this.schedule = true;
    }
}
