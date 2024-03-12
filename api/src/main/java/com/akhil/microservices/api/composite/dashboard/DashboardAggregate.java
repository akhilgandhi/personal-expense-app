package com.akhil.microservices.api.composite.dashboard;

import java.util.List;

public class DashboardAggregate {

    private final int accountId;
    private final String name;
    private final List<ExpenseSummary> expenses;
    private final ServiceAddresses serviceAddresses;

    public DashboardAggregate(int accountId, String name,
                              List<ExpenseSummary> expenses,
                              ServiceAddresses serviceAddresses) {
        this.accountId = accountId;
        this.name = name;
        this.expenses = expenses;
        this.serviceAddresses = serviceAddresses;
    }

    public int getAccountId() {
        return accountId;
    }

    public List<ExpenseSummary> getExpenses() {
        return expenses;
    }

    public ServiceAddresses getServiceAddresses() {
        return serviceAddresses;
    }

    public String getName() {
        return name;
    }
}
