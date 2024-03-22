package com.akhil.microservices.core.account.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface AccountRepository extends
        PagingAndSortingRepository<AccountEntity, String>,
        CrudRepository<AccountEntity, String> {

    Optional<AccountEntity> findByAccountId(int accountId);
}
