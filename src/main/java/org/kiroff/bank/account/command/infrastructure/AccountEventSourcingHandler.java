package org.kiroff.bank.account.command.infrastructure;

import org.kiroff.bank.account.command.domain.AccountAggregate;
import org.kiroff.bank.cqrs.core.domain.AggregateRoot;
import org.kiroff.bank.cqrs.core.events.BaseEvent;
import org.kiroff.bank.cqrs.core.handlers.EventSourcingHandler;
import org.kiroff.bank.cqrs.core.infrastructure.EventStore;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.logging.Logger;

@Service
public class AccountEventSourcingHandler implements EventSourcingHandler<AccountAggregate> {
    private static final Logger LOGGER = Logger.getLogger(AccountEventSourcingHandler.class.getName());
    private final EventStore eventStore;

    public AccountEventSourcingHandler(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public void save(AggregateRoot aggregate) {
        eventStore.saveEvents(aggregate.getId(), aggregate.getUncommittedChanges(), aggregate.getVersion());
        aggregate.markChangesAsCommitted();
    }

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
}
