package com.akhil.microservices.composite.dashboard.services;

import com.akhil.microservices.api.composite.dashboard.DashboardAggregate;
import com.akhil.microservices.api.composite.dashboard.DashboardCompositeService;
import com.akhil.microservices.api.composite.dashboard.ExpenseSummary;
import com.akhil.microservices.api.composite.dashboard.ServiceAddresses;
import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.exceptions.NotFoundException;
import com.akhil.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DashboardCompositeServiceImpl implements DashboardCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardCompositeServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private DashboardCompositeIntegration integration;

    @Autowired
    public DashboardCompositeServiceImpl(ServiceUtil serviceUtil, DashboardCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public DashboardAggregate getAccountSummary(int accountId) {

        LOG.debug("getAccountSummary: lookup a dashboard aggregate for accountId: {}", accountId);

        Account account = integration.getAccount(accountId);

        if (account == null) {
            throw new NotFoundException("No account found for accountId: " + accountId);
        }

        List<Expense> expenses = integration.getExpenses(accountId);

        LOG.debug("getAccountSummary: aggregate entity found for accountId: {}", accountId);

        return createDashboardAggregate(account, expenses, serviceUtil.getServiceAddress());
    }

    @Override
    public void createAccount(DashboardAggregate body) {

        try {
            LOG.debug("createAccount: creates a new aggregate entity for accountId: {}", body.getAccountId());

            Account account = new Account(body.getAccountId(), body.getName(), null);
            integration.createAccount(account);

            if (body.getExpenses() != null) {
                body.getExpenses().forEach(
                        exps -> {
                            Expense expense = new Expense(body.getAccountId(),
                                    exps.getExpenseId(),
                                    exps.getTransactionDateTime(),
                                    exps.getAmount(),
                                    exps.getCategory(),
                                    exps.getDescription(),
                                    exps.getPaymentMode(),
                                    exps.getNotes(),
                                    null);
                            integration.createExpense(expense);
                        }
                );
            }

            LOG.debug("createAccount: composite entities created for accountId: {}", body.getAccountId());
        } catch (RuntimeException re) {
            LOG.warn("createAccount failed", re);
            throw re;
        }
    }

    @Override
    public void deleteAccount(int accountId) {

        LOG.debug("deleteAccount: Deletes a dashboard account aggregate for accountId: {}", accountId);

        integration.deleteAccount(accountId);
        integration.deleteExpenses(accountId);

        LOG.debug("deleteAccount: aggregate entities deleted for accountId: {}", accountId);
    }

    private DashboardAggregate createDashboardAggregate(Account account, List<Expense> expenses, String serviceAddress) {

        // 1. setup account info
        int accountId = account.getAccountId();
        String name = account.getName();

        // 2. Copy summary expense info, if available
        List<ExpenseSummary> expenseSummaries = (expenses == null) ? null : expenses.stream()
                .map(expense -> new ExpenseSummary(expense.getExpenseId(),
                        expense.getTransactionDateTime(),
                        expense.getAmount(),
                        expense.getCategory(),
                        expense.getDescription(),
                        expense.getPaymentMode(),
                        expense.getNotes()
                        ))
                .toList();

        // 3. create info regarding the involved microservices addresses
        String accountAddress = account.getServiceAddress();
        String expenseAddress = (expenses != null && !expenses.isEmpty()) ? expenses.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(accountAddress, expenseAddress);

        return new DashboardAggregate(accountId, name, expenseSummaries, serviceAddresses);
    }
}
