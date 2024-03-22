package com.akhil.microservices.core.expense.services;

import com.akhil.microservices.api.core.expense.Category;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.core.expense.ExpenseService;
import com.akhil.microservices.api.core.expense.PaymentMode;
import com.akhil.microservices.api.exceptions.InvalidInputException;
import com.akhil.microservices.api.exceptions.NotFoundException;
import com.akhil.microservices.core.expense.persistence.ExpenseEntity;
import com.akhil.microservices.core.expense.persistence.ExpenseRepository;
import com.akhil.microservices.util.http.ServiceUtil;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public List<Expense> getExpenses(int accountId) {

        if (accountId < 1) {
            throw new InvalidInputException("Invalid accountId: " + accountId);
        }

        List<ExpenseEntity> entityList = repository.findByAccountId(accountId);
        List<Expense> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOG.debug("getExpenses: response size: {}", list.size());

        return list;
    }

    @Override
    public Expense createExpense(Expense expense) {
        try {
            ExpenseEntity entity = mapper.apiToEntity(expense);
            ExpenseEntity newEntity = repository.save(entity);

            LOG.debug("createExpense: created a expense entity: {}/{}", expense.getAccountId(), expense.getExpenseId());
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Account Id: " + expense.getAccountId() +
                    ", Expense Id:" + expense.getExpenseId());
        }
    }

    @Override
    public void deleteExpenses(int accountId) {
        LOG.debug("deleteExpenses: tries to delete expenses for the account with accountId: {}", accountId);
        repository.deleteAll(repository.findByAccountId(accountId));
    }
}
