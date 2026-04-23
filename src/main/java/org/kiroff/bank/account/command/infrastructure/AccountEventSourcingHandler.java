package org.kiroff.bank.account.command.infrastructure;

import org.kiroff.bank.account.command.domain.AccountAggregate;
import org.kiroff.bank.cqrs.core.domain.AggregateRoot;
import org.kiroff.bank.cqrs.core.events.BaseEvent;
import org.kiroff.bank.cqrs.core.handlers.EventSourcingHandler;
import org.kiroff.bank.cqrs.core.infrastructure.EventStore;
import org.kiroff.bank.cqrs.core.producers.EventProducer;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles the event sourcing operations for the {@code AccountAggregate}.
 * This class is responsible for persisting and reconstructing aggregates
 * from their event streams in an event sourcing system.
 *
 * It interacts with an {@code EventStore} to save and retrieve events, ensuring
 * that aggregates are properly persisted and can be reconstructed from their
 * historical events.
 *
 * This handler is utilized to manage the lifecycle of the {@code AccountAggregate},
 * including its creation, updates, and replaying of events to restore its state.
 *
 * Responsibilities:
 * - Save new events for an aggregate to the associated event store.
 * - Reconstruct an aggregate by replaying its historical events from the event store.
 *
 * Implements:
 * {@link EventSourcingHandler}
 *
 * Dependencies:
 * - {@link EventStore}
 *
 */
@Service
public class AccountEventSourcingHandler implements EventSourcingHandler<AccountAggregate> {

    private static final Logger LOGGER = Logger.getLogger(AccountEventSourcingHandler.class.getName());

    private final EventStore eventStore;

    private final EventProducer eventProducer;

    public AccountEventSourcingHandler(EventStore eventStore, EventProducer eventProducer) {
        this.eventStore = eventStore;
        this.eventProducer = eventProducer;
    }

    /**
     * Persists the events of the specified aggregate to the event store and marks
     * the aggregate's changes as committed.
     *
     * @param aggregate the aggregate whose events are to be saved. The aggregate must
     *                  provide its unique identifier, list of uncommitted changes,
     *                  and current version to ensure proper storage and integrity.
     */
    @Override
    public void save(AggregateRoot aggregate) {
        eventStore.saveEvents(aggregate.getId(), aggregate.getUncommittedChanges(), aggregate.getVersion());
        aggregate.markChangesAsCommitted();
    }

    /**
     * Retrieves an {@code AccountAggregate} instance by its unique identifier. The method fetches
     * the event stream associated with the provided ID from the event store, replays the events
     * to restore the aggregate's state, and sets the latest version of the aggregate.
     *
     * @param id the unique identifier of the aggregate to be retrieved
     * @return the reconstructed {@code AccountAggregate} instance with the state
     *         restored by replaying its events
     */
    @Override
    public AccountAggregate findById(String id) {
        var aggregate = new AccountAggregate();
        var events = eventStore.getEvents(id);
        if (events != null && !events.isEmpty()) {
            aggregate.replayEvents(events);
            events.stream()
                    .map(BaseEvent::getVersion)
                    .max(Comparator.naturalOrder())
                    .ifPresentOrElse(aggregate::setVersion, () -> LOGGER.warning("No version found."));
        }
        return aggregate;
    }

    @Override
    public void republishEvents() {
        var events = eventStore.getEventsForAllActiveAggregates();
        if (events != null && !events.isEmpty()) {
            events.forEach(ev -> eventProducer.produce(ev.getClass().getSimpleName(), ev));
        }
    }
}
