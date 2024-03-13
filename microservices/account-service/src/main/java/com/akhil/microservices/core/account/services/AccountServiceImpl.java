package com.akhil.microservices.core.account.services;

import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.account.AccountService;
import com.akhil.microservices.api.exceptions.InvalidInputException;
import com.akhil.microservices.api.exceptions.NotFoundException;
import com.akhil.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountServiceImpl implements AccountService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountServiceImpl.class);

    private ServiceUtil serviceUtil;

    @Autowired
    public AccountServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }


    @Override
    public Account getAccount(int accountId) {

        LOG.debug("/account return the found account for accountId={}", accountId);

        if (accountId < 1) {
            throw new InvalidInputException("Invalid accountId: " + accountId);
        }

        if (accountId == 13) {
            throw new NotFoundException("No account found for accountId: " + accountId);
        }

        return new Account(accountId, "Account " + accountId, serviceUtil.getServiceAddress());
    }
}
