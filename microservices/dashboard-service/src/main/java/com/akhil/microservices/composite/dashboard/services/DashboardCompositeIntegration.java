package com.akhil.microservices.composite.dashboard.services;

import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.account.AccountService;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.core.expense.ExpenseService;
import com.akhil.microservices.api.event.Event;
import com.akhil.microservices.api.exceptions.InvalidInputException;
import com.akhil.microservices.api.exceptions.NotFoundException;
import com.akhil.microservices.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.logging.Level;

@Component
public class DashboardCompositeIntegration implements AccountService, ExpenseService {

    private static final Logger LOG = LoggerFactory.getLogger(DashboardCompositeIntegration.class);
    public static final String EXPENSES_BINDING = "expenses-out-0";
    public static final String ACCOUNTS_BINDING = "accounts-out-0";

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final String accountServiceUrl;
    private final String expenseServiceUrl;

    private final StreamBridge streamBridge;
    private final Scheduler publishEventScheduler;


    @Autowired
    public DashboardCompositeIntegration(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient.Builder webclient,
            ObjectMapper mapper,
            StreamBridge streamBridge,
            @Value("${app.account-service.host}") String accountServiceHost,
            @Value("${app.account-service.port}") int accountServicePort,
            @Value("${app.expense-service.host}") String expenseServiceHost,
            @Value("${app.expense-service.port}") int expenseServicePort) {
        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webclient.build();
        this.mapper = mapper;
        this.streamBridge = streamBridge;

        this.accountServiceUrl = "http://" + accountServiceHost + ":" + accountServicePort;
        this.expenseServiceUrl = "http://" + expenseServiceHost + ":" + expenseServicePort;
    }

    public Flux<Expense> getExpenses(int accountId) {

        String url = expenseServiceUrl + "/expense?accountId=" + accountId;

        LOG.debug("Will call the getExpenses API on url: {}", url);

        // Return an empty result if something goes wrong to make it possible for the dashboard service to
        // return partial responses
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Expense.class)
                .log(LOG.getName(), Level.FINE)
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public Mono<Expense> createExpense(Expense expense) {

        return Mono.fromCallable(() -> {
            sendMessage(EXPENSES_BINDING, new Event<>(Event.Type.CREATE, expense.getAccountId(), expense));
            return expense;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteExpenses(int accountId) {

        return Mono.fromRunnable(() -> sendMessage(EXPENSES_BINDING,
                new Event<>(Event.Type.DELETE, accountId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Mono<Account> getAccount(int accountId) {

        String url = accountServiceUrl + "/account/" + accountId;

        LOG.debug("Will call the getAccount API on url: {}", url);

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Account.class)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Account> createAccount(Account body) {
        return Mono.fromCallable(() -> {

            sendMessage(ACCOUNTS_BINDING,
                    new Event<>(Event.Type.CREATE, body.getAccountId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteAccount(int accountId) {

        return Mono.fromRunnable(() -> sendMessage(ACCOUNTS_BINDING,
                new Event<>(Event.Type.DELETE, accountId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    // health endpoints
    public Mono<Health> getAccountHealth() {
        return getHealth(accountServiceUrl);
    }

    public Mono<Health> getExpenseHealth() {
        return getHealth(expenseServiceUrl);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOG.debug("Will call the Health API on url: {}", url);
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(
                        new Health.Builder().down(ex).build()
                ))
                .log(LOG.getName(), Level.FINE);
    }

    private void sendMessage(String bindingName, Event event) {
        LOG.debug("Sending a {} message to {}", event.getType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got an unexpected error: {}, will rethrow it", ex.toString());
        }

        WebClientResponseException wcre = (WebClientResponseException) ex;

        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
            case NOT_FOUND -> throw new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(wcre));
            default -> {
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
            }
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ioex.getMessage();
        }
    }
}
