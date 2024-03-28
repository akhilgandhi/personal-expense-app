package com.akhil.microservices.api.composite.dashboard;

import com.akhil.microservices.api.core.expense.Category;
import com.akhil.microservices.api.core.expense.PaymentMode;

import java.time.LocalDateTime;

public class ExpenseSummary {

    private int expenseId;
    private LocalDateTime transactionDateTime;
    private double amount;
    private Category category;
    private String description;
    private PaymentMode paymentMode;
    private String notes;

    public ExpenseSummary() {
        expenseId = 0;
        transactionDateTime = LocalDateTime.now();
        amount = 0.0;
        category = null;
        description = null;
        paymentMode = PaymentMode.CASH;
        notes = null;
    }

    public ExpenseSummary(int expenseId, LocalDateTime transactionDateTime,
                          double amount, Category category, String description, PaymentMode paymentMode,
                          String notes) {
        this.expenseId = expenseId;
        this.transactionDateTime = transactionDateTime;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.paymentMode = paymentMode;
        this.notes = (notes == null || notes.isEmpty()) ? "" : notes;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime;
    }

    public void setTransactionDateTime(LocalDateTime transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
