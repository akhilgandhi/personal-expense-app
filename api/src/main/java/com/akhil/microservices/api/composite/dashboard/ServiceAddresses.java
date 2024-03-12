package com.akhil.microservices.api.composite.dashboard;

public class ServiceAddresses {

    private final String dash;
    private final String expense;

    public ServiceAddresses() {
        dash = null;
        expense = null;
    }

    public ServiceAddresses(String dash, String expense) {
        this.dash = dash;
        this.expense = expense;
    }

    public String getDash() {
        return dash;
    }

    public String getExpense() {
        return expense;
    }
}
