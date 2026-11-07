package com.bankcore.account.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bankcore.account.model.Account;
import com.bankcore.account.service.AccountService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void shouldRejectBlankIban() throws Exception {
        mockMvc.perform(
                post("/api/v1/accounts").contentType("application/json").content("""
                        {
                            "iban":"",
                            "currency":"TRY"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        [
                            "IBAN cannot be blank",
                            "Iban length must be between 15 and 34"
                        ]
                        """));

    }

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {
        Account account = new Account("TR123456789012345678901234",
                "TRY");

        when(accountService.createAccount("TR123456789012345678901234",
                "TRY")).thenReturn(account);

        mockMvc.perform(post("/api/v1/accounts")
                .contentType("application/json").content("""
                                    {
                                  "iban": "TR123456789012345678901234",
                                  "currency": "TRY"
                                }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.iban").value("TR123456789012345678901234"))
                .andExpect(jsonPath("$.currency").value("TRY"))
                .andExpect(jsonPath("$.balance").value(0));

    }
}