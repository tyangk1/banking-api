package com.banking.api.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update user profile")
public class UpdateProfileRequest {

    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Schema(description = "First name", example = "Le")
    private String firstName;

    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Schema(description = "Last name", example = "Minh Tuan")
    private String lastName;

    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    @Schema(description = "Phone number", example = "0912345678")
    private String phoneNumber;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Home address", example = "123 Nguyen Hue, Q1, TP.HCM")
    private String address;

    @Schema(description = "Date of birth", example = "1995-06-15")
    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    @Schema(description = "Avatar image URL")
    private String avatarUrl;
}
