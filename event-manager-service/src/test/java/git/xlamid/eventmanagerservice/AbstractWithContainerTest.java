package git.xlamid.eventmanagerservice;

import git.xlamid.eventmanagerservice.util.UserTestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class AbstractWithContainerTest {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected UserTestUtil userTestUtil;
    @Autowired
    protected MockMvc mockMvc;

    public static final PostgreSQLContainer POSTGRES_CONTAINER =
            new PostgreSQLContainer("postgres:16")
                    .withDatabaseName("event-manager-service-test")
                    .withUsername("postgres")
                    .withPassword("postgres")
                    .withReuse(true);

    public static final KafkaContainer KAFKA_CONTAINER =
            new KafkaContainer(DockerImageName.parse("apache/kafka:3.8.1"))
                    .withReuse(true);

    static {
        POSTGRES_CONTAINER.start();
        KAFKA_CONTAINER.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);

        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }

    protected String getAuthHeader(String login) {
        return "Bearer " + userTestUtil.getJwtTokenByLogin(login);
    }
}