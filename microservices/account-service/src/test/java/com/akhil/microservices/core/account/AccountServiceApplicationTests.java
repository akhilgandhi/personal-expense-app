package com.akhil.microservices.core.account;

import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.core.account.persistence.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountServiceApplicationTests extends MongoDbTestBase {

	@Autowired
	private WebTestClient client;

	@Autowired
	private AccountRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getAccountById() {

		int accountId = 1;

		postAndVerifyAccount(accountId, OK);

		assertTrue(repository.findByAccountId(accountId).isPresent());

		getAndVerifyAccount(accountId, OK).jsonPath("$.accountId").isEqualTo(accountId);
	}

	@Test
	@Disabled
	void duplicateError() {

		int accountId = 1;

		postAndVerifyAccount(accountId, OK);

		assertTrue(repository.findByAccountId(accountId).isPresent());

		postAndVerifyAccount(accountId, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/account")
				.jsonPath("$.message").isEqualTo("Duplicate key, Account Id: " + accountId);
	}

	@Test
	void deleteProduct() {

		int accountId = 1;

		postAndVerifyAccount(accountId, OK);
		assertTrue(repository.findByAccountId(accountId).isPresent());

		deleteAndVerifyAccount(accountId, OK);
		assertFalse(repository.findByAccountId(accountId).isPresent());

		deleteAndVerifyAccount(accountId, OK);
	}

	@Test
	void getProductInvalidParameterString() {

		getAndVerifyAccount("/32412232476575", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/account/32412232476575");
	}

	@Test
	void getProductNotFound() {

		int accountIdNotFound = 13;
		getAndVerifyAccount(accountIdNotFound, NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/account/" + accountIdNotFound)
				.jsonPath("$.message").isEqualTo("No account found for accountId: " + accountIdNotFound);
	}

	@Test
	void getProductInvalidParameterNegativeValue() {

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

	private WebTestClient.BodyContentSpec postAndVerifyAccount(int accountId, HttpStatus expectedStatus) {
		Account account = new Account(accountId, "Name " + accountId, "SA");
		return client.post()
				.uri("/account")
				.body(just(account), Account.class)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyAccount(int accountId, HttpStatus expectedStatus) {
		return client.delete()
				.uri("/account/" + accountId)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}
}
