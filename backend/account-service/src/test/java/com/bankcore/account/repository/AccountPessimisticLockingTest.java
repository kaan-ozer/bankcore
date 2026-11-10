
package com.bankcore.account.repository;

import com.bankcore.account.entity.AccountEntity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AccountPessimisticLockingTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldBlockSecondTransactionUntilFirstCommits()
            throws Exception {

        UUID accountId = UUID.randomUUID();

        String iban = "TR" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 24);

        AccountEntity entity = new AccountEntity(
                accountId,
                iban,
                new BigDecimal("1000"),
                "TRY");

        accountRepository.saveAndFlush(entity);

        CountDownLatch lockAcquired = new CountDownLatch(1);

        CountDownLatch releaseLock = new CountDownLatch(1);

        CountDownLatch secondStarted = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        try {

            Future<?> first = executor.submit(() -> {

                transactionTemplate.executeWithoutResult(
                        status -> {

                            accountRepository
                                    .findByIdForUpdate(accountId)
                                    .orElseThrow();

                            lockAcquired.countDown();

                            try {
                                if (!releaseLock.await(
                                        10, TimeUnit.SECONDS)) {

                                    throw new IllegalStateException(
                                            "A bekleme süresi doldu");
                                }

                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            }
                        });
            });

            assertTrue(
                    lockAcquired.await(5, TimeUnit.SECONDS));

            Future<?> second = executor.submit(() -> {

                transactionTemplate.executeWithoutResult(
                        status -> {

                            secondStarted.countDown();

                            accountRepository
                                    .findByIdForUpdate(accountId)
                                    .orElseThrow();
                        });
            });

            assertTrue(
                    secondStarted.await(5, TimeUnit.SECONDS));

            assertThrows(
                    TimeoutException.class,
                    () -> second.get(
                            300, TimeUnit.MILLISECONDS));

            releaseLock.countDown();

            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);

        } finally {

            releaseLock.countDown();

            executor.shutdownNow();

            if (executor.awaitTermination(
                    5, TimeUnit.SECONDS)) {

                accountRepository.deleteById(accountId);
            }
        }
    }
}
