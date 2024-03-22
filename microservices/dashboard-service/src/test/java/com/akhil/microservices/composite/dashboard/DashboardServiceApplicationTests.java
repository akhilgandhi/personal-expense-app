package com.akhil.microservices.composite.dashboard;

import com.akhil.microservices.api.composite.dashboard.DashboardAggregate;
import com.akhil.microservices.api.composite.dashboard.ExpenseSummary;
import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.expense.Category;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.core.expense.PaymentMode;
import com.akhil.microservices.api.exceptions.InvalidInputException;
import com.akhil.microservices.api.exceptions.NotFoundException;
import com.akhil.microservices.composite.dashboard.services.DashboardCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DashboardServiceApplicationTests {

	private static final int ACCOUNT_ID_OK = 1;
	private static final int ACCOUNT_ID_NOT_FOUND = 2;
	private static final int ACCOUNT_ID_INVALID = 3;

	@Autowired
	private WebTestClient client;

	@MockBean
	private DashboardCompositeIntegration integration;

	@BeforeEach
	void setUp() {

		when(integration.getAccount(ACCOUNT_ID_OK))
				.thenReturn(new Account(ACCOUNT_ID_OK, "name", "mock-address"));
		when(integration.getExpenses(ACCOUNT_ID_OK))
				.thenReturn(Collections.singletonList(
						new Expense(
								ACCOUNT_ID_OK,
								1,
								LocalDateTime.now(),
								10.0,
								new Category("Category 1", true),
								"Account 1",
								PaymentMode.CASH,
								Optional.empty(),
								"mock-address")));

		when(integration.getAccount(ACCOUNT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: " + ACCOUNT_ID_NOT_FOUND));

		when(integration.getAccount(ACCOUNT_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID: " + ACCOUNT_ID_INVALID));
	}

	@Test
	void contextLoads() {}

	@Test
	void createAccountSummary() {

		DashboardAggregate aggregateAccount = new DashboardAggregate(
				1,
				"name",
				null,
				null
		);

		postAndVerifyAccount(aggregateAccount, OK);
	}

	@Test
	void createAccountSummary1() {

		DashboardAggregate aggregateAccount = new DashboardAggregate(
				1,
				"name",
				Collections.singletonList(new ExpenseSummary(
						1,
						LocalDateTime.now(),
						10.0,
						new Category("category", true),
						"description",
						PaymentMode.CASH,
						Optional.empty())
				),
				null
		);

		postAndVerifyAccount(aggregateAccount, OK);
	}

	@Test
	void deleteAccountSummary() {
		DashboardAggregate aggregateAccount = new DashboardAggregate(
				1,
				"name",
				Collections.singletonList(new ExpenseSummary(
						1,
						LocalDateTime.now(),
						10.0,
						new Category("category", true),
						"description",
						PaymentMode.CASH,
						Optional.empty())
				),
				null
		);

		postAndVerifyAccount(aggregateAccount, OK);

		deleteAndVerifyAccount(aggregateAccount.getAccountId(), OK);
		deleteAndVerifyAccount(aggregateAccount.getAccountId(), OK);
	}

	@Test
	void getAccountById() {

		client.get()
				.uri("/dashboard/" + ACCOUNT_ID_OK)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.accountId").isEqualTo(ACCOUNT_ID_OK)
				.jsonPath("$.expenses.length()").isEqualTo(1);
	}

	@Test
	void getAccountNotFound() {

		client.get()
				.uri("/dashboard/" + ACCOUNT_ID_NOT_FOUND)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/dashboard/" + ACCOUNT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND: " + ACCOUNT_ID_NOT_FOUND);
	}

	@Test
	void getAccountInvalidInput() {

		client.get()
				.uri("/dashboard/" + ACCOUNT_ID_INVALID)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/dashboard/" + ACCOUNT_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID: " + ACCOUNT_ID_INVALID);
	}

	private void postAndVerifyAccount(DashboardAggregate aggregate, HttpStatus expectedStatus) {
		client.post()
				.uri("/dashboard")
				.body(just(aggregate), DashboardAggregate.class)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}

	private void deleteAndVerifyAccount(int accountId, HttpStatus expectedStatus) {
		client.delete()
				.uri("/dashboard/" + accountId)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus);
	}
}
