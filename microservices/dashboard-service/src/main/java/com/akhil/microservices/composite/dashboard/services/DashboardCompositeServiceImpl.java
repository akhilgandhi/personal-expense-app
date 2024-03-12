package com.akhil.microservices.composite.dashboard.services;

import com.akhil.microservices.api.composite.dashboard.DashboardAggregate;
import com.akhil.microservices.api.composite.dashboard.DashboardCompositeService;
import com.akhil.microservices.api.composite.dashboard.ExpenseSummary;
import com.akhil.microservices.api.composite.dashboard.ServiceAddresses;
import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.exceptions.NotFoundException;
import com.akhil.microservices.util.http.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DashboardCompositeServiceImpl implements DashboardCompositeService {

    private final ServiceUtil serviceUtil;
    private DashboardCompositeIntegration integration;

    @Autowired
    public DashboardCompositeServiceImpl(ServiceUtil serviceUtil, DashboardCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public DashboardAggregate getAccountSummary(int accountId) {

        Account account = integration.getAccount(accountId);

        if (account == null) {
            throw new NotFoundException("No account found for accountId: " + accountId);
        }

        List<Expense> expenses = integration.getExpenses(accountId);
        return createDashboardAggregate(account, expenses, serviceUtil.getServiceAddress());
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
                        expense.getDescription()
                        ))
                .toList();

        // 3. create info regarding the involved microservices addresses
        String accountAddress = account.getServiceAddress();
        String expenseAddress = (expenses != null && !expenses.isEmpty()) ? expenses.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(accountAddress, expenseAddress);

        return new DashboardAggregate(accountId, name, expenseSummaries, serviceAddresses);
    }
}
