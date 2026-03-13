package com.banking.api.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new beneficiary")
public class CreateBeneficiaryRequest {

    @NotBlank(message = "Nickname is required")
    @Size(max = 100, message = "Nickname must not exceed 100 characters")
    @Schema(description = "Display nickname for the beneficiary", example = "Chủ nhà")
    private String nickname;

    @NotBlank(message = "Account number is required")
    @Size(max = 20, message = "Account number must not exceed 20 characters")
    @Schema(description = "Beneficiary account number", example = "1000000010")
    private String accountNumber;

    @NotBlank(message = "Account holder name is required")
    @Size(max = 200, message = "Account holder name must not exceed 200 characters")
    @Schema(description = "Full name of the account holder", example = "Hoang Van Duc")
    private String accountHolderName;

    @Schema(description = "Bank name (default: Premium Banking)", example = "Premium Banking")
    @Builder.Default
    private String bankName = "Premium Banking";
}
