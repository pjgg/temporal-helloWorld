package org.pjgg.temporal;

import static org.pjgg.temporal.TemporalCommons.HELLO_WORLD_TASK_QUEUE;
import static org.pjgg.temporal.TemporalWorker.newTemporalWorkerBuilder;
import static org.pjgg.temporal.TemporalWorkflow.newTemporalWorkflowBuilder;

import java.util.concurrent.Callable;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;
import org.pjgg.temporal.examples.helloWorld.FormatActivity;
import org.pjgg.temporal.examples.helloWorld.HelloWorldWorkflow;
import org.pjgg.temporal.examples.helloWorld.HelloWorldWorkflowImpl;

import com.google.common.base.Strings;
import com.google.protobuf.util.Durations;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.runtime.ShutdownEvent;
import io.temporal.api.workflowservice.v1.RegisterNamespaceRequest;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import picocli.CommandLine;

@TopCommand
@CommandLine.Command(name="example", mixinStandardHelpOptions = true, subcommands = {CreateNamespace.class, HelloWorldExample.class},
        version= "version 1.0",
        footerHeading = "2022 - Temporal.io Demo\n",
        headerHeading = "Quarkus Devs Tool\n",
        description = "Quarkus / Temporal.io integration example")
public class CommandCli { }

@CommandLine.Command(name = "helloWorld", description = "create namespace", subcommands = {HelloWorldStartWorkflow.class, HelloWorldStartWorker.class})
class HelloWorldExample { }

@CommandLine.Command(name = "new-namespace", description = "create namespace")
class CreateNamespace implements Runnable {

    @CommandLine.Parameters
    String namespace;

    @CommandLine.Option(names = {"-h", "--host"}, description = "temporal host", defaultValue = "127.0.0.1")
    String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "temporal port", defaultValue = "7233")
    String port;

    @Override
    public void run() {
        String endpoint = String.format("%s:%s", host.trim(), port.trim());
        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder().setTarget(endpoint).build());

        if(!Strings.isNullOrEmpty(namespace)) {
            RegisterNamespaceRequest request =
                    RegisterNamespaceRequest.newBuilder()
                            .setNamespace(namespace)
                            .setWorkflowExecutionRetentionPeriod(Durations.fromDays(7))
                            .build();

            service.blockingStub().registerNamespace(request);
        } else {
            throw new RuntimeException("Invalid namespace name.");
        }
    }
}

@CommandLine.Command(name = "workflow", description = "Start workflow")
class HelloWorldStartWorkflow implements Callable<String> {

    private static final Logger LOG = Logger.getLogger(HelloWorldStartWorkflow.class);

    @CommandLine.Option(names = {"-n", "--namespace"}, description = "set namespace that command applies", defaultValue = "default")
    String namespace;

    @CommandLine.Option(names = {"-h", "--host"}, description = "temporal host", defaultValue = "127.0.0.1")
    String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "temporal port", defaultValue = "7233")
    String port;

    @CommandLine.Option(names = {"-i", "--yourName"}, description = "your name", defaultValue = "World")
    String yourName;

    @Override
    public String call() {
        TemporalWorkflow workflow = newTemporalWorkflowBuilder(HELLO_WORLD_TASK_QUEUE)
                    .withTemporalServerHost(host)
                    .withTemporalServerPort(Integer.parseInt(port.trim()))
                    .withNamespace(namespace)
                    .build();

        HelloWorldWorkflow helloWorldWorkflow = workflow.getStub(HelloWorldWorkflow.class);
        String greeting = helloWorldWorkflow.getGreeting(yourName);
        LOG.info(greeting);
        return greeting;
    }
}

@CommandLine.Command(name = "worker", description = "Start worker")
class HelloWorldStartWorker implements Runnable {

    private static final Logger LOG = Logger.getLogger(HelloWorldStartWorker.class);

    @CommandLine.Option(names = {"-n", "--namespace"}, description = "set namespace that command applies", defaultValue = "default")
    String namespace;

    @CommandLine.Option(names = {"-h", "--host"}, description = "temporal host", defaultValue = "127.0.0.1")
    String host;

    @CommandLine.Option(names = {"-p", "--port"}, description = "temporal port", defaultValue = "7233")
    String port;

    @CommandLine.Option(names = {"-w", "--wait"}, description = "worker should hold listening", defaultValue = "true")
    String waitForExist;

    @Inject
    @Named("format-activity")
    FormatActivity formatActivity;

    @Override
    public void run() {
        TemporalWorker worker = newTemporalWorkerBuilder(HELLO_WORLD_TASK_QUEUE)
                .withTemporalServerHost(host)
                .withTemporalServerPort(Integer.parseInt(port.trim()))
                .withNamespace(namespace)
                .withWorkflowImplementationClasses(HelloWorldWorkflowImpl.class)
                .withActivityImplInstances(formatActivity)
                .withWaitForExist(Boolean.parseBoolean(waitForExist))
                .build();

        worker.start();
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOG.info("Worker graceful shutdown ...");
    }

}
