package org.kiroff.bank.account.command.api.commands;

public interface CommandHandler {
    void handle(OpenAccountCommand command);

    void handle(CloseAccountCommand command);

    void handle(DepositFundsCommand command);

    void handle(WithdrawFundsCommand command);

    void handle(RestoreReadDbCommand command);
}
