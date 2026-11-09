package com.bankcore.account.repository;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.bankcore.account.entity.AccountEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;

@SpringBootTest
@ActiveProfiles("test")
public class AccountOptimisticLockingTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private UUID createdAccountId;

    @Test
    void shouldRejectStaleAccountUpdate() {
        String iban = "TR" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 24);

        createdAccountId = UUID.randomUUID();

        AccountEntity entity = new AccountEntity(
                createdAccountId,
                iban,
                BigDecimal.ZERO,
                "TRY");

        accountRepository.saveAndFlush(entity);

        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();

        try {

            em1.getTransaction().begin();
            em2.getTransaction().begin();

            AccountEntity account1 = em1.find(AccountEntity.class, createdAccountId);
            AccountEntity account2 = em2.find(AccountEntity.class, createdAccountId);

            account1.setBalance(new BigDecimal("500"));
            em1.getTransaction().commit();

            // İkinci işlem eski version ile güncellemeye çalışıyor
            account2.setBalance(new BigDecimal("700"));

            RollbackException exception = assertThrows(
                    RollbackException.class,
                    () -> em2.getTransaction().commit());

            assertInstanceOf(
                    OptimisticLockException.class,
                    exception.getCause());

        } finally {
            if (em1.getTransaction().isActive()) {
                em1.getTransaction().rollback();
            }

            if (em2.getTransaction().isActive()) {
                em2.getTransaction().rollback();
            }

            em1.close();
            em2.close();
        }
    }

    @AfterEach
    void cleanup() {
        if (createdAccountId != null) {
            accountRepository.deleteById(createdAccountId);
        }
    }
}
