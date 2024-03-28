package com.akhil.microservices.core.account;

import com.akhil.microservices.core.account.persistence.AccountEntity;
import com.akhil.microservices.core.account.persistence.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;

import java.util.Objects;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private AccountRepository repository;

    private AccountEntity savedEntity;

    @BeforeEach
    void setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete();

        AccountEntity entity = new AccountEntity(1, "a");
        StepVerifier.create(repository.save(entity))
                .expectNextMatches(createdEntity -> {
                    savedEntity = createdEntity;
                    return areAccountEqual(entity, savedEntity);
                })
                .verifyComplete();
    }

    @Test
    void create() {

        AccountEntity newEntity = new AccountEntity(2, "n");

        StepVerifier.create(repository.save(newEntity))
                .expectNextMatches(createdEntity -> newEntity.getAccountId() == createdEntity.getAccountId())
                .verifyComplete();

        StepVerifier.create(repository.findById(newEntity.getId()))
                .expectNextMatches(foundEntity -> areAccountEqual(newEntity, foundEntity))
                .verifyComplete();

        StepVerifier.create(repository.count())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void update() {
        savedEntity.setName("n2");
        StepVerifier.create(repository.save(savedEntity))
                .expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
                .verifyComplete();

        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity -> foundEntity.getVersion() == 1 && foundEntity.getName().equals("n2"))
                .verifyComplete();
    }

    @Test
    void delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
        StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
    }

    @Test
    void getByAccountId() {
        StepVerifier.create(repository.findByAccountId(savedEntity.getAccountId()))
                .expectNextMatches(foundEntity -> areAccountEqual(savedEntity, foundEntity))
                .verifyComplete();
    }

    @Test
    @Disabled
    void duplicateError() {
        AccountEntity entity = new AccountEntity(savedEntity.getAccountId(), "n");
        StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
    }

    @Test
    void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        AccountEntity entity1 = repository.findById(savedEntity.getId()).block();
        AccountEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setName("n1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

        // Get the updated entity from the database and verify its new state
        StepVerifier.create(repository.findById(savedEntity.getId()))
                .expectNextMatches(foundEntity ->
                        foundEntity.getVersion() == 1
                                && foundEntity.getName().equals("n1"))
                .verifyComplete();
    }

    private boolean areAccountEqual(AccountEntity expectedEntity, AccountEntity actualEntity) {
        return
                (expectedEntity.getId().equals(actualEntity.getId()))
                && (Objects.equals(expectedEntity.getVersion(), actualEntity.getVersion()))
                && (expectedEntity.getAccountId() == actualEntity.getAccountId())
                && (Objects.equals(expectedEntity.getName(), actualEntity.getName()));
    }
}
