package org.kiroff.bank.account.command.api.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kiroff.bank.cqrs.core.commands.BaseCommand;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class CloseAccountCommand extends BaseCommand {

}

