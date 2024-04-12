package com.akhil.microservices.composite.dashboard;

import com.akhil.microservices.api.composite.dashboard.AccountSummary;
import com.akhil.microservices.api.composite.dashboard.DashboardAggregate;
import com.akhil.microservices.api.composite.dashboard.ExpenseSummary;
import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.expense.Category;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.core.expense.PaymentMode;
import com.akhil.microservices.api.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.akhil.microservices.composite.dashboard.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(
        classes = {TestSecurityConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "eureka.client.enabled=false"
        }
)
@Import({
        TestChannelBinderConfiguration.class
})
public class MessagingTests {

    private static final Logger LOG = LoggerFactory.getLogger(MessagingTests.class);

    @Autowired
    private WebTestClient client;

    @Autowired
    private OutputDestination target;

    @BeforeEach
    void setUp() {
        purgeMessages("accounts");
        purgeMessages("expenses");
    }

    @Test
    void createDashboard1() {

        DashboardAggregate composite = new DashboardAggregate(new AccountSummary(1, "name"), null, null);
        postAndVerifyAccount(composite, OK);

        final List<String> accountMessages = getMessages("accounts");
        final List<String> expenseMessages = getMessages("expenses");

        // Assert one expected new product event queued up
        assertEquals(1, accountMessages.size());

        Event<Integer, Account> expectedEvent = new Event<>(Event.Type.CREATE, composite.getAccount().getAccountId(),
                new Account(composite.getAccount().getAccountId(), composite.getAccount().getName(), null));
        assertThat(accountMessages.get(0), is(sameEventExceptCreatedAt(expectedEvent)));

        // Assert no recommendation and review events
        assertEquals(0, expenseMessages.size());
    }

    @Test
    void createDashboard2() {

        DashboardAggregate composite = new DashboardAggregate(new AccountSummary(1, "name"),
                Collections.singletonList(
                        new ExpenseSummary(1, LocalDateTime.now(), 10.0,
                                new Category("c", true),
                                "Content", PaymentMode.CASH, null)), null);
        postAndVerifyAccount(composite, OK);

        final List<String> accountMessages = getMessages("accounts");
        final List<String> expenseMessages = getMessages("expenses");

        // Assert one create account event queued up
        assertEquals(1, accountMessages.size());

        Event<Integer, Account> expectedAccountEvent =
                new Event<>(Event.Type.CREATE, composite.getAccount().getAccountId(),
                        new Account(composite.getAccount().getAccountId(), composite.getAccount().getName(), null));
        assertThat(accountMessages.get(0), is(sameEventExceptCreatedAt(expectedAccountEvent)));

        // Assert one create expense event queued up
        assertEquals(1, expenseMessages.size());

        ExpenseSummary rec = composite.getExpenses().get(0);
        Event<Integer, Expense> expectedExpenseEvent =
                new Event<>(Event.Type.CREATE, composite.getAccount().getAccountId(),
                        new Expense(composite.getAccount().getAccountId(),
                                rec.getExpenseId(),
                                rec.getTransactionDateTime(),
                                rec.getAmount(),
                                rec.getCategory(),
                                rec.getDescription(),
                                rec.getPaymentMode(),
                                rec.getNotes(), null));
        assertThat(expenseMessages.get(0), is(sameEventExceptCreatedAt(expectedExpenseEvent)));
    }

    @Test
    void deleteCompositeProduct() {
        deleteAndVerifyAccount(1, OK);

        final List<String> accountMessages = getMessages("accounts");
        final List<String> expenseMessages = getMessages("expenses");

        // Assert one delete account event queued up
        assertEquals(1, accountMessages.size());

        Event<Integer, Account> expectedAccountEvent = new Event(Event.Type.DELETE, 1, null);
        assertThat(accountMessages.get(0), is(sameEventExceptCreatedAt(expectedAccountEvent)));

        // Assert one delete expense event queued up
        assertEquals(1, expenseMessages.size());

        Event<Integer, Expense> expectedExpenseEvent = new Event(Event.Type.DELETE, 1, null);
        assertThat(expenseMessages.get(0), is(sameEventExceptCreatedAt(expectedExpenseEvent)));
    }

    private void postAndVerifyAccount(DashboardAggregate compositeProduct, HttpStatus expectedStatus) {
        client.post()
                .uri("/dashboard")
                .body(just(compositeProduct), DashboardAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyAccount(int accountId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/dashboard/account/" + accountId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void purgeMessages(String bindingName) {
        getMessages(bindingName);
    }

    private List<String> getMessages(String bindingName) {
        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;

        while (anyMoreMessages) {
            Message<byte[]> message = getMessage(bindingName);

            if (message == null) {
                anyMoreMessages = false;

            } else {
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }

    private Message<byte[]> getMessage(String bindingName) {
        try {
            return target.receive(0, bindingName);
        } catch (NullPointerException npe) {
            // If the messageQueues member variable in the target object contains no queues when
            // the receive method is called, it will cause a NPE to be thrown.
            // So we catch the NPE here and return null to indicate that no messages were found.
            LOG.error("getMessage() received a NPE with binding = {}", bindingName);
            return null;
        }
    }
}
