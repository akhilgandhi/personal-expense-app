package com.akhil.microservices.core.expense;

import com.akhil.microservices.api.core.expense.Category;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.core.expense.PaymentMode;
import com.akhil.microservices.core.expense.persistence.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExpenseServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ExpenseRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	@Disabled
	void getExpensesByAccountId() {

		int accountId = 1;

		postAndVerifyExpense(accountId, 1, OK);
		postAndVerifyExpense(accountId, 2, OK);
		postAndVerifyExpense(accountId, 3, OK);

		assertEquals(3, repository.findByAccountId(accountId).size());

		getAndVerifyExpensesByAccountId(accountId, OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].accountId").isEqualTo(accountId)
				.jsonPath("$[2].expenseId").isEqualTo(3);
	}

	@Test
	@Disabled
	void duplicateError() {

		int accountId = 1;
		int expenseId = 1;

		postAndVerifyExpense(accountId, expenseId, OK)
				.jsonPath("$.accountId").isEqualTo(accountId)
				.jsonPath("$.expenseId").isEqualTo(expenseId);

		assertEquals(1, repository.count());

		postAndVerifyExpense(accountId, expenseId, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/expense")
				.jsonPath("$.message").isEqualTo("Duplicate key, Account Id: 1, Expense Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	@Disabled
	void deleteExpenses() {

		int accountId = 1;
		int expenseId = 1;

		postAndVerifyExpense(accountId, expenseId, OK);
		assertEquals(1, repository.findByAccountId(accountId).size());

		deleteAndVerifyExpensesByAccountId(accountId, OK);
		assertEquals(0, repository.findByAccountId(accountId).size());

		deleteAndVerifyExpensesByAccountId(accountId, OK);
	}

	@Test
	@Disabled
	void getExpensesMissingParameter() {

		getAndVerifyExpensesByAccountId("", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/expense")
				.jsonPath("$.message").isEqualTo("Required query parameter 'accountId' is not present.");
	}

	@Test
	@Disabled
	void getExpensesInvalidParameter() {

		getAndVerifyExpensesByAccountId("?accountId=no-integer", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/expense")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	@Disabled
	void getExpensesNotFound() {

		getAndVerifyExpensesByAccountId("?accountId=113", OK)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	@Disabled
	void getExpensesInvalidParameterNegativeValue() {

		int accountIdInvalid = -1;

		getAndVerifyExpensesByAccountId("?accountId=" + accountIdInvalid, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/expense")
				.jsonPath("$.message").isEqualTo("Invalid accountId: " + accountIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyExpensesByAccountId(int accountId, HttpStatus expectedStatus) {
		return getAndVerifyExpensesByAccountId("?accountId=" + accountId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyExpensesByAccountId(String accountIdQuery, HttpStatus expectedStatus) {
		return client.get()
				.uri("/expense" + accountIdQuery)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyExpense(int accountId, int expenseId, HttpStatus expectedStatus) {
		Expense expense = new Expense(accountId, expenseId, LocalDateTime.now(), 11.0,
				new Category("a", true), "desc", PaymentMode.CASH, Optional.empty(),
				"SA");
		return client.post()
				.uri("/expense")
				.body(just(expense), Expense.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyExpensesByAccountId(int accountId, HttpStatus expectedStatus) {
		return client.delete()
				.uri("/expense?accountId=" + accountId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}

}
