package com.akhil.microservices.api.composite.dashboard;

public class AccountSummary {

    private int accountId;
    private String name;

    public AccountSummary() {
        accountId = 0;
        name = null;
    }

    public AccountSummary(int accountId, String name) {
        this.accountId = accountId;
        this.name = name;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
