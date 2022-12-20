package org.pjgg.temporal.examples.helloWorld;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.pjgg.temporal.QuarkusTemporalAppBuilder;
import org.pjgg.temporal.TemporalServerResource;
import org.pjgg.temporal.Utils;

import io.quarkus.test.QuarkusProdModeTest;

public class HelloWorldWorkflowTest {

    static TemporalServerResource temporalServer = new TemporalServerResource();

    @RegisterExtension
    static final QuarkusProdModeTest helloWorldWorkflow = new QuarkusTemporalAppBuilder(temporalServer)
            .withApplicationRoot((jar) -> jar.addClasses(Utils.findAllClassesFromSource()))
            .setApplicationName("helloWorldWorkflow")
            .setApplicationVersion("0.1-SNAPSHOT")
            .setCommandLineParameters("helloWorld", "workflow", "-i Maria")
            .setExpectExit(true).setRun(false);

    @RegisterExtension
    static final QuarkusProdModeTest helloWorldWorker = new QuarkusTemporalAppBuilder(temporalServer)
            .withApplicationRoot((jar) -> jar.addClasses(Utils.findAllClassesFromSource()))
            .setApplicationName("helloWorldWorker")
            .setApplicationVersion("0.1-SNAPSHOT")
            .setCommandLineParameters("helloWorld", "worker")
            .setExpectExit(true).setRun(false);

    @AfterAll
    public static void afterAll() {
        helloWorldWorker.stop();
    }

    @BeforeAll
    public static void beforeAll() {
        CompletableFuture.runAsync(helloWorldWorker::start);
    }

    @Test
    public void verifyHelloWorldFormatted() {
        helloWorldWorkflow.start();
        String name = "Maria";
        String expectedOutput = String.format("Hello %s!", name);
        assertThat(helloWorldWorkflow.getStartupConsoleOutput(), containsStringIgnoringCase(expectedOutput));
        assertTrue(helloWorldWorkflow.getExitCode() == 0, "Unexpected helloWorldWorkflow exit");
        helloWorldWorkflow.stop();
    }
}
