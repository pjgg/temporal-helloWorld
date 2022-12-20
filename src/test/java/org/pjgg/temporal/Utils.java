package org.pjgg.temporal;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.OS;

import com.google.protobuf.util.Durations;

import io.temporal.api.workflowservice.v1.DescribeNamespaceRequest;
import io.temporal.api.workflowservice.v1.DescribeNamespaceResponse;
import io.temporal.api.workflowservice.v1.RegisterNamespaceRequest;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

public class Utils {
    private static final Path SOURCE_CLASSES_LOCATION = Paths.get("target", "classes");
    private static final String CLASS_SUFFIX = ".class";
    private static final int NAMESPACE_SIZE = 10;
    private static final Logger LOG = Logger.getLogger(Utils.class);

    public static String generateRandomNamespace(String host, int port) {
        String ns = ThreadLocalRandom.current().ints(NAMESPACE_SIZE, 'a', 'z' + 1)
                .collect(() -> new StringBuilder("ts-"), StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generateRandomNamespace(host, port, ns);
    }

    public static String generateRandomNamespace(String host, String port) {
        return generateRandomNamespace(host, Integer.parseInt(port));
    }

    public static String generateRandomNamespace(String host, int port, String namespace) {
        createNamespace(namespace, host, port);
        return namespace;
    }

    public static String generateRandomNamespace(String host, String port, String namespace) {
        return generateRandomNamespace(host, Integer.parseInt(port), namespace);
    }

    public static Class<?>[] findAllClassesFromSource() {
        List<Class<?>> classes = new LinkedList<>();
        try {
            if (!Files.exists(SOURCE_CLASSES_LOCATION)) {
                return new Class<?>[0];
            }
            try (Stream<Path> stream = Files.walk(SOURCE_CLASSES_LOCATION)) {
                stream.map(Path::toString)
                        .filter(s -> s.endsWith(CLASS_SUFFIX))
                        .map(Utils::normalizeClassName)
                        .forEach(className -> {
                            try {
                                classes.add(Thread.currentThread().getContextClassLoader().loadClass(className));
                            } catch (ClassNotFoundException ex) {
                                LOG.warn("Could not load %s. Caused by: %s", className, ex);
                            }
                        });
            }
        } catch (Exception ex) {
            fail("Can't load source classes location. Caused by " + ex.getMessage());
        }

        return classes.toArray(new Class<?>[classes.size()]);
    }

    private static String normalizeClassName(String path) {
        String source = SOURCE_CLASSES_LOCATION.relativize(Paths.get(path)).toString()
                .replace(CLASS_SUFFIX, StringUtils.EMPTY);
        if (OS.WINDOWS.isCurrentOs()) {
            source = source.replace("\\", ".");
        } else {
            source = source.replace("/", ".");
        }

        return source;
    }

    private static void createNamespace(String namespace, String host, int port) {
        String endpoint = String.format("%s:%s", host, port);
        WorkflowServiceStubs service = WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder().setTarget(endpoint).build());

        RegisterNamespaceRequest request =
                RegisterNamespaceRequest.newBuilder()
                        .setNamespace(namespace)
                        .setWorkflowExecutionRetentionPeriod(Durations.fromDays(1))
                        .build();

        service.blockingStub().registerNamespace(request);

        await().pollInterval(1, TimeUnit.SECONDS).atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
                    DescribeNamespaceResponse resp =
                            service.blockingStub()
                                    .describeNamespace(DescribeNamespaceRequest.newBuilder().setNamespace(namespace).build());

            Assertions.assertTrue(namespace.equalsIgnoreCase(resp.getNamespaceInfo().getName()));
        });
        //TODO https://github.com/temporalio/temporal/issues/1941
        wait(Duration.ofSeconds(30));
        LOG.infof("Namespace %s created", namespace);
    }

    public static void wait(Duration amount) {
        try {
            Thread.sleep(amount.toMillis());
        } catch(Exception e) {}
    }
}
