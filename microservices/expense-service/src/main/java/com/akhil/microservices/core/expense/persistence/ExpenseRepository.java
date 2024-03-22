package com.akhil.microservices.core.expense.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ExpenseRepository extends CrudRepository<ExpenseEntity, String> {

    List<ExpenseEntity> findByAccountId(int accountId);
}
