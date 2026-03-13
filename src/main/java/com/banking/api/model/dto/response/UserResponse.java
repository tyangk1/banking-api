package com.banking.api.model.dto.response;

import com.banking.api.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User response")
public class UserResponse {

    @Schema(description = "User ID", example = "uuid-string")
    private String id;

    @Schema(description = "First name", example = "Nguyen")
    private String firstName;

    @Schema(description = "Last name", example = "Van A")
    private String lastName;

    @Schema(description = "Full name", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Email", example = "nguyenvana@email.com")
    private String email;

    @Schema(description = "Phone number", example = "0901234567")
    private String phoneNumber;

    @Schema(description = "User role")
    private Role role;

    @Schema(description = "Avatar URL")
    private String avatarUrl;

    @Schema(description = "Home address")
    private String address;

    @Schema(description = "Date of birth")
    private LocalDate dateOfBirth;

    @Schema(description = "KYC status: PENDING, VERIFIED, REJECTED")
    private String kycStatus;

    @Schema(description = "Account enabled")
    private boolean enabled;

    @Schema(description = "Number of bank accounts")
    private int accountCount;

    @Schema(description = "Account creation time")
    private LocalDateTime createdAt;
}
