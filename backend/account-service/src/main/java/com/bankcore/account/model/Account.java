package com.bankcore.account.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Account {
    private UUID id;
    private String iban;
    private BigDecimal balance;
    private String currency;
}