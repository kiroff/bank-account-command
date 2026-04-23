package org.kiroff.bank.account.command.api.commands;

import org.kiroff.bank.account.command.domain.AccountAggregate;
import org.kiroff.bank.cqrs.core.handlers.EventSourcingHandler;
import org.springframework.stereotype.Service;

/**
 * The AccountCommandHandler class handles commands related to account operations.
 * It acts as a mediator between the commands and the event sourcing handler for account aggregates.
 * The commands involve opening accounts, closing accounts, depositing funds, and withdrawing funds.
 *
 * Responsibilities:
 * - Handles the OpenAccountCommand to create a new account aggregate and save it.
 * - Handles the CloseAccountCommand to retrieve an existing account aggregate,
 *   execute the close operation, and save it.
 * - Handles the DepositFundsCommand to retrieve an account aggregate,
 *   deposit funds, and save the changes.
 * - Handles the WithdrawFundsCommand to retrieve an account aggregate,
 *   withdraw funds if there is sufficient balance, and save the changes.
 *
 * This class uses event sourcing principles to manage the account aggregates
 * and persist their state through the EventSourcingHandler interface.
 */
@Service
public class AccountCommandHandler implements CommandHandler {
    private final EventSourcingHandler<AccountAggregate> eventSourcingHandler;

    public AccountCommandHandler(EventSourcingHandler<AccountAggregate> eventSourcingHandler) {
        this.eventSourcingHandler = eventSourcingHandler;
    }

    @Override
    public void handle(OpenAccountCommand command) {
        var aggregate = new AccountAggregate(command);
        eventSourcingHandler.save(aggregate);
    }

    @Override
    public void handle(CloseAccountCommand command) {
        var aggregate = eventSourcingHandler.findById(command.getId());
        aggregate.closeAccount();
        eventSourcingHandler.save(aggregate);
    }

    @Override
    public void handle(DepositFundsCommand command) {
        var aggregate = eventSourcingHandler.findById(command.getId());
        aggregate.depositFunds(command.getAmount());
        eventSourcingHandler.save(aggregate);
    }

    @Override
    public void handle(WithdrawFundsCommand command) {
        var aggregate = eventSourcingHandler.findById(command.getId());
        if (aggregate.getBalance() < command.getAmount()) {
            throw new IllegalStateException("Cannot withdraw funds that are not enough");
        }
        aggregate.withdrawFunds(command.getAmount());
        eventSourcingHandler.save(aggregate);
    }

    @Override
    public void handle(RestoreReadDbCommand command) {
        eventSourcingHandler.republishEvents();
    }
}
