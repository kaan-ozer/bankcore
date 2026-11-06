package com.bankcore.account.model;
 
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AccountTest {
    @Test
    void shouldIncreaseBalanceWhenDepositIsValid() {
        Account account = new Account("TR123","TRY");
        account.deposit(new BigDecimal("100.00"));

        assertEquals(
            new BigDecimal("100.00"),
            account.getBalance()
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> account.deposit(new BigDecimal("-50.00"))
        );
    }

    @Test
    void shouldRejectNegativeDeposit() {
        Account account = new Account("TR123","TRY");
        assertThrows(
            IllegalArgumentException.class,
            () -> account.deposit(new BigDecimal("-50000"))
        );
        
    }
}

