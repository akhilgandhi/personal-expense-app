package com.akhil.microservices.core.expense.persistence;

import com.akhil.microservices.api.core.expense.Category;
import com.akhil.microservices.api.core.expense.PaymentMode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(
        collection = "expenses"
)
@CompoundIndex(
        name = "acc-exp-idx",
        unique = true,
        def = "{'accountId': 1, 'expenses': 1}"
)
public class ExpenseEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    private int accountId;
    private int expenseId;
    private LocalDateTime transactionDateTime;
    private double amount;
    private Category category;
    private String description;
    private PaymentMode paymentMode;
    private String notes;

    public ExpenseEntity() {
        accountId = 0;
        expenseId = 0;
        transactionDateTime = LocalDateTime.now();
        amount = 0.0;
        category = null;
        description = null;
        paymentMode = PaymentMode.CASH;
        notes = null;
    }

    public ExpenseEntity(int accountId, int expenseId, LocalDateTime transactionDateTime,
                         double amount, Category category, String description,
                         PaymentMode paymentMode, String notes) {
        this.accountId = accountId;
        this.expenseId = expenseId;
        this.transactionDateTime = transactionDateTime;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.paymentMode = paymentMode;
        this.notes = (notes == null || notes.isEmpty()) ? "" : notes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
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
