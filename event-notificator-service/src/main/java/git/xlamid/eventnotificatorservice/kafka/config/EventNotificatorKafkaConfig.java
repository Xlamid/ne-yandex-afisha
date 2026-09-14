package git.xlamid.eventnotificatorservice.kafka.config;

import git.xlamid.eventcommon.kafka.model.NotificationKafkaEvent;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.Map;

@EnableKafka
@Configuration
public class EventNotificatorKafkaConfig {

    @Bean
    public ConsumerFactory<Long, NotificationKafkaEvent> consumerFactory(KafkaProperties kafkaProps) {
        Map<String, Object> props = kafkaProps.buildConsumerProperties();
        DefaultKafkaConsumerFactory<Long, NotificationKafkaEvent> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props);
        consumerFactory.setValueDeserializer(
                new JacksonJsonDeserializer<>(NotificationKafkaEvent.class, false)
        );
        return consumerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Long, NotificationKafkaEvent> kafkaListenerContainerFactory(
            ConsumerFactory<Long, NotificationKafkaEvent> consumerFactory
    ) {
        var containerFactory =
                new ConcurrentKafkaListenerContainerFactory<Long, NotificationKafkaEvent>();
        containerFactory.setConsumerFactory(consumerFactory);
        containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return containerFactory;
    }
}