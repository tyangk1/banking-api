package com.banking.api.service;

import com.banking.api.exception.BadRequestException;
import com.banking.api.exception.InsufficientBalanceException;
import com.banking.api.exception.ResourceNotFoundException;
import com.banking.api.messaging.EventPublisher;
import com.banking.api.model.dto.request.DepositRequest;
import com.banking.api.model.dto.request.TransferRequest;
import com.banking.api.model.dto.response.TransactionResponse;
import com.banking.api.model.entity.Account;
import com.banking.api.model.entity.Transaction;
import com.banking.api.model.entity.User;
import com.banking.api.model.enums.AccountStatus;
import com.banking.api.model.enums.AccountType;
import com.banking.api.model.enums.TransactionStatus;
import com.banking.api.model.enums.TransactionType;
import com.banking.api.repository.AccountRepository;
import com.banking.api.repository.TransactionRepository;
import com.banking.api.service.impl.TransactionServiceImpl;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private TransactionServiceImpl transactionService;

    private User sourceUser, destUser;
    private Account sourceAccount, destAccount;

    @BeforeEach
    void setUp() {
        sourceUser = User.builder().firstName("Nguyen").lastName("Van A").email("a@email.com").build();
        sourceUser.setId("user-1");
        destUser = User.builder().firstName("Tran").lastName("Van B").email("b@email.com").build();
        destUser.setId("user-2");

        sourceAccount = Account.builder()
                .accountNumber("1000000001").accountName("Source")
                .accountType(AccountType.CHECKING).balance(new BigDecimal("5000000"))
                .currency("VND").status(AccountStatus.ACTIVE).user(sourceUser)
                .build();
        sourceAccount.setId("acc-1");
        sourceAccount.setCreatedAt(LocalDateTime.now());
        sourceAccount.setUpdatedAt(LocalDateTime.now());

        destAccount = Account.builder()
                .accountNumber("1000000002").accountName("Dest")
                .accountType(AccountType.CHECKING).balance(new BigDecimal("2000000"))
                .currency("VND").status(AccountStatus.ACTIVE).user(destUser)
                .build();
        destAccount.setId("acc-2");
        destAccount.setCreatedAt(LocalDateTime.now());
        destAccount.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("transfer()")
    class Transfer {
        @Test
        @DisplayName("should transfer successfully")
        void shouldTransfer() {
            TransferRequest request = TransferRequest.builder()
                    .sourceAccountNumber("1000000001")
                    .destinationAccountNumber("1000000002")
                    .amount(new BigDecimal("1000000"))
                    .description("Test transfer")
                    .build();

            when(accountRepository.findByAccountNumber("1000000001")).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findByAccountNumber("1000000002")).thenReturn(Optional.of(destAccount));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
                Transaction t = inv.getArgument(0);
                t.setId("tx-1");
                t.setCreatedAt(LocalDateTime.now());
                return t;
            });

            TransactionResponse response = transactionService.transfer(request, "user-1");

            assertThat(response).isNotNull();
            assertThat(response.getType()).isEqualTo(TransactionType.TRANSFER);
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("1000000"));
            assertThat(sourceAccount.getBalance()).isEqualByComparingTo(new BigDecimal("4000000"));
            assertThat(destAccount.getBalance()).isEqualByComparingTo(new BigDecimal("3000000"));
            verify(eventPublisher).publishTransfer(any());
        }

        @Test
        @DisplayName("should throw when same account")
        void shouldThrowWhenSameAccount() {
            TransferRequest request = TransferRequest.builder()
                    .sourceAccountNumber("1000000001")
                    .destinationAccountNumber("1000000001")
                    .amount(new BigDecimal("1000000"))
                    .build();

            assertThatThrownBy(() -> transactionService.transfer(request, "user-1"))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("should throw when insufficient balance")
        void shouldThrowWhenInsufficientBalance() {
            TransferRequest request = TransferRequest.builder()
                    .sourceAccountNumber("1000000001")
                    .destinationAccountNumber("1000000002")
                    .amount(new BigDecimal("99999999"))
                    .build();

            when(accountRepository.findByAccountNumber("1000000001")).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findByAccountNumber("1000000002")).thenReturn(Optional.of(destAccount));

            assertThatThrownBy(() -> transactionService.transfer(request, "user-1"))
                    .isInstanceOf(InsufficientBalanceException.class);
        }

        @Test
        @DisplayName("should throw when not owner")
        void shouldThrowWhenNotOwner() {
            TransferRequest request = TransferRequest.builder()
                    .sourceAccountNumber("1000000001")
                    .destinationAccountNumber("1000000002")
                    .amount(new BigDecimal("100"))
                    .build();

            when(accountRepository.findByAccountNumber("1000000001")).thenReturn(Optional.of(sourceAccount));
            when(accountRepository.findByAccountNumber("1000000002")).thenReturn(Optional.of(destAccount));

            assertThatThrownBy(() -> transactionService.transfer(request, "user-999"))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    @DisplayName("deposit()")
    class Deposit {
        @Test
        @DisplayName("should deposit successfully")
        void shouldDeposit() {
            DepositRequest request = DepositRequest.builder()
                    .accountNumber("1000000002")
                    .amount(new BigDecimal("3000000"))
                    .description("ATM deposit")
                    .build();

            when(accountRepository.findByAccountNumber("1000000002")).thenReturn(Optional.of(destAccount));
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
                Transaction t = inv.getArgument(0);
                t.setId("tx-2");
                t.setCreatedAt(LocalDateTime.now());
                return t;
            });

            TransactionResponse response = transactionService.deposit(request);

            assertThat(response.getType()).isEqualTo(TransactionType.DEPOSIT);
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("3000000"));
            assertThat(destAccount.getBalance()).isEqualByComparingTo(new BigDecimal("5000000"));
            verify(eventPublisher).publishDeposit(any());
        }

        @Test
        @DisplayName("should throw when account not found")
        void shouldThrowWhenAccountNotFound() {
            DepositRequest request = DepositRequest.builder()
                    .accountNumber("9999999999")
                    .amount(new BigDecimal("1000"))
                    .build();

            when(accountRepository.findByAccountNumber("9999999999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.deposit(request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
