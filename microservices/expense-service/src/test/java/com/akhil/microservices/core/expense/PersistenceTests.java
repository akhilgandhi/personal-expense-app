package com.akhil.microservices.core.expense;

import com.akhil.microservices.api.core.expense.Category;
import com.akhil.microservices.api.core.expense.PaymentMode;
import com.akhil.microservices.core.expense.persistence.ExpenseEntity;
import com.akhil.microservices.core.expense.persistence.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private ExpenseRepository repository;

    private ExpenseEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        ExpenseEntity entity = new ExpenseEntity(1, 1, LocalDateTime.now(),
                10.0, new Category("c", true), "desc",
                PaymentMode.CASH, Optional.empty());
        savedEntity = repository.save(entity);

        assertEqualsExpense(entity, savedEntity);
    }


    @Test
    @Disabled
    void create() {

        ExpenseEntity newEntity = new ExpenseEntity(1, 2, LocalDateTime.now(),
                11.0, new Category("c", true), "desc",
                PaymentMode.CASH, Optional.empty());
        repository.save(newEntity);

        ExpenseEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsExpense(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
    @Disabled
    void update() {
        savedEntity.setDescription("description");
        repository.save(savedEntity);

        ExpenseEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("description", foundEntity.getDescription());
    }

    @Test
    @Disabled
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    @Disabled
    void getByProductId() {
        List<ExpenseEntity> entityList = repository.findByAccountId(savedEntity.getAccountId());

        assertThat(entityList, hasSize(1));
        assertEqualsExpense(savedEntity, entityList.get(0));
    }

    @Test
    @Disabled
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            ExpenseEntity entity = new ExpenseEntity(1, 2, LocalDateTime.now(),
                    11.0, new Category("c", true), "desc",
                    PaymentMode.CASH, Optional.empty());
            repository.save(entity);
        });
    }

    @Test
    @Disabled
    void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        ExpenseEntity entity1 = repository.findById(savedEntity.getId()).get();
        ExpenseEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setDescription("desc1");
        repository.save(entity1);

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setDescription("desc2");
            repository.save(entity2);
        });

        // Get the updated entity from the database and verify its new state
        ExpenseEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("desc1", updatedEntity.getDescription());
    }

    private void assertEqualsExpense(ExpenseEntity expectedEntity, ExpenseEntity actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getAccountId(), actualEntity.getAccountId());
        assertEquals(expectedEntity.getExpenseId(), actualEntity.getExpenseId());
        assertEquals(expectedEntity.getAmount(), actualEntity.getAmount());
        assertEquals(expectedEntity.getPaymentMode(), actualEntity.getPaymentMode());
        assertEquals(expectedEntity.getDescription(), actualEntity.getDescription());
    }
}
