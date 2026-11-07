package com.bankcore.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.bankcore.account.entity.AccountEntity;
import com.bankcore.account.model.Account;
import com.bankcore.account.repository.AccountRepository;

public class AccountServiceTest {

    @Test
    void shouldSaveAccountWhenCreated() {
        AccountRepository repository = mock(AccountRepository.class);
        AccountService accountService = new AccountService(repository);

        Account account = accountService.createAccount(
                "TR123456789012345678901234",
                "TRY");

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);

        verify(repository).save(captor.capture());

        AccountEntity savedEntity = captor.getValue();

        assertEquals("TRY", savedEntity.getCurrency());

        assertEquals(
                "TR123456789012345678901234",
                savedEntity.getIban());

        assertEquals(
                0,
                savedEntity.getBalance().compareTo(BigDecimal.ZERO));

        assertNotNull(savedEntity.getId());

        assertEquals(account.getId(), savedEntity.getId());
    }
}
