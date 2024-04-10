package com.akhil.microservices.core.expense.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseRepository extends ReactiveCrudRepository<ExpenseEntity, String> {

    Flux<ExpenseEntity> findByAccountId(int accountId);

    Mono<ExpenseEntity> findByAccountIdAndExpenseId(int accountId, int expenseId);
}
