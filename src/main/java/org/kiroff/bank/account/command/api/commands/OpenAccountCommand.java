package org.kiroff.bank.account.command.api.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kiroff.bank.account.common.dto.AccountType;
import org.kiroff.bank.cqrs.core.commands.BaseCommand;

@EqualsAndHashCode(callSuper = true)
@Data
public class OpenAccountCommand extends BaseCommand {
    public AccountType accountType;
    public double openingBalance;
    private String accountHolder;
}

