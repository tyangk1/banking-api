package com.banking.api.service;

import com.banking.api.exception.BadRequestException;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.messaging.EventPublisher;
import com.banking.api.model.dto.request.CreateAccountRequest;
import com.banking.api.model.dto.response.AccountResponse;
import com.banking.api.model.entity.Account;
import com.banking.api.model.entity.User;
import com.banking.api.model.enums.AccountStatus;
import com.banking.api.model.enums.AccountType;
import com.banking.api.repository.AccountRepository;
import com.banking.api.repository.UserRepository;
import com.banking.api.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private AccountServiceImpl accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@email.com")
                .build();
        testUser.setId("user-1");

        testAccount = Account.builder()
                .accountNumber("1000000001")
                .accountName("My Savings")
                .accountType(AccountType.SAVINGS)
                .balance(new BigDecimal("10000000"))
                .currency("VND")
                .status(AccountStatus.ACTIVE)
                .user(testUser)
                .build();
        testAccount.setId("acc-1");
        testAccount.setCreatedAt(LocalDateTime.now());
        testAccount.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("createAccount()")
    class CreateAccount {
        @Test
        @DisplayName("should create account successfully")
        void shouldCreateAccount() {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountName("New Account")
                    .accountType(AccountType.CHECKING)
                    .currency("VND")
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
            when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
                Account a = inv.getArgument(0);
                a.setId("new-acc-id");
                return a;
            });

            AccountResponse response = accountService.createAccount(request, "user-1");

            assertThat(response).isNotNull();
            assertThat(response.getAccountName()).isEqualTo("New Account");
            assertThat(response.getAccountType()).isEqualTo(AccountType.CHECKING);
            assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            verify(accountRepository).save(any(Account.class));
            verify(eventPublisher).publishAccountCreated(any());
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            CreateAccountRequest request = CreateAccountRequest.builder()
                    .accountName("Test")
                    .accountType(AccountType.SAVINGS)
                    .currency("VND")
                    .build();

            when(userRepository.findById("invalid")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.createAccount(request, "invalid"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAccountById()")
    class GetAccountById {
        @Test
        @DisplayName("should return account when found")
        void shouldReturnAccount() {
            when(accountRepository.findById("acc-1")).thenReturn(Optional.of(testAccount));

            AccountResponse response = accountService.getAccountById("acc-1");

            assertThat(response.getId()).isEqualTo("acc-1");
            assertThat(response.getAccountNumber()).isEqualTo("1000000001");
            assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("10000000"));
        }

        @Test
        @DisplayName("should throw when account not found")
        void shouldThrowWhenNotFound() {
            when(accountRepository.findById("invalid")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> accountService.getAccountById("invalid"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAccountsByUserId()")
    class GetAccountsByUserId {
        @Test
        @DisplayName("should return user accounts")
        void shouldReturnUserAccounts() {
            when(accountRepository.findByUserId("user-1")).thenReturn(List.of(testAccount));

            List<AccountResponse> accounts = accountService.getAccountsByUserId("user-1");

            assertThat(accounts).hasSize(1);
            assertThat(accounts.get(0).getOwnerName()).isEqualTo(testUser.getFullName());
        }
    }

    @Nested
    @DisplayName("closeAccount()")
    class CloseAccount {
        @Test
        @DisplayName("should close account with zero balance")
        void shouldCloseAccount() {
            testAccount.setBalance(BigDecimal.ZERO);
            when(accountRepository.findById("acc-1")).thenReturn(Optional.of(testAccount));
            when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

            AccountResponse response = accountService.closeAccount("acc-1", "user-1");

            assertThat(response.getStatus()).isEqualTo(AccountStatus.CLOSED);
            verify(eventPublisher).publishAccountClosed(any());
        }

        @Test
        @DisplayName("should throw when balance is not zero")
        void shouldThrowWhenBalanceNotZero() {
            when(accountRepository.findById("acc-1")).thenReturn(Optional.of(testAccount));

            assertThatThrownBy(() -> accountService.closeAccount("acc-1", "user-1"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("zero");
        }

        @Test
        @DisplayName("should throw when not own account")
        void shouldThrowWhenNotOwner() {
            when(accountRepository.findById("acc-1")).thenReturn(Optional.of(testAccount));

            assertThatThrownBy(() -> accountService.closeAccount("acc-1", "other-user"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("own");
        }
    }
}
