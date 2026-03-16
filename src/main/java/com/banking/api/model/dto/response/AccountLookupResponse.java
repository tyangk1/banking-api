package com.banking.api.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account lookup response with masked owner name")
public class AccountLookupResponse {

    @Schema(description = "Account number", example = "5379585164")
    private String accountNumber;

    @Schema(description = "Masked owner name", example = "BOB B***")
    private String ownerName;

    @Schema(description = "Account type", example = "CHECKING")
    private String accountType;

    @Schema(description = "Whether the account is active", example = "true")
    private boolean active;
}
