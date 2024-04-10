package com.akhil.microservices.api.composite.dashboard;

import java.util.List;

public class DashboardAggregate {

    private final AccountSummary account;
    private final List<ExpenseSummary> expenses;
    private final ServiceAddresses serviceAddresses;

    public DashboardAggregate() {
        account = null;
        expenses = null;
        serviceAddresses = null;
    }

    public DashboardAggregate(AccountSummary account,
                              List<ExpenseSummary> expenses,
                              ServiceAddresses serviceAddresses) {
        this.account = account;
        this.expenses = expenses;
        this.serviceAddresses = serviceAddresses;
    }

    public AccountSummary getAccount() {
        return account;
    }

    public List<ExpenseSummary> getExpenses() {
        return expenses;
    }

    public ServiceAddresses getServiceAddresses() {
        return serviceAddresses;
    }
}
