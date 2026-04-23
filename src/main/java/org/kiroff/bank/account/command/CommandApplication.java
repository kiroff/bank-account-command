package org.kiroff.bank.account.command;

import jakarta.annotation.PostConstruct;
import org.kiroff.bank.account.command.api.commands.CloseAccountCommand;
import org.kiroff.bank.account.command.api.commands.CommandHandler;
import org.kiroff.bank.account.command.api.commands.DepositFundsCommand;
import org.kiroff.bank.account.command.api.commands.OpenAccountCommand;
import org.kiroff.bank.account.command.api.commands.RestoreReadDbCommand;
import org.kiroff.bank.account.command.api.commands.WithdrawFundsCommand;
import org.kiroff.bank.cqrs.core.infrastructure.CommandDispatcher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommandApplication {

    private final CommandDispatcher commandDispatcher;

    private final CommandHandler commandHandler;

    static void main(String[] args) {
        SpringApplication.run(CommandApplication.class, args);
    }

    public CommandApplication(CommandDispatcher commandDispatcher, CommandHandler commandHandler) {
        this.commandDispatcher = commandDispatcher;
        this.commandHandler = commandHandler;
    }

    @PostConstruct
    public void registerHandlers() {
        commandDispatcher.registerHandler(OpenAccountCommand.class, commandHandler::handle);
        commandDispatcher.registerHandler(CloseAccountCommand.class, commandHandler::handle);
        commandDispatcher.registerHandler(DepositFundsCommand.class, commandHandler::handle);
        commandDispatcher.registerHandler(WithdrawFundsCommand.class, commandHandler::handle);
        commandDispatcher.registerHandler(RestoreReadDbCommand.class, commandHandler::handle);
    }
}
