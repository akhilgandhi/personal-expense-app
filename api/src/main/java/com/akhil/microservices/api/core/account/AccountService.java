package com.akhil.microservices.api.core.account;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface AccountService {

    @GetMapping(
            value = "/account/{accountId}",
            produces = "application/json"
    )
    Mono<Account> getAccount(@PathVariable int accountId);

    @PostMapping(
            value = "/account",
            consumes = "application/json",
            produces = "application/json"
    )
    Mono<Account> createAccount(@RequestBody Account body);

    @DeleteMapping(
            value = "/account/{accountId}"
    )
    Mono<Void> deleteAccount(@PathVariable int accountId);
}
