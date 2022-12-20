package org.pjgg.temporal;

import java.util.Objects;

import io.quarkus.runtime.util.StringUtil;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

public abstract class TemporalCommons {

    public static final String HELLO_WORLD_TASK_QUEUE = "HELLO_WORLD_TASK_QUEUE";
    protected WorkflowClientOptions clientOpts;
    protected String temporalServerHost = "127.0.0.1";
    protected Integer temporalServerPort = 7233;
    protected String namespace = "default";

    protected WorkflowClient createWorkflowClient(WorkflowServiceStubs service) {
        if(Objects.isNull(this.clientOpts)) {
            if(StringUtil.isNullOrEmpty(namespace)) {
                this.clientOpts = WorkflowClientOptions.newBuilder().build();
            } else this.clientOpts = WorkflowClientOptions.newBuilder().setNamespace(namespace).build();
        }
        return WorkflowClient.newInstance(service, this.clientOpts);
    }
    protected WorkflowServiceStubs createDefaultWorkflowServiceStubs() {
        String endpoint = String.format("%s:%s", temporalServerHost, temporalServerPort);
        return WorkflowServiceStubs.newServiceStubs(WorkflowServiceStubsOptions.newBuilder().setTarget(endpoint).build());
    }
}
