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

    public void deposit(BigDecimal amount) {
        if(amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public BigDecimal getBalance() {
        return balance;
    }
}