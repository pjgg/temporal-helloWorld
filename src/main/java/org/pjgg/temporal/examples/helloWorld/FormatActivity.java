package org.pjgg.temporal.examples.helloWorld;

import java.time.Duration;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;

@ActivityInterface
public interface FormatActivity {
    @ActivityMethod
    String composeGreeting(String name);

    // Define all available configurations here
    ActivityOptions FORMAT_ACTIVITY_DEFAULT_OPTS = ActivityOptions.newBuilder()
            .setScheduleToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumAttempts(2)
                    .build())
            .build();
}
