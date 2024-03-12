package com.akhil.microservices.api.core.account;

public class Account {

    private int accountId;
    private String name;
    private String serviceAddress;

    public Account() {
        accountId = 0;
        name = null;
        serviceAddress = null;
    }

    public Account(int accountId, String name, String serviceAddress) {
        this.accountId = accountId;
        this.name = name;
        this.serviceAddress = serviceAddress;
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

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
