package com.akhil.microservices.composite.dashboard.services;

import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.account.AccountService;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.core.expense.ExpenseService;
import com.akhil.microservices.api.exceptions.InvalidInputException;
import com.akhil.microservices.api.exceptions.NotFoundException;
import com.akhil.microservices.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class DashboardCompositeIntegration implements AccountService, ExpenseService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String accountServiceUrl;
    private final String expenseServiceUrl;


    @Autowired
    public DashboardCompositeIntegration(RestTemplate restTemplate,
                                         ObjectMapper mapper,
                                         @Value("${app.account-service.host}") String accountServiceHost,
                                         @Value("${app.account-service.port}") int accountServicePort,
                                         @Value("${app.expense-service.host}") String expenseServiceHost,
                                         @Value("${app.expense-service.port}") int expenseServicePort) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        this.accountServiceUrl = "http://" + accountServiceHost + ":" + accountServicePort + "/account";
        this.expenseServiceUrl = "http://" + expenseServiceHost + ":" + expenseServicePort + "/expense";
    }

    public List<Expense> getExpenses(int accountId) {
        try {
            String url = expenseServiceUrl + "?accountId=" + accountId;

            LOG.debug("Will call getExpenses API on URL: {}", url);
            List<Expense> expenses = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<Expense>>() {
                    }).getBody();

            LOG.debug("Found {} expenses for an account with id: {}", expenses.size(), accountId);
            return expenses;
        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting expenses, return zero expenses: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Expense createExpense(Expense expense) {

        try {
            String url = expenseServiceUrl;
            LOG.debug("Will post a new expense to URL: {}", url);

            Expense newExpense = restTemplate.postForObject(url, expense, Expense.class);
            LOG.debug("Created a expense with id: {}", newExpense.getAccountId());

            return newExpense;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteExpenses(int accountId) {

        try {
            String url = expenseServiceUrl + "?accountId=" + accountId;
            LOG.debug("Will call the deleteExpenses API on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Account getAccount(int accountId) {
        try {
            String url = accountServiceUrl + "/" + accountId;
            LOG.debug("Will call getAccount API on URL: {}", url);

            Account account = restTemplate.getForObject(url, Account.class);
            LOG.debug("Found an account with id: {}", account.getAccountId());

            return account;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Account createAccount(Account body) {
        try {
            String url = accountServiceUrl;
            LOG.debug("Will post a new account to URL: {}", url);

            Account account = restTemplate.postForObject(url, body, Account.class);
            LOG.debug("Created a account with id: {}", account.getAccountId());

            return account;
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public void deleteAccount(int accountId) {

        try {
            String url = accountServiceUrl + "/" + accountId;
            LOG.debug("Will call the deleteAccount API on URL: {}", url);

            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (HttpStatus.resolve(ex.getStatusCode().value())) {
            case NOT_FOUND -> throw new NotFoundException(getErrorMessage(ex));
            case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(ex));
            default -> {
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                throw ex;
            }
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ioex.getMessage();
        }
    }
}
