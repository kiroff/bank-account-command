package org.kiroff.bank.account.command.api.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kiroff.bank.cqrs.core.commands.BaseCommand;

@EqualsAndHashCode(callSuper = true)
@Data
public class WithdrawFundsCommand extends BaseCommand {
    private double amount;
}
