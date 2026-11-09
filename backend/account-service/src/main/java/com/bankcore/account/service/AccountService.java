package com.bankcore.account.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Account getAccount(UUID accountId) {
        AccountEntity entity = findAccountEntity(accountId);

        return toDomain(entity);
    }

    @Transactional
    public void deposit(UUID accountId, BigDecimal amount) {

        AccountEntity entity = findAccountEntity(accountId);

        Account account = toDomain(entity);

        account.deposit(amount);

        entity.setBalance(amount);
    }

    @Transactional
    public void withdraw(UUID accountId, BigDecimal amount) {

        AccountEntity entity = accountRepository.findByIdForUpdate(accountId).orElseThrow();

        Account account = toDomain(entity);

        account.withdraw(amount);

        entity.setBalance(account.getBalance());
    }

    private AccountEntity findAccountEntity(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    private Account toDomain(AccountEntity entity) {
        return new Account(
                entity.getId(),
                entity.getIban(),
                entity.getBalance(),
                entity.getCurrency());
    }
}
