package com.bankcore.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.bankcore.account.entity.AccountEntity;
import com.bankcore.account.model.Account;
import com.bankcore.account.repository.AccountRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AccountServiceIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistCreatedAccount() {

        Account account = accountService.createAccount(
                "TR123456789012345678901234",
                "TRY");

        entityManager.flush();
        entityManager.clear();

        AccountEntity entity = accountRepository
                .findById(account.getId())
                .orElseThrow();

        assertEquals(account.getId(), entity.getId());
        assertEquals(account.getIban(), entity.getIban());
        assertEquals(account.getCurrency(), entity.getCurrency());
        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(entity.getBalance()));
    }

    @Test
    void shouldPersistDepositToDatabase() {

        Account account = accountService.createAccount(
                "TR123456789012345678901234",
                "TRY");

        accountService.deposit(
                account.getId(),
                new BigDecimal("500"));

        entityManager.flush();
        entityManager.clear();

        AccountEntity entity = accountRepository
                .findById(account.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("500").compareTo(entity.getBalance()));
    }

    @Test
    void shouldPersistWithdrawalToDatabase() {

        Account account = accountService.createAccount(
                "TR123456789012345678901234",
                "TRY");

        accountService.deposit(account.getId(), new BigDecimal("1000"));

        accountService.withdraw(account.getId(), new BigDecimal("300"));

        entityManager.flush();

        entityManager.clear();

        AccountEntity entity = accountRepository.findById(account.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("700").compareTo(entity.getBalance()));

    }

    @Test
    void shouldRejectWithdrawalWhenBalanceIsInsufficient() {
        Account account = accountService.createAccount(
                "TR123456789012345678901234",
                "TRY");

        accountService.deposit(account.getId(), new BigDecimal("500"));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.withdraw(account.getId(), new BigDecimal("800")));

        entityManager.flush();
        entityManager.clear();

        AccountEntity entity = accountRepository.findById(account.getId()).orElseThrow();// Boşsa otomatik olarak
                                                                                         // NoSuchElementException
                                                                                         // fırlatır.

        assertEquals(0, new BigDecimal("500").compareTo(entity.getBalance()));
    }
}
