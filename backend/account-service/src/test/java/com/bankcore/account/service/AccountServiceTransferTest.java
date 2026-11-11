
package com.bankcore.account.service;

import com.bankcore.account.entity.AccountEntity;
import com.bankcore.account.repository.AccountRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTransferTest {

        @Autowired
        private AccountService accountService;

        @Autowired
        private AccountRepository accountRepository;

        @Autowired
        private TransactionTemplate transactionTemplate;

        @Test
        void shouldTransferMoneyBetweenAccounts() {

                UUID senderId = UUID.randomUUID();
                UUID receiverId = UUID.randomUUID();

                AccountEntity sender = new AccountEntity(
                                senderId,
                                "TR-SENDER",
                                new BigDecimal("1000"),
                                "TRY");

                AccountEntity receiver = new AccountEntity(
                                receiverId,
                                "TR-RECEIVER",
                                new BigDecimal("500"),
                                "TRY");

                accountRepository.saveAndFlush(sender);
                accountRepository.saveAndFlush(receiver);

                try {
                        accountService.transfer(
                                        senderId,
                                        receiverId,
                                        new BigDecimal("300"));

                        AccountEntity updatedSender = accountRepository
                                        .findById(senderId)
                                        .orElseThrow();

                        AccountEntity updatedReceiver = accountRepository
                                        .findById(receiverId)
                                        .orElseThrow();

                        assertEquals(
                                        0,
                                        new BigDecimal("700")
                                                        .compareTo(updatedSender.getBalance()));

                        assertEquals(
                                        0,
                                        new BigDecimal("800")
                                                        .compareTo(updatedReceiver.getBalance()));

                } finally {
                        accountRepository.deleteById(senderId);
                        accountRepository.deleteById(receiverId);
                }
        }

        @Test
        void shouldRejectTransferWhenBalanceIsInsufficient() {

                UUID senderId = UUID.randomUUID();
                UUID receiverId = UUID.randomUUID();

                AccountEntity sender = new AccountEntity(
                                senderId, "TR-SENDER-LOW",
                                new BigDecimal("100"), "TRY");

                AccountEntity receiver = new AccountEntity(
                                receiverId, "TR-RECEIVER-LOW",
                                new BigDecimal("500"), "TRY");

                accountRepository.saveAndFlush(sender);
                accountRepository.saveAndFlush(receiver);

                try {
                        assertThrows(
                                        IllegalArgumentException.class,
                                        () -> accountService.transfer(
                                                        senderId,
                                                        receiverId,
                                                        new BigDecimal("300")));

                        AccountEntity updatedSender = accountRepository.findById(senderId).orElseThrow();

                        AccountEntity updatedReceiver = accountRepository.findById(receiverId).orElseThrow();

                        assertEquals(
                                        0,
                                        new BigDecimal("100")
                                                        .compareTo(updatedSender.getBalance()));

                        assertEquals(
                                        0,
                                        new BigDecimal("500")
                                                        .compareTo(updatedReceiver.getBalance()));

                } finally {
                        accountRepository.deleteById(senderId);
                        accountRepository.deleteById(receiverId);
                }
        }

        @Test
        void shouldRollbackDatabaseChangesWhenTransactionFails() {

                UUID accountId = UUID.randomUUID();

                AccountEntity account = new AccountEntity(
                                accountId,
                                "TR-ROLLBACK-TEST",
                                new BigDecimal("1000"),
                                "TRY");

                accountRepository.saveAndFlush(account);

                try {
                        assertThrows(IllegalStateException.class, () -> {

                                transactionTemplate.executeWithoutResult(status -> {

                                        AccountEntity lockedAccount = accountRepository.findByIdForUpdate(accountId)
                                                        .orElseThrow();

                                        lockedAccount.setBalance(new BigDecimal("700"));

                                        accountRepository.flush();

                                        throw new IllegalStateException("Simulated failure");
                                });
                        });

                        AccountEntity updatedAccount = accountRepository.findById(accountId).orElseThrow();

                        assertEquals(
                                        0,
                                        new BigDecimal("1000")
                                                        .compareTo(updatedAccount.getBalance()));

                } finally {
                        accountRepository.deleteById(accountId);
                }
        }

        @Test
        void shouldTransferConcurrentlyWithoutDeadlock() throws Exception {

                UUID aId = UUID.randomUUID();
                UUID bId = UUID.randomUUID();

                accountRepository.saveAndFlush(new AccountEntity(
                                aId, "TR-CONCURRENT-A", new BigDecimal("1000"), "TRY"));

                accountRepository.saveAndFlush(new AccountEntity(
                                bId, "TR-CONCURRENT-B", new BigDecimal("1000"), "TRY"));

                ExecutorService executor = Executors.newFixedThreadPool(2);

                CountDownLatch start = new CountDownLatch(1);
                try {

                        Future<?> first = executor.submit(() -> {
                                start.await();
                                accountService.transfer(aId, bId, new BigDecimal("300"));
                                return null;
                        });

                        Future<?> second = executor.submit(() -> {
                                start.await();
                                accountService.transfer(bId, aId, new BigDecimal("200"));
                                return null;
                        });

                        start.countDown();

                        first.get(10, TimeUnit.SECONDS);
                        second.get(10, TimeUnit.SECONDS);

                        AccountEntity a = accountRepository.findById(aId).orElseThrow();
                        AccountEntity b = accountRepository.findById(bId).orElseThrow();

                        assertEquals(0, new BigDecimal("900").compareTo(a.getBalance()));
                        assertEquals(0, new BigDecimal("1100").compareTo(b.getBalance()));

                } finally {
                        executor.shutdownNow();
                        accountRepository.deleteById(aId);
                        accountRepository.deleteById(bId);
                }
        }

}
