package org.kiroff.bank.account.command.infrastructure;

import org.kiroff.bank.cqrs.core.commands.BaseCommand;
import org.kiroff.bank.cqrs.core.commands.CommandHandlerMethod;
import org.kiroff.bank.cqrs.core.infrastructure.CommandDispatcher;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountCommandDispatcher implements CommandDispatcher {
    private final Map<Class<? extends BaseCommand>, List<CommandHandlerMethod>> routes = new HashMap<>();

    @Override
    public <T extends BaseCommand> void registerHandler(Class<T> type, CommandHandlerMethod<T> commandHandlerMethod) {
        routes.computeIfAbsent(type, _ -> new LinkedList<>());
        routes.get(type).add(commandHandlerMethod);
    }

    @Override
    public void send(BaseCommand command) {
        var handlers = Optional.ofNullable(
                        routes.get(command.getClass()))
                .orElseThrow(() -> new RuntimeException("No command handler method found"));
        if (handlers.isEmpty()) {
            throw new RuntimeException("No command handler method found");
        } else if (handlers.size() == 1) {
            handlers.getFirst().handle(command);
        } else {
            throw new IllegalStateException("More than one command handler for type " + command.getClass());
        }

    }

}
