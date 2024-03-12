package com.akhil.microservices.api.core.expense;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ExpenseService {

    @GetMapping(
            value = "/expense",
            produces = "application/json"
    )
    List<Expense> getExpenses(@RequestParam(value = "accountId", required = true) int accountId);
}
