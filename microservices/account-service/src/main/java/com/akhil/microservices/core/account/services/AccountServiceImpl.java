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
    public Account getAccount(int accountId) {

        LOG.debug("/account return the found account for accountId={}", accountId);

        if (accountId < 1) {
            throw new InvalidInputException("Invalid accountId: " + accountId);
        }

        AccountEntity entity = repository.findByAccountId(accountId)
                .orElseThrow(() -> new NotFoundException("No account found for accountId: " + accountId));

        Account response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());
        return response;
    }

    @Override
    public Account createAccount(Account body) {
        try {
            AccountEntity entity = mapper.apiToEntity(body);
            AccountEntity newEntity =  repository.save(entity);
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Account Id: " + body.getAccountId());
        }
    }

    @Override
    public void deleteAccount(int accountId) {
        repository.findByAccountId(accountId).ifPresent(repository::delete);
    }
}
