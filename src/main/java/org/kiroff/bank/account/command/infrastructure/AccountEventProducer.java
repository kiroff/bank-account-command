package org.kiroff.bank.account.command.infrastructure;

import org.kiroff.bank.cqrs.core.events.BaseEvent;
import org.kiroff.bank.cqrs.core.producers.EventProducer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * The AccountEventProducer class is responsible for producing domain events
 * in a banking application and sending them to a specified Kafka topic.
 * This class implements the EventProducer interface and utilizes a KafkaTemplate to
 * interact with a Kafka cluster for event publishing.
 *
 * The primary function of this class is to ensure that events, which represent
 * significant state changes in the application's domain, are correctly produced and
 * delivered to the appropriate Kafka topic for downstream processing or persistence.
 *
 * Dependencies:
 * - KafkaTemplate: Manages Kafka producer operations.
 * - BaseEvent: Represents the event structure, encapsulating metadata and payload.
 *
 * Functionality:
 * - Produces events to a specified Kafka topic with the provided payload.
 */
@Service
public class AccountEventProducer implements EventProducer {

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    public AccountEventProducer(KafkaTemplate<String, BaseEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a domain event to the specified Kafka topic using the KafkaTemplate.
     * This method is part of the EventProducer interface implementation, utilized to
     * communicate state changes across the system by sending events to Kafka.
     *
     * @param topic the name of the Kafka topic to which the event will be published
     * @param event the domain event to be sent, encapsulated as an instance of BaseEvent
     */
    @Override
    public void produce(String topic, BaseEvent event) {
        kafkaTemplate.send(topic, event);
    }
}