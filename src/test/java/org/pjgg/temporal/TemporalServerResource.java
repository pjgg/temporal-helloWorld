package org.pjgg.temporal;

import static org.pjgg.temporal.PostgresResource.DEFAULT_POSTGRES_PORT;
import static org.pjgg.temporal.PostgresResource.POSTGRES_PWD;
import static org.pjgg.temporal.PostgresResource.POSTGRES_USER;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class TemporalServerResource implements QuarkusTestResourceLifecycleManager {

    private static final String TEMPORAL_IMAGE_NAME = "temporalio/auto-setup";
    private static final String TEMPORAL_VERSION = "1.19.0";
    private static final String TEMPORAL_IMAGE_VERSION = String.format("%s:%s", TEMPORAL_IMAGE_NAME, TEMPORAL_VERSION);
    private static final String DEFAULT_POSTGRES_CONFIG = "config/dynamicconfig/development-sql.yaml";
    protected static final String POSTGRESQL_DB = "postgresql";
    private static int DEFAULT_TEMPORAL_PORT = 7233;
    private GenericContainer<?> temporalContainer;
    private boolean isRunning;
    private String host;
    private String port;
    private static final String NAMESPACE = "default";

    @Override
    public Map<String, String> start() {
        if(!isRunning) {
            Network network = Network.newNetwork();
            PostgresResource postgresContainer = new PostgresResource(network, POSTGRESQL_DB);

            temporalContainer = new GenericContainer<>(DockerImageName.parse(TEMPORAL_IMAGE_VERSION))
                    .dependsOn(postgresContainer.getPostgresContainer())
                    .withNetwork(network)
                    .withEnv("POSTGRES_USER", POSTGRES_USER)
                    .withEnv("POSTGRES_PWD", POSTGRES_PWD)
                    .withEnv("DB", POSTGRESQL_DB)
                    .withEnv("DB_PORT", "" + DEFAULT_POSTGRES_PORT)
                    .withEnv("POSTGRES_SEEDS", POSTGRESQL_DB)
                    .withEnv("DYNAMIC_CONFIG_FILE_PATH", DEFAULT_POSTGRES_CONFIG)
                    .withEnv("DEFAULT_NAMESPACE", NAMESPACE)
                    .withExposedPorts(DEFAULT_TEMPORAL_PORT)
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("development-cass.yaml"),
                            "/etc/temporal/config/dynamicconfig/development-cass.yaml")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("docker.yaml"),
                            "/etc/temporal/config/dynamicconfig/docker.yaml")
                    .withCopyFileToContainer(
                            MountableFile.forClasspathResource("development-sql.yaml"),
                            "/etc/temporal/config/dynamicconfig/development-sql.yaml"
                    );

            temporalContainer
                    .waitingFor(new LogMessageWaitStrategy().withRegEx(".*Temporal server started.*\\s"))
                    .start();

            isRunning = true;
            port = "" + temporalContainer.getMappedPort(DEFAULT_TEMPORAL_PORT);
            host = temporalContainer.getHost();
        }

        // TODO https://github.com/temporalio/temporal/issues/1057
        Utils.wait(Duration.ofSeconds(15));
        return Map.of(
                "temporal.server.host", host,
                "temporal.server.port", port,
                "temporal.server.namespace", NAMESPACE
        );
    }

    @Override
    public void stop() {
        if(Objects.nonNull(temporalContainer) && isRunning) {
            temporalContainer.stop();
            isRunning= false;
        }
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }
}
