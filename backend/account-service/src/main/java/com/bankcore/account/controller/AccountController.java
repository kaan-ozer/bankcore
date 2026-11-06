package com.bankcore.account.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bankcore.account.dto.AccountResponse;
import com.bankcore.account.model.Account;
import com.bankcore.account.service.AccountService;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public AccountResponse createAccount(@RequestParam String iban, @RequestParam String currency) {
        Account account = accountService.createAccount(iban, currency);

        return new AccountResponse(
                account.getId(),
                account.getIban(),
                account.getBalance(),
                account.getCurrency());
    }

}
