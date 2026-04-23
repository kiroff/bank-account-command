package org.kiroff.bank.account.command.api.controllers;

import org.kiroff.bank.account.command.api.commands.CloseAccountCommand;
import org.kiroff.bank.account.command.api.commands.OpenAccountCommand;
import org.kiroff.bank.account.command.api.dto.OpenAccountResponse;
import org.kiroff.bank.account.common.dto.BaseResponse;
import org.kiroff.bank.cqrs.core.infrastructure.CommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller class for managing bank accounts.
 * Handles API endpoints for account-related operations.
 */
@RestController
@RequestMapping(path = "/v1/accounts")
public class AccountsController {
    private final Logger logger = LoggerFactory.getLogger(AccountsController.class);

    private final CommandDispatcher commandDispatcher;

    public AccountsController(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

    /**
     * Creates a new bank account based on the provided command data.
     * Generates a unique account ID, processes the account creation command,
     * and responds with the result of the operation.
     *
     * @param command the command containing account details such as account type,
     *                opening balance, and account holder's information
     * @return a ResponseEntity containing a BaseResponse object; includes either
     *         a success message with the generated account ID or an error message
     *         in case of failure. Returns with HTTP status:
     *         - 201 (Created) if account creation is successful
     *         - 400 (Bad Request) if there is a validation error or a known
     *           IllegalStateException
     *         - 500 (Internal Server Error) in case of any unexpected errors
     */
    @PostMapping
    public ResponseEntity<BaseResponse> openAccount(@RequestBody OpenAccountCommand command) {
        var id = UUID.randomUUID().toString();
        command.setId(id);
        try {
            commandDispatcher.send(command);
            return new ResponseEntity<>(new OpenAccountResponse("Account opened successfully", id), HttpStatus.CREATED);
        } catch (IllegalStateException ise) {
            logger.error("Failed to open account {}", id, ise);
            return ResponseEntity.badRequest().body(new BaseResponse(ise.toString()));
        } catch (Exception e) {
            String msg = String.format("Failed to open account %s", id);
            logger.error(msg, e);
            return new ResponseEntity<>(new OpenAccountResponse(e.toString()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Closes an existing bank account based on the provided account ID.
     * Processes the account closure command and responds with the result of the operation.
     *
     * @param id the unique identifier of the account to be closed
     * @return a ResponseEntity containing a BaseResponse object; includes either
     *         a success message confirming the account closure or an error message
     *         in case of failure. Returns with HTTP status:
     *         - 410 (Gone) if the account closure is successful
     *         - 400 (Bad Request) if there is a validation error or a known
     *           IllegalStateException
     *         - 500 (Internal Server Error) in case of any unexpected errors
     */
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<BaseResponse> closeAccount(@PathVariable("id") String id) {
        CloseAccountCommand command = new CloseAccountCommand();
        command.setId(id);
        try {
            commandDispatcher.send(command);
            return new ResponseEntity<>(new BaseResponse("Account closed successfully"), HttpStatus.GONE);
        } catch (IllegalStateException ise) {
            logger.error("Failed to close account {}", id, ise);
            return ResponseEntity.badRequest().body(new BaseResponse(ise.toString()));
        } catch (Exception e) {
            String msg = String.format("Failed to close account %s", id);
            logger.error(msg, e);
            return new ResponseEntity<>(new OpenAccountResponse(e.toString()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
