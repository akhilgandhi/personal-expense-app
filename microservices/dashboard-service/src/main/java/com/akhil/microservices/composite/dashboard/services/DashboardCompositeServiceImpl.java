package com.akhil.microservices.composite.dashboard.services;

import com.akhil.microservices.api.composite.dashboard.DashboardAggregate;
import com.akhil.microservices.api.composite.dashboard.DashboardCompositeService;
import com.akhil.microservices.api.composite.dashboard.ExpenseSummary;
import com.akhil.microservices.api.composite.dashboard.ServiceAddresses;
import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RestController
public class DashboardCompositeServiceImpl implements DashboardCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardCompositeServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final DashboardCompositeIntegration integration;

    @Autowired
    public DashboardCompositeServiceImpl(ServiceUtil serviceUtil, DashboardCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public Mono<DashboardAggregate> getAccountSummary(int accountId) {

        LOG.info("Will get aggregated account info for account id={}", accountId);

        return Mono.zip(
                values -> createDashboardAggregate((Account) values[0], (List<Expense>) values[1],
                        serviceUtil.getServiceAddress()),
                integration.getAccount(accountId),
                integration.getExpenses(accountId).collectList())
                .doOnError(ex -> LOG.warn("getAccountSummary failed: {}", ex.toString()))
                .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<Void> createAccount(DashboardAggregate body) {

        try {
            List<Mono> monoList = new ArrayList<>();

            LOG.debug("Will create a new composite entity for account.id: {}", body.getAccountId());

            Account account = new Account(body.getAccountId(), body.getName(), null);
            monoList.add(integration.createAccount(account));

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
                            monoList.add(integration.createExpense(expense));
                        }
                );
            }

            LOG.debug("createAccount: composite entities created for accountId: {}", body.getAccountId());

            return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                    .doOnError(ex -> LOG.warn("createDashboardAccount failed: {}", ex.toString()))
                    .then();
        } catch (RuntimeException re) {
            LOG.warn("createDashboardAccount failed", re);
            throw re;
        }
    }

    @Override
    public Mono<Void> deleteAccount(int accountId) {

        try {
            LOG.info("Will delete a account aggregate for product.id: {}", accountId);

            return Mono.zip(
                            r -> "",
                            integration.deleteAccount(accountId),
                            integration.deleteExpenses(accountId))
                    .doOnError(ex -> LOG.warn("deleteDashboardAccount failed: {}", ex.toString()))
                    .log(LOG.getName(), Level.FINE).then();

        } catch (RuntimeException re) {
            LOG.warn("deleteDashboardAccount failed: {}", re.toString());
            throw re;
        }
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
