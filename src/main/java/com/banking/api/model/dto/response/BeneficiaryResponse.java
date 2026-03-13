package com.banking.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Beneficiary response")
public class BeneficiaryResponse {

    @Schema(description = "Beneficiary ID")
    private String id;

    @Schema(description = "Display nickname", example = "Chủ nhà")
    private String nickname;

    @Schema(description = "Account number", example = "1000000010")
    private String accountNumber;

    @Schema(description = "Account holder name", example = "Hoang Van Duc")
    private String accountHolderName;

    @Schema(description = "Bank name", example = "Premium Banking")
    private String bankName;

    @Schema(description = "Whether the beneficiary is verified")
    private boolean verified;

    @Schema(description = "Whether marked as favorite")
    private boolean favorite;

    @Schema(description = "Number of transfers made to this beneficiary")
    private int transferCount;

    @Schema(description = "Last time a transfer was made to this beneficiary")
    private LocalDateTime lastUsedAt;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;
}
