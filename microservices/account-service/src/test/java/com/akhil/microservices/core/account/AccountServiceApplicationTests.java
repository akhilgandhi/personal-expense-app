package com.akhil.microservices.core.account;

import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.event.Event;
import com.akhil.microservices.api.exceptions.InvalidInputException;
import com.akhil.microservices.core.account.persistence.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {
				"eureka.client.enabled=false"
		})
class AccountServiceApplicationTests extends MongoDbTestBase {

	@Autowired
	private WebTestClient client;

	@Autowired
	private AccountRepository repository;

	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Integer, Account>> messageProcessor;

	@BeforeEach
	void setupDb() {
		repository.deleteAll().block();
	}

	@Test
	void getAccountById() {

		int accountId = 1;

		assertNull(repository.findByAccountId(accountId).block());
		assertEquals(0, repository.count().block());

		sendCreateAccountEvent(accountId);

		assertNotNull(repository.findByAccountId(accountId).block());
		assertEquals(1, repository.count().block());

		getAndVerifyAccount(accountId, OK).jsonPath("$.accountId").isEqualTo(accountId);
	}

	@Test
	@Disabled
	void duplicateError() {

		int accountId = 1;

		assertNull(repository.findByAccountId(accountId).block());

		sendCreateAccountEvent(accountId);

		assertNotNull(repository.findByAccountId(accountId).block());

		InvalidInputException thrown = assertThrows(
				InvalidInputException.class,
				() -> sendCreateAccountEvent(accountId),
				"Expected a InvalidInputException here!");
		assertEquals("Duplicate key, Account Id: " + accountId, thrown.getMessage());
	}

	@Test
	void deleteProduct() {

		int accountId = 1;

		sendCreateAccountEvent(accountId);
		assertNotNull(repository.findByAccountId(accountId).block());

		sendDeleteAccountEvent(accountId);
		assertNull(repository.findByAccountId(accountId).block());

		sendDeleteAccountEvent(accountId);
	}

	@Test
	@Disabled
	void getAccountInvalidParameterString() {

		getAndVerifyAccount("/35617356276356787", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/product/35617356276356787")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getAccountNotFound() {

		int accountIdNotFound = 13;
		getAndVerifyAccount(accountIdNotFound, NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/account/" + accountIdNotFound)
				.jsonPath("$.message").isEqualTo("No account found for accountId: " + accountIdNotFound);
	}

	@Test
	void getAccountInvalidParameterNegativeValue() {

		int accountIdInvalid = -1;

		getAndVerifyAccount(accountIdInvalid, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/account/" + accountIdInvalid)
				.jsonPath("$.message").isEqualTo("Invalid accountId: " + accountIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyAccount(int accountId, HttpStatus expectedStatus) {
		return getAndVerifyAccount("/" + accountId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyAccount(String accountIdPath, HttpStatus expectedStatus) {
		return client.get()
				.uri("/account" + accountIdPath)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateAccountEvent(int accountId) {
		Account account = new Account(accountId, "Name " + accountId, "SA");
		Event<Integer, Account> event = new Event<>(Event.Type.CREATE, accountId, account);
		messageProcessor.accept(event);
	}

	private void sendDeleteAccountEvent(int accountId) {
		Event<Integer, Account> event = new Event<>(Event.Type.DELETE, accountId, null);
		messageProcessor.accept(event);
	}
}
