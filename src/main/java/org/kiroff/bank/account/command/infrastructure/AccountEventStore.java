package org.kiroff.bank.account.command.infrastructure;

import org.kiroff.bank.account.command.domain.AccountAggregate;
import org.kiroff.bank.account.command.domain.EventStoreRepository;
import org.kiroff.bank.cqrs.core.events.BaseEvent;
import org.kiroff.bank.cqrs.core.events.EventModel;
import org.kiroff.bank.cqrs.core.excpetions.AggregateNotFoundException;
import org.kiroff.bank.cqrs.core.excpetions.ConcurrencyException;
import org.kiroff.bank.cqrs.core.infrastructure.EventStore;
import org.kiroff.bank.cqrs.core.producers.EventProducer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The AccountEventStore class is a service implementation of the EventStore interface.
 * It is responsible for storing and retrieving events related to the account aggregate.
 * This class integrates with an EventStoreRepository for database operations and an EventProducer
 * to publish the events to an external messaging system.
 */
@Service
public class AccountEventStore implements EventStore {
    final EventStoreRepository eventStoreRepository;

    final EventProducer eventProducer;

    public AccountEventStore(EventStoreRepository eventStoreRepository, EventProducer eventProducer) {
        this.eventStoreRepository = eventStoreRepository;
        this.eventProducer = eventProducer;
    }

    /**
     * Saves a sequence of events for the specified aggregate identifier to the event store.
     * If the expected version does not match the latest version in the event stream, a
     * {@code ConcurrencyException} is thrown to indicate concurrent modifications.
     *
     * @param aggregateId The unique identifier of the aggregate for which the events are being saved.
     * @param events An iterable list of {@code BaseEvent} objects representing the events to save.
     * @param expectedVersion The expected version of the aggregate. Used to detect concurrency issues.
     *                        Pass {@code -1} to skip version validation.
     * @throws ConcurrencyException if the provided expected version does not match the version
     *                               of the most recent event in the event stream.
     */
    @Override
    public void saveEvents(String aggregateId, Iterable<BaseEvent> events, int expectedVersion) {
        final var eventStream = eventStoreRepository.findByAggregateIdentifier(aggregateId);
        if (expectedVersion != -1 && eventStream.getLast().getVersion() != expectedVersion) {
            throw new ConcurrencyException();
        }
        var version = new AtomicInteger(expectedVersion);
        StreamSupport.stream(events.spliterator(), false)
                .peek(e -> e.setVersion(version.incrementAndGet()))
                .map(event -> EventModel.builder()
                        .aggregateIdentifier(aggregateId)
                        .aggregateType(AccountAggregate.class.getTypeName())
                        .eventType(event.getClass().getSimpleName())
                        .eventData(event)
                        .version(version.get())
                        .timeStamp(LocalDateTime.now())
                        .build())
                .map(eventStoreRepository::save)
                .forEach(savedEvent -> eventProducer.produce(savedEvent.getEventType(), savedEvent.getEventData()));
    }

    /**
     * Retrieves a list of events associated with the specified aggregate identifier.
     * If no events are found for the provided identifier, an {@code AggregateNotFoundException} is thrown.
     *
     * @param aggregateId The unique identifier of the aggregate whose events are to be retrieved.
     * @return A list of {@code BaseEvent} objects representing the events associated with the given aggregate identifier.
     * @throws AggregateNotFoundException if no events are found for the specified aggregate identifier.
     */
    @Override
    public List<BaseEvent> getEvents(String aggregateId) {
        return Optional.ofNullable(eventStoreRepository.findByAggregateIdentifier(aggregateId))
                .filter(l -> !l.isEmpty())
                .map(l -> l.stream().map(EventModel::getEventData).collect(Collectors.toList()))
                .orElseThrow(() -> new AggregateNotFoundException("Incorrect id=" + aggregateId));
    }
}
