package git.xlamid.eventnotificatorservice;

import git.xlamid.eventnotificatorservice.util.UserTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class EventNotificatorAbstractWithContainerTest {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected UserTestUtil userTestUtil;
    @Autowired
    protected MockMvc mockMvc;

    public static final PostgreSQLContainer POSTGRES_CONTAINER =
            new PostgreSQLContainer("postgres:16")
                    .withDatabaseName("event-notificator-service-test")
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withReuse(true);

    public static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"))
                    .withReuse(true);

    @SuppressWarnings("resource")
    public static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:7"))
                    .withExposedPorts(6379)
                    .withReuse(true);

    static {
        Startables.deepStart(
                POSTGRES_CONTAINER,
                KAFKA_CONTAINER,
                REDIS_CONTAINER
        ).join();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);

        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    }

    protected String getAuthHeader(String login) {
        return "Bearer " + userTestUtil.getJwtTokenByLogin(login);
    }
}