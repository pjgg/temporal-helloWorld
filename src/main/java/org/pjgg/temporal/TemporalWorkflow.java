package org.pjgg.temporal;

import java.util.Objects;

import org.jboss.logging.Logger;

import com.google.common.base.Throwables;

import io.quarkus.runtime.util.StringUtil;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class TemporalWorkflow extends TemporalCommons {
    private static final Logger LOG = Logger.getLogger(TemporalWorkflow.class);
    private String taskQueueName;
    private WorkflowClientOptions clientOpts;
    private WorkflowOptions workflowOpts;

    public <T> T getStub(Class<T> stub) {
        try {
            WorkflowServiceStubs service = createDefaultWorkflowServiceStubs();
            WorkflowClient client = createWorkflowClient(service);
            return client.newWorkflowStub(stub, workflowOpts);
        } catch (Exception e) {
            LOG.errorf("Stack Trace: %s" + Throwables.getStackTraceAsString(e));
            Throwable cause = Throwables.getRootCause(e);
            LOG.errorf("Root cause:: %s" + cause.getMessage());
            throw new RuntimeException("Error on Workflow getStub. " + e.getMessage());
        }
    }

    private TemporalWorkflow(){}

    public static TemporalWorkflow.Builder newTemporalWorkflowBuilder(String taskQueueName) {
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
        private WorkflowClientOptions clientOpts;
        private WorkflowOptions workflowOpts;
        private final String taskQueueName;

        public TemporalWorkflow.Builder withNamespace(String namespace) {
            if(StringUtil.isNullOrEmpty(namespace)) {
                throw new IllegalArgumentException( "namespace can't be empty or null" );
            }
            this.namespace = namespace;
            return this;
        }

        public TemporalWorkflow.Builder withTemporalServerHost(String host) {
            this.temporalServerHost = host;
            return this;
        }

        public TemporalWorkflow.Builder withTemporalServerPort(int port) {
            if(port <= 0) {
                throw new IllegalArgumentException( "port must be an unsigned integer" );
            }

            this.temporalServerPort = port;
            return this;
        }

        public TemporalWorkflow.Builder withWorkflowClientOptions(WorkflowClientOptions opts) {
            this.clientOpts = opts;
            return this;
        }

        public TemporalWorkflow.Builder withWorkflowOptions(WorkflowOptions options) {
            this.workflowOpts = options;
            return this;
        }

        public TemporalWorkflow build() {
            TemporalWorkflow workflow = new TemporalWorkflow();
            workflow.taskQueueName = this.taskQueueName;
            if(!StringUtil.isNullOrEmpty(this.temporalServerHost)) {
                workflow.temporalServerHost = this.temporalServerHost;
            }

            if(this.temporalServerPort > 0) {
                workflow.temporalServerPort = this.temporalServerPort;
            }

            workflow.workflowOpts = this.workflowOpts;
            if(Objects.isNull(this.workflowOpts)) {
                workflow.workflowOpts = WorkflowOptions.newBuilder()
                        .setTaskQueue(workflow.taskQueueName)
                        .build();
            }

            workflow.clientOpts = this.clientOpts;
            workflow.namespace = this.namespace;
            if(!StringUtil.isNullOrEmpty(this.namespace) && Objects.nonNull(workflow.clientOpts)) {
                if(workflow.clientOpts.getNamespace().equalsIgnoreCase(workflow.namespace)) {
                    throw new IllegalArgumentException("WorkflowClientOptions namespace doesn't match with workflow namespace");
                }
            }

            return workflow;
        }
    }
}
