
package com.bankcore.account.service;

import com.bankcore.account.entity.AccountEntity;
import com.bankcore.account.repository.AccountRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

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

                                        // Bakiyeyi değiştiriyoruz.
                                        lockedAccount.setBalance(new BigDecimal("700"));

                                        // SQL'i veritabanına gönderiyoruz.
                                        accountRepository.flush();

                                        // Bilerek hata oluşturuyoruz.
                                        throw new IllegalStateException("Simulated failure");
                                });
                        });

                        // Transaction bittikten sonra yeniden okuyoruz.
                        AccountEntity updatedAccount = accountRepository.findById(accountId).orElseThrow();

                        assertEquals(
                                        0,
                                        new BigDecimal("1000")
                                                        .compareTo(updatedAccount.getBalance()));

                } finally {
                        accountRepository.deleteById(accountId);
                }
        }

}
