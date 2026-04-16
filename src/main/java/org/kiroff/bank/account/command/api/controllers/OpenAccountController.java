package org.kiroff.bank.account.command.api.controllers;

import org.kiroff.bank.account.command.api.commands.OpenAccountCommand;
import org.kiroff.bank.account.command.api.dto.OpenAccountResponse;
import org.kiroff.bank.account.common.dto.BaseResponse;
import org.kiroff.bank.cqrs.core.infrastructure.CommandDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/accounts")
public class OpenAccountController {
    private final Logger logger = LoggerFactory.getLogger(OpenAccountController.class);

    private final CommandDispatcher commandDispatcher;

    public OpenAccountController(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }

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
}
