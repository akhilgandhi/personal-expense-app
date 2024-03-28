package com.akhil.microservices.core.account.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends
        ReactiveCrudRepository<AccountEntity, String> {

    Mono<AccountEntity> findByAccountId(int accountId);
}
