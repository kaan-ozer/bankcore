package com.bankcore.account.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import com.bankcore.account.entity.AccountEntity;

import jakarta.persistence.EntityManager;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndFindAccount() {

        UUID accountId = UUID.randomUUID();

        AccountEntity entity = new AccountEntity(
                accountId,
                "TR123456789012345678901234",
                BigDecimal.ZERO,
                "TRY");
        AccountEntity savedEntity = accountRepository.save(entity);

        entityManager.flush();

        entityManager.clear();

        Optional<AccountEntity> foundEntity = accountRepository.findById(savedEntity.getId());

        assertTrue(foundEntity.isPresent());

        AccountEntity found = foundEntity.orElseThrow();

        assertEquals(entity.getId(), found.getId());
        assertEquals(entity.getIban(), found.getIban());
        assertEquals(0, entity.getBalance().compareTo(found.getBalance()));
        assertEquals(entity.getCurrency(), found.getCurrency());
    }
}
