package com.akhil.microservices.core.expense;

import com.akhil.microservices.api.core.expense.Category;
import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.core.expense.PaymentMode;
import com.akhil.microservices.api.event.Event;
import com.akhil.microservices.api.exceptions.InvalidInputException;
import com.akhil.microservices.core.expense.persistence.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"eureka.client.enabled=false"
		})
class ExpenseServiceApplicationTests extends MongoDbTestBase {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ExpenseRepository repository;

	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Integer, ?>> messageProcessor;

	@BeforeEach
	void setupDb() {
		repository.deleteAll().block();
	}

	@Test
	void getExpensesByAccountId() {

		int accountId = 1;

		sendCreateExpenseEvent(accountId, 1);
		sendCreateExpenseEvent(accountId, 2);
		sendCreateExpenseEvent(accountId, 3);

		assertEquals(3, repository.findByAccountId(accountId).count().block());

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

		sendCreateExpenseEvent(accountId, expenseId);

		assertEquals(1, repository.count().block());

		InvalidInputException thrown = assertThrows(
				InvalidInputException.class,
				() -> sendCreateExpenseEvent(accountId, expenseId),
				"Expected a InvalidInputException here!");
		assertEquals("Duplicate key, Account Id: 1, Expense Id:1", thrown.getMessage());

		assertEquals(1, repository.count().block());
	}

	@Test
	void deleteExpenses() {

		int accountId = 1;
		int expenseId = 1;

		sendCreateExpenseEvent(accountId, expenseId);
		assertEquals(1, repository.findByAccountId(accountId).count().block());

		sendDeleteExpenseEvent(accountId);
		assertEquals(0, repository.findByAccountId(accountId).count().block());

		sendDeleteExpenseEvent(accountId);
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

		getAndVerifyExpensesByAccountId("?accountId=346878787521376", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/expense")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getExpensesNotFound() {

		getAndVerifyExpensesByAccountId("?accountId=113", OK)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
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

	private void sendCreateExpenseEvent(int accountId, int expenseId) {
		Expense expense = new Expense(accountId,
				expenseId, LocalDateTime.now(), 10.0,
				new Category("category-1", true), "Content " + expenseId,
				PaymentMode.CASH, null, "SA");
		Event<Integer, Expense> event = new Event<>(Event.Type.CREATE, accountId, expense);
		messageProcessor.accept(event);
	}

	private void sendDeleteExpenseEvent(int accountId) {
		Event<Integer, Expense> event = new Event<>(Event.Type.DELETE, accountId, null);
		messageProcessor.accept(event);
	}

}
