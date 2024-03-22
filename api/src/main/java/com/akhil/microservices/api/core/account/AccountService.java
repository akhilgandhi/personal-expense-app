package com.akhil.microservices.api.core.account;

import org.springframework.web.bind.annotation.*;

public interface AccountService {

    @GetMapping(
            value = "/account/{accountId}",
            produces = "application/json"
    )
    Account getAccount(@PathVariable int accountId);

    @PostMapping(
            value = "/account",
            consumes = "application/json",
            produces = "application/json"
    )
    Account createAccount(@RequestBody Account body);

    @DeleteMapping(
            value = "/account/{accountId}"
    )
    void deleteAccount(@PathVariable int accountId);
}
