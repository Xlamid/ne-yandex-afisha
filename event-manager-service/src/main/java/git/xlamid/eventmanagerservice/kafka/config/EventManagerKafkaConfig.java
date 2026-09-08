package git.xlamid.eventmanagerservice.kafka.config;

import git.xlamid.eventcommon.kafka.model.NotificationKafkaEvent;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class EventManagerKafkaConfig {

    @Bean
    public KafkaTemplate<Long, NotificationKafkaEvent> kafkaTemplate(KafkaProperties kafkaProps) {
        Map<String, Object> props = kafkaProps.buildProducerProperties();
        ProducerFactory<Long, NotificationKafkaEvent> producerFactory =
                new DefaultKafkaProducerFactory<>(props);
        return new KafkaTemplate<>(producerFactory);
    }
}