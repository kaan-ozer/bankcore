package com.bankcore.account.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.bankcore.account.model.Account;

@Service 
public class AccountService {
    public Account createAccount(String iban, String currency) {
        return new Account(iban, currency);
    }

    public void deposit(Account account, BigDecimal amount) {
        account.deposit(amount);
    }
}
