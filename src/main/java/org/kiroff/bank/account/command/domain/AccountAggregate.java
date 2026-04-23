package org.kiroff.bank.account.command.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiroff.bank.account.command.api.commands.OpenAccountCommand;
import org.kiroff.bank.account.common.events.AccountClosedEvent;
import org.kiroff.bank.account.common.events.AccountOpenedEvent;
import org.kiroff.bank.account.common.events.FundsDepositedEvent;
import org.kiroff.bank.account.common.events.FundsWithdrawnEvent;
import org.kiroff.bank.cqrs.core.domain.AggregateRoot;

import java.time.LocalDate;

@NoArgsConstructor
public class AccountAggregate extends AggregateRoot {
    private Boolean active;
    @Getter
    private double balance;

    public AccountAggregate(OpenAccountCommand command) {
        raiseEvent(AccountOpenedEvent.builder()
                .id(command.getId())
                .accountHolder(command.getAccountHolder())
                .accountType(command.getAccountType())
                .openingBalance(command.getOpeningBalance())
                .createdDate(LocalDate.now())
                .build());
    }

    public void apply(AccountOpenedEvent event) {
        this.id = event.getId();
        this.active = true;
        this.balance = event.getOpeningBalance();
    }

    public void depositFunds(double amount) {
        if (!this.active) {
            throw new IllegalStateException("Cannot deposit funds to a closed account");
        }
        if (amount <= 0) {
            throw new IllegalStateException("Cannot deposit funds with a non-positive amount");
        }
        raiseEvent(FundsDepositedEvent.builder()
                .id(this.id)
                .amount(amount)
                .build());
    }

    public void apply(FundsDepositedEvent event) {
        this.id = event.getId();
        this.active = true;
        this.balance += event.getAmount();
    }

    public void withdrawFunds(double amount) {
        if (!this.active) {
            throw new IllegalStateException("Cannot withdraw funds from a closed account");
        }
        if (amount < 0) {
            throw new IllegalStateException("Cannot withdraw funds with a negative amount");
        }
        raiseEvent(FundsWithdrawnEvent.builder()
                .id(this.id)
                .amount(amount)
                .build());
    }

    public void apply(FundsWithdrawnEvent event) {
        this.id = event.getId();
        this.active = true;
        this.balance -= event.getAmount();
    }

    public void closeAccount() {
        if (!this.active) {
            throw new IllegalStateException("Account closed");
        }

        raiseEvent(AccountClosedEvent.builder()
                .id(this.id)
                .build());
    }

    public void apply(AccountClosedEvent event) {
        this.id = event.getId();
        this.active = false;
    }
}
