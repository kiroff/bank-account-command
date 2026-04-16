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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AccountEventStore implements EventStore {
    final EventStoreRepository eventStoreRepository;

    final EventProducer eventProducer;

    public AccountEventStore(EventStoreRepository eventStoreRepository, EventProducer eventProducer) {
        this.eventStoreRepository = eventStoreRepository;
        this.eventProducer = eventProducer;
    }

    @Override
    public void saveEvents(String aggregateId, Iterable<BaseEvent> events, int expectedVersion) {
        final var eventStream = eventStoreRepository.findByAggregateIdentifier(aggregateId);
        if (expectedVersion != -1 && eventStream.getLast().getVersion() != expectedVersion) {
            throw new ConcurrencyException();
        }
        var version = new AtomicInteger(expectedVersion + 1);
        StreamSupport.stream(events.spliterator(), false)
                .map(event -> EventModel.builder()
                        .aggregateIdentifier(aggregateId)
                        .aggregateType(AccountAggregate.class.getTypeName())
                        .eventType(event.getClass().getSimpleName())
                        .eventData(event)
                        .version(version.getAndIncrement())
                        .build())
                .map(eventStoreRepository::save)
                .forEach(savedEvent -> eventProducer.produce(savedEvent.getEventType(), savedEvent.getEventData()));
    }

    @Override
    public List<BaseEvent> getEvents(String aggregateId) {
        return Optional.ofNullable(eventStoreRepository.findByAggregateIdentifier(aggregateId))
                .filter(l -> !l.isEmpty())
                .map(l -> l.stream().map(EventModel::getEventData).collect(Collectors.toList()))
                .orElseThrow(() -> new AggregateNotFoundException("Incorrect id=" + aggregateId));
    }
}
