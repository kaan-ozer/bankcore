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

        AccountEntity entity = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        Account account = toDomain(entity);

        account.deposit(amount);

        entity.setBalance(account.getBalance());
    }

    @Transactional
    public void withdraw(UUID accountId, BigDecimal amount) {

        AccountEntity entity = accountRepository.findByIdForUpdate(accountId).orElseThrow();

        Account account = toDomain(entity);

        account.withdraw(amount);

        entity.setBalance(account.getBalance());
    }

    @Transactional
    public void transfer(UUID fromId, UUID toId, BigDecimal amount) {

        if (fromId.equals(toId)) {
            throw new IllegalArgumentException(
                    "Cannot transfer to the same account");
        }

        // Deadlock riskini azaltmak için sabit kilit sırası
        UUID firstId = fromId.compareTo(toId) < 0 ? fromId : toId;
        UUID secondId = fromId.compareTo(toId) < 0 ? toId : fromId;

        AccountEntity first = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        AccountEntity second = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        AccountEntity sender = fromId.equals(firstId) ? first : second;
        AccountEntity receiver = toId.equals(firstId) ? first : second;

        Account senderAccount = toDomain(sender);
        Account receiverAccount = toDomain(receiver);

        senderAccount.withdraw(amount);
        receiverAccount.deposit(amount);

        sender.setBalance(senderAccount.getBalance());
        receiver.setBalance(receiverAccount.getBalance());
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
