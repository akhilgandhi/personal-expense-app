package com.akhil.microservices.core.expense.services;

import com.akhil.microservices.api.core.expense.Category;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.core.expense.ExpenseService;
import com.akhil.microservices.api.core.expense.PaymentMode;
import com.akhil.microservices.api.exceptions.InvalidInputException;
import com.akhil.microservices.api.exceptions.NotFoundException;
import com.akhil.microservices.util.http.ServiceUtil;
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
    private final ServiceUtil serviceUtil;

    public ExpenseServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Expense> getExpenses(int accountId) {

        if (accountId < 1) {
            throw new InvalidInputException("Invalid accountId: " + accountId);
        }

        if (accountId == 113) {
            LOG.debug("No expenses found for accountId: " + accountId);
            return new ArrayList<>();
        }

        List<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense(accountId, 1, LocalDateTime.now(), 10.0,
                new Category("Category 1", true), "Expense 1", PaymentMode.CASH,
                Optional.empty(), serviceUtil.getServiceAddress()));
        expenses.add(new Expense(accountId, 2, LocalDateTime.now(), 11.0,
                new Category("Category 2", true), "Expense 2", PaymentMode.CASH,
                Optional.empty(), serviceUtil.getServiceAddress()));

        LOG.debug("/expense response size: {}", expenses.size());

        return expenses;
    }
}
