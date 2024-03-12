package com.akhil.microservices.api.core.account;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface AccountService {

    @GetMapping(
            value = "/account/{accountId}",
            produces = "application/json"
    )
    Account getAccount(@PathVariable int accountId);
}
