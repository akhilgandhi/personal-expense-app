package com.akhil.microservices.composite.dashboard.services;

import com.akhil.microservices.api.composite.dashboard.*;
import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.composite.dashboard.services.tracing.ObservationUtil;
import com.akhil.microservices.util.http.ServiceUtil;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RestController
public class DashboardCompositeServiceImpl implements DashboardCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardCompositeServiceImpl.class);

    private final SecurityContext nullSecCtx = new SecurityContextImpl();

    private final ServiceUtil serviceUtil;
    private final ObservationUtil observationUtil;
    private final DashboardCompositeIntegration integration;

    public DashboardCompositeServiceImpl(ServiceUtil serviceUtil, ObservationUtil observationUtil, DashboardCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
      this.observationUtil = observationUtil;
      this.integration = integration;
    }

    @Override
    public Mono<DashboardAggregate> getDashboardSummary(int accountId,
        int delay, int faultPercent) {

        return observationWithAccountInfo(accountId,
            () -> getDashboardSummaryInternal(accountId, delay, faultPercent));
    }

    private Mono<DashboardAggregate> getDashboardSummaryInternal(int accountId, int delay,
        int faultPercent) {
        LOG.info("Will get aggregated account info for account id={}", accountId);

        return Mono.zip(
                values -> createDashboardAggregate((Account) values[0], (List<Expense>) values[1],
                    serviceUtil.getServiceAddress()),
                integration.getAccount(accountId, delay, faultPercent),
                integration.getExpenses(accountId).collectList())
            .doOnError(ex -> LOG.warn("getAccountSummary failed: {}", ex.toString()))
            .log(LOG.getName(), Level.FINE);
    }

    @Override
    public Mono<Void> createAccount(DashboardAggregate body) {

        return observationWithAccountInfo(body.getAccount().getAccountId(),
            () -> createAccountInternal(body));
    }

    private Mono<Void> createAccountInternal(DashboardAggregate body) {
        try {
            List<Mono> monoList = new ArrayList<>();

            LOG.debug("Will create a new composite entity for account.id: {}", body.getAccount().getAccountId());

            Account account = new Account(body.getAccount().getAccountId(), body.getAccount().getName(), null);
            monoList.add(integration.createAccount(account));

            if (body.getExpenses() != null) {
                body.getExpenses().forEach(
                        exps -> {
                            Expense expense = new Expense(body.getAccount().getAccountId(),
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

            LOG.debug("createAccount: composite entities created for accountId: {}", body.getAccount().getAccountId());

            return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                .doOnError(ex -> LOG.warn("createDashboardAccount failed: {}", ex.toString()))
                .then();
        } catch (RuntimeException re) {
            LOG.warn("createDashboardAccount failed", re);
            throw re;
        }
    }

    @Override
    public Mono<Void> createAccount(AccountSummary body) {

        return observationWithAccountInfo(body.getAccountId(), () -> createAccountInternal(body));
    }

    private Mono<Void> createAccountInternal(AccountSummary body) {
        try {
            LOG.debug("Will create a new account entity for account.id: {}", body.getAccountId());

            Account account = new Account(body.getAccountId(), body.getName(), null);
            return integration.createAccount(account)
                .doOnSuccess(res -> LOG.debug("createAccount: account created for accountId: {}",
                    body.getAccountId()))
                .doOnError(ex -> LOG.warn("createDashboardAccount failed: {}", ex.toString()))
                .then();
        } catch (RuntimeException re) {
            LOG.warn("createDashboardAccount failed", re);
            throw re;
        }
    }

    @Override
    public Mono<Void> deleteAccount(int accountId) {

        return observationWithAccountInfo(accountId, () -> deleteAccountInternal(accountId));
    }

    private Mono<Void> deleteAccountInternal(int accountId) {
        try {
            LOG.info("Will delete a account aggregate for account.id: {}", accountId);

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

    @Override
    public Mono<Void> createExpense(int accountId, ExpenseSummary body) {

        try {
            LOG.debug("Will create a new expense entity for account.id: {}", accountId);

            Expense expense = new Expense(accountId,
                    body.getExpenseId(),
                    body.getTransactionDateTime(),
                    body.getAmount(),
                    body.getCategory(),
                    body.getDescription(),
                    body.getPaymentMode(),
                    body.getNotes(),
                    null);

            return integration.createExpense(expense)
                    .doOnSuccess(res -> LOG.debug("createExpense: expense created for accountId: {}", accountId))
                    .doOnError(ex -> LOG.warn("createExpense failed: {}", ex.toString()))
                    .then();

        } catch (RuntimeException re) {
            LOG.warn("createExpense failed", re);
            throw re;
        }
    }

    @Override
    public Mono<Void> deleteExpense(int accountId, int expenseId) {

        try {
            LOG.info("Will delete an expense for account.id: {}, expense.id: {}", accountId, expenseId);

            return integration.deleteExpense(accountId, expenseId)
                    .doOnSuccess(res -> LOG.debug("deleteExpense: expense with expenseId {} deleted for accountId: {}",
                            expenseId, accountId))
                    .doOnError(ex -> LOG.warn("deleteExpense failed: {}", ex.toString()))
                    .then();
        } catch (RuntimeException re) {
            LOG.warn("deleteExpense failed", re);
            throw re;
        }
    }

    private <T> T observationWithAccountInfo(int accountInfo, Supplier<T> supplier) {
        return observationUtil.observe(
            "dashboard observation",
            "account info",
            "accountId",
            String.valueOf(accountInfo),
            supplier
        );
    }

    private DashboardAggregate createDashboardAggregate(Account account, List<Expense> expenses, String serviceAddress) {

        // 1. setup account info
        AccountSummary accountSummary = new AccountSummary(account.getAccountId(), account.getName());

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

        return new DashboardAggregate(accountSummary, expenseSummaries, serviceAddresses);
    }

    private Mono<SecurityContext> getLogAuthorizationInfoMono() {
        return getSecurityContextMono().doOnNext(this::logAuthorizationInfo);
    }

    private Mono<SecurityContext> getSecurityContextMono() {
        return ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSecCtx);
    }

    private void logAuthorizationInfo(SecurityContext sc) {
        if (sc != null && sc.getAuthentication() != null && sc.getAuthentication() instanceof JwtAuthenticationToken) {
            Jwt jwtToken = ((JwtAuthenticationToken)sc.getAuthentication()).getToken();
            logAuthorizationInfo(jwtToken);
        } else {
            LOG.warn("No JWT based Authentication supplied, running tests are we?");
        }
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) {
            LOG.warn("No JWT supplied, running tests are we?");
        } else {
            if (LOG.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getClaims().get("exp");

                LOG.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject, scopes, expires, issuer, audience);
            }
        }
    }
}
