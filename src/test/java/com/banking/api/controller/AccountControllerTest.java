package com.banking.api.controller;

import com.banking.api.model.dto.response.AccountResponse;
import com.banking.api.model.enums.AccountStatus;
import com.banking.api.model.enums.AccountType;
import com.banking.api.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AccountController Integration Tests")
class AccountControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private AccountService accountService;

    private final AccountResponse sampleResponse = AccountResponse.builder()
            .id("acc-1")
            .accountNumber("1000000001")
            .accountName("Test Account")
            .accountType(AccountType.SAVINGS)
            .balance(new BigDecimal("10000000"))
            .currency("VND")
            .status(AccountStatus.ACTIVE)
            .ownerName("Test User")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    @Test
    @DisplayName("GET /v1/accounts - should return 401 without auth")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/v1/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@email.com")
    @DisplayName("GET /v1/accounts - should return accounts with auth")
    void shouldReturnAccountsWithAuth() throws Exception {
        when(accountService.getAccountsByUserId(any())).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/v1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /v1/auth/login - should be accessible without auth")
    void loginEndpointShouldBePublic() throws Exception {
        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@email.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /v1/health - should return 200")
    void healthEndpointShouldBePublic() throws Exception {
        mockMvc.perform(get("/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }
}
