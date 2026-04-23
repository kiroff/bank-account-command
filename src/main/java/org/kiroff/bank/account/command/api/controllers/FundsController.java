package org.kiroff.bank.account.command.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.kiroff.bank.account.command.api.commands.DepositFundsCommand;
import org.kiroff.bank.account.command.api.commands.WithdrawFundsCommand;
import org.kiroff.bank.account.common.dto.BaseResponse;
import org.kiroff.bank.cqrs.core.commands.BaseCommand;
import org.kiroff.bank.cqrs.core.excpetions.AggregateNotFoundException;
import org.kiroff.bank.cqrs.core.infrastructure.CommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FundsController handles HTTP requests related to fund operations,
 * such as depositing or withdrawing funds for a specific account.
 * It processes incoming commands via the CommandDispatcher to ensure
 * changes are applied to the account state.
 */
@Slf4j
@RestController
@RequestMapping(path = "/v1/funds")
public class FundsController {

    private final CommandDispatcher commandDispatcher;

    public FundsController(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    /**
     * Updates the balance of a specific account by either depositing or withdrawing funds,
     * depending on the provided amount.
     *
     * @param id the unique identifier of the account whose balance is to be updated
     * @param amount the amount to be deposited (if positive) or withdrawn (if negative or zero)
     *               (null values are not accepted)
     * @return a ResponseEntity containing a BaseResponse with the operation status message
     *         and an appropriate HTTP status code, depending on the operation outcome
     */
    @PutMapping(path = "/{id}/{amount}")
    public ResponseEntity<BaseResponse> updateBalance(@PathVariable(value = "id") String id, @PathVariable(value = "amount") Double amount) {
        if(amount == null) {
            return ResponseEntity.badRequest().body(new BaseResponse("Amount must be provided"));
        }
        else if(amount <= 0) {
            WithdrawFundsCommand command = new WithdrawFundsCommand();
            command.setAmount(Math.abs(amount));
            command.setId(id);
            return setBalance(command, "withdrawn");
        } else {
            DepositFundsCommand command = new DepositFundsCommand();
            command.setAmount(amount);
            command.setId(id);
            return setBalance(command, "deposited");
        }

    }

    /**
     * Executes the balance update operation by dispatching a command and handling success or failure scenarios.
     *
     * @param command the command representing the action to be performed, such as depositing or withdrawing funds
     * @param action a descriptive string indicating the type of operation ("deposited" or "withdrawn")
     * @return a ResponseEntity containing a BaseResponse with the operation status message and an appropriate HTTP status code:
     *         - HTTP 200 (OK) if the operation completes successfully
     *         - HTTP 400 (Bad Request) for known validation errors (e.g., illegal state or aggregate not found)
     *         - HTTP 500 (Internal Server Error) for unexpected runtime exceptions
     */
    private ResponseEntity<BaseResponse> setBalance(BaseCommand command, String action) {
        try {
            commandDispatcher.send(command);
            return new ResponseEntity<>(new BaseResponse("Funds " + action + " successfully"), HttpStatus.OK);
        } catch (IllegalStateException | AggregateNotFoundException ise) {
            log.error("Funds unsuccessfully {} [{}]", action, command.getId(), ise);
            return ResponseEntity.badRequest().body(new BaseResponse(ise.toString()));
        } catch (Exception e) {
            String msg = String.format("Funds unsuccessfully %s [%s]", action, command.getId());
            log.error(msg, e);
            return new ResponseEntity<>(new BaseResponse(e.toString()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
