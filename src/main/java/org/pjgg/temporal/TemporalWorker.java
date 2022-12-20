package org.pjgg.temporal;

import java.util.Objects;

import org.jboss.logging.Logger;

import com.google.common.base.Throwables;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.util.StringUtil;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.internal.worker.Startable;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class TemporalWorker extends TemporalCommons implements Startable {
    private static final Logger LOG = Logger.getLogger(TemporalWorker.class);
    private boolean waitForExist = true;
    private String taskQueueName;
    private Class<?>[] workflowImplementationClasses;
    private Object[] activityImplInstances;
    private boolean running;

    public void start() {
        try {
            WorkflowServiceStubs service = createDefaultWorkflowServiceStubs();
            WorkflowClient client = createWorkflowClient(service);
            WorkerFactory factory = WorkerFactory.newInstance(client);

            Worker worker = factory.newWorker(this.taskQueueName);
            worker.registerWorkflowImplementationTypes(this.workflowImplementationClasses);
            worker.registerActivitiesImplementations(this.activityImplInstances);
            LOG.infof("Listening queue -> %s", this.taskQueueName);

            factory.start();
            this.running = true;
            if(waitForExist) {
                Quarkus.waitForExit();
            }

        } catch (Exception e) {
            LOG.errorf("Stack Trace: %s" + Throwables.getStackTraceAsString(e));
            Throwable cause = Throwables.getRootCause(e);
            LOG.errorf("Root cause:: %s" + cause.getMessage());
            stop();
        }
    }

    @Override
    public boolean isStarted() {
        return running;
    }

    public void stop() {
        this.running = false;
        Quarkus.asyncExit(0);
    }

    private TemporalWorker(){}

    public static Builder newTemporalWorkerBuilder(String taskQueueName) {
        return new Builder(taskQueueName);
    }

    public static class Builder {
        public Builder(String taskQueueName) {
            if(StringUtil.isNullOrEmpty(taskQueueName)) {
                throw new IllegalArgumentException( "taskQueueName must be not empty" );
            }

            this.taskQueueName = taskQueueName;
        }
        private String namespace;
        private String temporalServerHost;
        private int temporalServerPort;
        private Boolean waitForExist;
        private WorkflowClientOptions clientOpts;
        private final String taskQueueName;
        private Class<?>[] workflowImplementationClasses;
        private Object[] activityImplInstances;

        public Builder withNamespace(String namespace) {
            if(StringUtil.isNullOrEmpty(namespace)) {
                throw new IllegalArgumentException( "namespace can't be empty or null" );
            }
            this.namespace = namespace;
            return this;
        }

        public Builder withTemporalServerHost(String host) {
            this.temporalServerHost = host;
            return this;
        }

        public Builder withTemporalServerPort(int port) {
            if(port <= 0) {
                throw new IllegalArgumentException( "port must be an unsigned integer" );
            }

            this.temporalServerPort = port;
            return this;
        }

        public Builder withWorkflowClientOptions(WorkflowClientOptions opts) {
            this.clientOpts = opts;
            return this;
        }

        public Builder withWorkflowImplementationClasses(Class<?>... implementationClasses) {
            this.workflowImplementationClasses = implementationClasses;
            return this;
        }

        public Builder withActivityImplInstances(Object... activityImplInstances) {
            this.activityImplInstances = activityImplInstances;
            return this;
        }

        public Builder withWaitForExist(boolean waitForExist) {
            this.waitForExist = waitForExist;
            return this;
        }

        public TemporalWorker build() {
            TemporalWorker worker = new TemporalWorker();
            worker.taskQueueName = this.taskQueueName;
            worker.activityImplInstances = this.activityImplInstances;
            worker.workflowImplementationClasses = this.workflowImplementationClasses;

            if(Objects.nonNull(waitForExist)) {
                worker.waitForExist = this.waitForExist;
            }

            if(!StringUtil.isNullOrEmpty(this.temporalServerHost)) {
                worker.temporalServerHost = this.temporalServerHost;
            }

            if(this.temporalServerPort > 0) {
                worker.temporalServerPort = this.temporalServerPort;
            }

            worker.clientOpts = this.clientOpts;
            worker.namespace = this.namespace;
            if(!StringUtil.isNullOrEmpty(this.namespace) && Objects.nonNull(worker.clientOpts)) {
                if(worker.clientOpts.getNamespace().equalsIgnoreCase(worker.namespace)) {
                    throw new IllegalArgumentException("WorkflowClientOptions namespace doesn't match with worker namespace");
                }
            }

            return worker;
        }
    }
}
