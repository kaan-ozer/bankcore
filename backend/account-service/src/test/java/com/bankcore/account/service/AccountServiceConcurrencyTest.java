package com.bankcore.account.service;

import com.bankcore.account.entity.AccountEntity;
import com.bankcore.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceConcurrencyTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldDepositMoneySuccessfully() {

        UUID accountId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId,
                "TR" + UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 24),
                new BigDecimal("1000"),
                "TRY");

        accountRepository.saveAndFlush(account);

        try {
            accountService.deposit(accountId, new BigDecimal("500"));

            AccountEntity updated = accountRepository.findById(accountId)
                    .orElseThrow();

            assertEquals(
                    0,
                    new BigDecimal("1500").compareTo(updated.getBalance()));
        } finally {
            accountRepository.deleteById(accountId);
        }
    }

    @Test
    void shouldHandleConcurrentDeposits() throws Exception {

        UUID accountId = UUID.randomUUID();

        AccountEntity account = new AccountEntity(
                accountId,
                "TR" + UUID.randomUUID().toString()
                        .replace("-", "").substring(0, 24),
                new BigDecimal("1000"),
                "TRY");

        accountRepository.saveAndFlush(account);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Future<?> first = executor.submit(() -> {
                ready.countDown();
                start.await();
                accountService.deposit(accountId, new BigDecimal("500"));
                return null;
            });

            Future<?> second = executor.submit(() -> {
                ready.countDown();
                start.await();
                accountService.deposit(accountId, new BigDecimal("200"));
                return null;
            });

            assertEquals(true, ready.await(5, TimeUnit.SECONDS));

            start.countDown();

            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);

            AccountEntity updated = accountRepository.findById(accountId)
                    .orElseThrow();

            assertEquals(
                    0,
                    new BigDecimal("1700").compareTo(updated.getBalance()));

        } finally {
            start.countDown();
            executor.shutdownNow();
            if (executor.awaitTermination(10, TimeUnit.SECONDS)) {
                accountRepository.deleteById(accountId);
            }
        }
    }
}