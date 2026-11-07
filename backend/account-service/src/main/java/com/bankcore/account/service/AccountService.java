package com.bankcore.account.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import com.bankcore.account.entity.AccountEntity;
import com.bankcore.account.model.Account;
import com.bankcore.account.repository.AccountRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account createAccount(String iban, String currency) {
        Account account = new Account(iban, currency);

        AccountEntity entity = new AccountEntity(
                account.getId(),
                account.getIban(),
                account.getBalance(),
                account.getCurrency());

        accountRepository.save(entity);

        return account;
    }

    public void deposit(Account account, BigDecimal amount) {
        account.deposit(amount);
    }
}
