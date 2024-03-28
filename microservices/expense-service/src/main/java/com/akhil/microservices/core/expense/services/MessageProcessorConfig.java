package com.akhil.microservices.core.expense.services;

import com.akhil.microservices.api.core.expense.Expense;
import com.akhil.microservices.api.core.expense.ExpenseService;
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

    private final ExpenseService expenseService;

    @Autowired
    public MessageProcessorConfig(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Bean
    public Consumer<Event<Integer, Expense>> messageProcessor() {

        return event -> {

            LOG.info("Process message created at {}...", event.getEventCreatedAt());

            switch (event.getType()) {
                case CREATE -> {
                    Expense expense = event.getData();
                    LOG.info("Create expense with ID: {}/{}", expense.getAccountId(), expense.getExpenseId());
                    expenseService.createExpense(expense).block();
                }

                case DELETE -> {
                    int accountId = event.getKey();
                    LOG.info("Delete expenses with AccountID: {}", accountId);
                    expenseService.deleteExpenses(accountId).block();
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
