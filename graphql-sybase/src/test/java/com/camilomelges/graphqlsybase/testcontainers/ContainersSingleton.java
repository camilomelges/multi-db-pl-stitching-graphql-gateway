package com.camilomelges.graphqlsybase.testcontainers;

import com.github.dockerjava.api.model.PruneType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

@Testcontainers
@Execution(ExecutionMode.SAME_THREAD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@SuppressWarnings({"rawtypes", "unchecked"})
public class ContainersSingleton {
    private static final String SYBASE_IMAGE_NAME = "bjornviggo/sybase-testcontainer";
    private static final String SYBASE_IMAGE_VERSION = "1.0";
    private static final String SYBASE_DATASOURCE_PORT = "5000";
    private static final String SYBASE_DATASOURCE_DATABASE = "DB_TEST1";
    private static final String SYBASE_USER = "guest";
    private static final String SYBASE_PASSWORD = "guest1234";
    protected static GenericContainer SYBASE_CONTAINER;
    protected static Network SYBASE_NETWORK;

    @Value("${server.port}")
    protected String serverPort;

    @BeforeAll
    public static void sybaseContainer() {
        if (SYBASE_CONTAINER == null || SYBASE_NETWORK == null) {
            final var dockerClientFactory = DockerClientFactory.lazyClient();

            try {
                dockerClientFactory.pruneCmd(PruneType.CONTAINERS).exec();
                dockerClientFactory.listContainersCmd().exec().forEach(container -> dockerClientFactory.stopContainerCmd(container.getId()).exec());

                dockerClientFactory.pruneCmd(PruneType.NETWORKS).exec();
                dockerClientFactory.listNetworksCmd().exec().forEach(network -> dockerClientFactory.removeNetworkCmd(network.getId()).exec());

            } catch (final Exception ignored) {
            }
        }

        startContainers();
    }

    @DynamicPropertySource
    static void registerDynamicProperties(final DynamicPropertyRegistry registry) {
        registry.add("gql-ase.datasource.host", SYBASE_CONTAINER::getHost);
        registry.add("gql-ase.datasource.port", SYBASE_CONTAINER::getFirstMappedPort);
        registry.add("gql-ase.datasource.dbName", () -> SYBASE_CONTAINER.getEnvMap().get("SYBASE_DB"));
        registry.add("gql-ase.datasource.username", () -> SYBASE_CONTAINER.getEnvMap().get("SYBASE_USER"));
        registry.add("gql-ase.datasource.password", () -> SYBASE_CONTAINER.getEnvMap().get("SYBASE_PASSWORD"));
        registry.add("gql-ase.datasource.url", () -> "jdbc:jtds:sybase://" + SYBASE_CONTAINER.getHost() + ":" + SYBASE_CONTAINER.getFirstMappedPort() + "/" + SYBASE_CONTAINER.getEnvMap().get("SYBASE_DB"));
    }

    private static void startContainers() {

        if (SYBASE_NETWORK == null) {
            SYBASE_NETWORK = Network.newNetwork();
        }

        if (SYBASE_CONTAINER == null) {
            SYBASE_CONTAINER = new GenericContainer<>(SYBASE_IMAGE_NAME + ":" + SYBASE_IMAGE_VERSION)
                .withLabel("group", "gql-ase")
                .withNetwork(SYBASE_NETWORK)
                .withEnv("SYBASE_USER", SYBASE_USER)
                .withEnv("SYBASE_PASSWORD", SYBASE_PASSWORD)
                .withEnv("SYBASE_DB", SYBASE_DATASOURCE_DATABASE)
                .withExposedPorts(Integer.valueOf(SYBASE_DATASOURCE_PORT), Integer.valueOf(SYBASE_DATASOURCE_PORT))
                .withAccessToHost(true)
                .withCreateContainerCmdModifier(cmd -> cmd.withHostName("dksybase"));

            SYBASE_CONTAINER.setPortBindings(ImmutableList.of("0.0.0.0:" + SYBASE_DATASOURCE_PORT + ":" + SYBASE_DATASOURCE_PORT));

            SYBASE_CONTAINER.start();
        }
    }
}