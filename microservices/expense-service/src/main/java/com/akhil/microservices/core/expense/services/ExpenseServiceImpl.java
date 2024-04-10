package com.akhil.microservices.core.expense.services;

import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.core.expense.ExpenseService;
import com.akhil.microservices.api.exceptions.InvalidInputException;
import com.akhil.microservices.core.expense.persistence.ExpenseEntity;
import com.akhil.microservices.core.expense.persistence.ExpenseRepository;
import com.akhil.microservices.util.http.ServiceUtil;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@RestController
public class ExpenseServiceImpl implements ExpenseService {

    private static final Logger LOG = LoggerFactory.getLogger(ExpenseServiceImpl.class);

    private final ExpenseRepository repository;
    private final ExpenseMapper mapper;
    private final ServiceUtil serviceUtil;

    public ExpenseServiceImpl(ExpenseRepository repository, ExpenseMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Flux<Expense> getExpenses(int accountId) {

        if (accountId < 1) {
            throw new InvalidInputException("Invalid accountId: " + accountId);
        }

        LOG.info("Will get expenses for account with id={}", accountId);

        return repository.findByAccountId(accountId)
                .log(LOG.getName(), Level.FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Expense> createExpense(Expense expense) {

        if (expense.getAccountId() < 1) {
            throw new InvalidInputException("Invalid accountId: " + expense.getAccountId());
        }

        ExpenseEntity entity = mapper.apiToEntity(expense);
        Mono<Expense> newEntity = repository.save(entity)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate Key, Account Id: " + expense.getAccountId() +
                                ", Expense Id: " + expense.getExpenseId())
                )
                .map(mapper::entityToApi);

        return newEntity;
    }

    @Override
    public Mono<Void> deleteExpenses(int accountId) {
        if (accountId < 1) {
            throw new InvalidInputException("Invalid accountId: " + accountId);
        }

        LOG.debug("deleteExpenses: tries to delete expenses for the account with accountId: {}", accountId);
        return repository.deleteAll(repository.findByAccountId(accountId));
    }

    @Override
    public Mono<Void> deleteExpense(int accountId, int expenseId) {
        if (accountId < 1 || expenseId < 1) {
            throw new InvalidInputException("Invalid accountId: " + accountId + ", or expenseId: " + expenseId);
        }

        LOG.debug("deleteExpense: tries to delete expense with expenseId: {} for the account with accountId: {}",
                expenseId, accountId);
        return repository.findByAccountIdAndExpenseId(accountId, expenseId)
                .log(LOG.getName(), Level.FINE)
                .map(repository::delete)
                .flatMap(e -> e);
    }

    private Expense setServiceAddress(Expense expense) {
        expense.setServiceAddress(serviceUtil.getServiceAddress());
        return expense;
    }
}
