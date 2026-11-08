package com.bankcore.account.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
    private UUID id;
    private String iban;
    private BigDecimal balance;
    private String currency;

    public Account(String iban, String currency) {
        this.id = UUID.randomUUID();
        this.iban = iban;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
    }

    public Account(
            UUID id,
            String iban,
            BigDecimal balance,
            String currency) {
        this.id = id;
        this.iban = iban;
        this.balance = balance;
        this.currency = currency;
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Withdrawal amount must be positive");
        }

        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance");
        }

        this.balance = this.balance.subtract(amount);
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }
}