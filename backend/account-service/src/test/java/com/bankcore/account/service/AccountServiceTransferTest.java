
package com.bankcore.account.service;

import com.bankcore.account.entity.AccountEntity;
import com.bankcore.account.repository.AccountRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceTransferTest {

        @Autowired
        private AccountService accountService;

        @Autowired
        private AccountRepository accountRepository;

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
}
