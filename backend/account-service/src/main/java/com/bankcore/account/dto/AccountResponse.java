package com.bankcore.account.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(UUID id, String iban, BigDecimal balance, String currency) {
}