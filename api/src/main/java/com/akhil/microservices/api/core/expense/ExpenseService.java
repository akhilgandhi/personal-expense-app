package com.akhil.microservices.api.core.expense;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseService {

    @GetMapping(
            value = "/expense",
            produces = "application/json"
    )
    Flux<Expense> getExpenses(@RequestParam(value = "accountId", required = true) int accountId);

    @PostMapping(
            value = "/expense",
            consumes = "application/json",
            produces = "application/json"
    )
    Mono<Expense> createExpense(@RequestBody Expense expense);

    @DeleteMapping(
            value = "/expense/all"
    )
    Mono<Void> deleteExpenses(@RequestParam(value = "accountId", required = true) int accountId);

    @DeleteMapping(
            value = "/expense"
    )
    Mono<Void> deleteExpense(@RequestParam(value = "accountId", required = true) int accountId,
                             @RequestParam(value = "expenseId", required = true) int expenseId);
}
