package com.akhil.microservices.core.account.services;

import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.account.AccountService;
import com.akhil.microservices.api.exceptions.InvalidInputException;
import com.akhil.microservices.api.exceptions.NotFoundException;
import com.akhil.microservices.core.account.persistence.AccountEntity;
import com.akhil.microservices.core.account.persistence.AccountRepository;
import com.akhil.microservices.util.http.ServiceUtil;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@RestController
public class AccountServiceImpl implements AccountService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository repository;
    private final AccountMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public AccountServiceImpl(AccountRepository repository,
                              AccountMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Mono<Account> getAccount(int accountId) {

        LOG.debug("/account return the found account for accountId={}", accountId);

        if (accountId < 1) {
            throw new InvalidInputException("Invalid accountId: " + accountId);
        }

        return repository.findByAccountId(accountId)
                .switchIfEmpty(Mono.error(new NotFoundException("No account found for accountId: " + accountId)))
                .log(LOG.getName(), Level.FINE)
                .map(mapper::entityToApi)
                .map(this::setServiceAddress);
    }

    @Override
    public Mono<Account> createAccount(Account body) {

        if (body.getAccountId() < 1) {
            throw new InvalidInputException("Invalid accountId: " + body.getAccountId());
        }

        AccountEntity entity = mapper.apiToEntity(body);
        Mono<Account> newEntity = repository.save(entity)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate Key, Account Id: " + body.getAccountId())
                )
                .map(mapper::entityToApi);

        return newEntity;
    }

    @Override
    public Mono<Void> deleteAccount(int accountId) {

        if (accountId < 1) {
            throw new InvalidInputException("Invalid accountId: " + accountId);
        }

        LOG.debug("deleteProduct: tries to delete an entity with accountId: {}", accountId);
        return repository.findByAccountId(accountId)
                .log(LOG.getName(), Level.FINE)
                .map(repository::delete)
                .flatMap(e -> e);
    }

    private Account setServiceAddress(Account e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
