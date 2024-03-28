package com.akhil.microservices.core.account.services;

import com.akhil.microservices.api.core.account.Account;
import com.akhil.microservices.api.core.account.AccountService;
import com.akhil.microservices.api.event.Event;
import com.akhil.microservices.api.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class MessageProcessorConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final AccountService accountService;

    @Autowired
    public MessageProcessorConfig(AccountService accountService) {
        this.accountService = accountService;
    }

    @Bean
    public Consumer<Event<Integer, Account>> messageProcessor() {

        return event -> {

            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getType()) {
                case CREATE -> {
                    Account account = event.getData();
                    LOG.info("Create account with ID: {}", account.getAccountId());
                    accountService.createAccount(account).block();
                }

                case DELETE -> {
                    int accountId = event.getKey();
                    LOG.info("Delete account with AccountID: {}", accountId);
                    accountService.deleteAccount(accountId).block();
                }

                default -> {
                    String errorMessage = "Incorrect event type: " + event.getType() +
                            ", expected a CREATE or DELETE event";
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }

            LOG.info("Message processing done!");
        };
    }
}
