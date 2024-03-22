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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.data.domain.Sort.Direction.ASC;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private AccountRepository repository;

    private AccountEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        AccountEntity entity = new AccountEntity(1, "a");
        savedEntity = repository.save(entity);

        assertEqualsAccount(entity, savedEntity);
    }

    @Test
    void create() {

        AccountEntity newEntity = new AccountEntity(2, "n");
        repository.save(newEntity);

        AccountEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsAccount(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedEntity.setName("n2");
        repository.save(savedEntity);

        AccountEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("n2", foundEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        Optional<AccountEntity> entity = repository.findByAccountId(savedEntity.getAccountId());

        assertTrue(entity.isPresent());
        assertEqualsAccount(savedEntity, entity.get());
    }

    @Test
    @Disabled
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            AccountEntity entity = new AccountEntity(1, "a");
            repository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        AccountEntity entity1 = repository.findById(savedEntity.getId()).get();
        AccountEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setName("n1");
        repository.save(entity1);

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setName("n2");
            repository.save(entity2);
        });

        // Get the updated entity from the database and verify its new sate
        AccountEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());
    }

    @Test
    void paging() {

        repository.deleteAll();

        List<AccountEntity> newAccounts = rangeClosed(1001, 1010)
                .mapToObj(i -> new AccountEntity(i, "name " + i))
                .toList();
        repository.saveAll(newAccounts);

        Pageable nextPage = PageRequest.of(0, 4, ASC, "accountId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<AccountEntity> productPage = repository.findAll(nextPage);
        assertEquals(expectedProductIds, productPage.getContent().stream().map(AccountEntity::getAccountId).toList().toString());
        assertEquals(expectsNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }

    private void assertEqualsAccount(AccountEntity expectedEntity, AccountEntity actualEntity) {

        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getAccountId(), actualEntity.getAccountId());
        assertEquals(expectedEntity.getName(), actualEntity.getName());
    }
}
