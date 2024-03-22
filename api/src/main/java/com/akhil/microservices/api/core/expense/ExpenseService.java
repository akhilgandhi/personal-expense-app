package com.akhil.microservices.api.core.expense;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ExpenseService {

    @GetMapping(
            value = "/expense",
            produces = "application/json"
    )
    List<Expense> getExpenses(@RequestParam(value = "accountId", required = true) int accountId);

    @PostMapping(
            value = "/expense",
            consumes = "application/json",
            produces = "application/json"
    )
    Expense createExpense(@RequestBody Expense expense);

    @DeleteMapping(
            value = "/expense"
    )
    void deleteExpenses(@RequestParam(value = "accountId", required = true) int accountId);
}
