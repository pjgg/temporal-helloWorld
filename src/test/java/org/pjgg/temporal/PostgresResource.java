package org.pjgg.temporal;

import java.util.Map;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class PostgresResource implements QuarkusTestResourceLifecycleManager {

    private static final String POSTGRESQL_IMAGE_NAME = "postgres";
    private static final String POSTGRESQL_VERSION = "latest";
    private static final String POSTGRESQL_IMAGE_VERSION = String.format("%s:%s", POSTGRESQL_IMAGE_NAME, POSTGRESQL_VERSION);
    protected static final int DEFAULT_POSTGRES_PORT = 5432;
    protected static final String POSTGRES_USER = "temporal";
    protected static final String POSTGRES_PWD = "temporal";
    private String host;
    private int port;
    private GenericContainer<?> postgresContainer;

    public PostgresResource(){
        postgresContainer = new GenericContainer<>(DockerImageName.parse(POSTGRESQL_IMAGE_VERSION))
                .withEnv("POSTGRES_USER", POSTGRES_USER)
                .withEnv("POSTGRES_PASSWORD", POSTGRES_PWD)
                .withExposedPorts(DEFAULT_POSTGRES_PORT);
    }

    public PostgresResource(Network network, String networkAlias){
        postgresContainer = new GenericContainer<>(DockerImageName.parse(POSTGRESQL_IMAGE_VERSION))
                .withEnv("POSTGRES_USER", POSTGRES_USER)
                .withEnv("POSTGRES_PASSWORD", POSTGRES_PWD)
                .withExposedPorts(DEFAULT_POSTGRES_PORT)
                .withNetwork(network)
                .withNetworkAliases(networkAlias);
    }

    @Override
    public Map<String, String> start() {
        postgresContainer
                .waitingFor(new LogMessageWaitStrategy().withRegEx(".*database system is ready to accept connections.*\\s"))
                .start();

        this.host = postgresContainer.getHost();
        this.port = postgresContainer.getMappedPort(DEFAULT_POSTGRES_PORT);

        return Map.of(
                "postgres.user", POSTGRES_USER,
                "postgres.pwd", POSTGRES_PWD,
                "postgres.host", postgresContainer.getHost(),
                "postgres.port", "" + postgresContainer.getMappedPort(DEFAULT_POSTGRES_PORT));
    }

    @Override
    public void stop() {
        postgresContainer.stop();
    }

    public GenericContainer<?> getPostgresContainer() {
        return postgresContainer;
    }
}
