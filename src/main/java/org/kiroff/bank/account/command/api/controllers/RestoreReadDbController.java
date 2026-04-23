package org.kiroff.bank.account.command.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.kiroff.bank.account.command.api.commands.RestoreReadDbCommand;
import org.kiroff.bank.account.command.api.dto.OpenAccountResponse;
import org.kiroff.bank.account.common.dto.BaseResponse;
import org.kiroff.bank.cqrs.core.infrastructure.CommandDispatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/_restore-read-db")
public class RestoreReadDbController {

    private final CommandDispatcher commandDispatcher;

    public RestoreReadDbController(CommandDispatcher commandDispatcher) {
        this.commandDispatcher = commandDispatcher;
    }
    @PostMapping
    public ResponseEntity<BaseResponse> restoreReadDb() {
        try {
            commandDispatcher.send(new RestoreReadDbCommand());
            return ResponseEntity.ok(new BaseResponse("Restored event db successfully"));
        } catch (IllegalStateException ise) {
            log.error("Failed to restore event db", ise);
            return ResponseEntity.badRequest().body(new BaseResponse(ise.toString()));
        } catch (Exception e) {
            log.error("Failed to restore event db", e);
            return ResponseEntity.internalServerError().body(new OpenAccountResponse(e.toString()));
        }
    }
}
