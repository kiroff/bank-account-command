package org.kiroff.bank.account.command.domain;

import org.kiroff.bank.cqrs.core.events.EventModel;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStoreRepository extends MongoRepository<EventModel, String> {
    List<EventModel> findByAggregateIdentifier(String aggregateIdentifier);

    List<EventModel> findByAggregateIdentifierIn(List<String> aggregateIdentifiers);

    @Aggregation(pipeline = {
            "{ $sort: { aggregateIdentifier: 1, version: -1 } }",
            "{ $group: { _id: \"$aggregateIdentifier\", latestEvent: { $first: \"$$ROOT\" } } }",
            "{ $match: { \"latestEvent.eventType\": { $ne: \"AccountClosedEvent\" } } }",
            "{ $replaceRoot: { newRoot: \"$latestEvent\" } }"
    })
    List<EventModel> findActiveAggregates();

}
