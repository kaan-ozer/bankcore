package com.bankcore.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(

        @NotBlank(message = "IBAN cannot be blank") @Size(min = 15, max = 34, message = "Iban length must be between 15 and 34") String iban,

        @NotBlank(message = "Currency cannot be blank") String currency) {
}
