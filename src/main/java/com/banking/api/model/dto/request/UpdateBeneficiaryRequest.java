package com.banking.api.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update a beneficiary")
public class UpdateBeneficiaryRequest {

    @Size(max = 100, message = "Nickname must not exceed 100 characters")
    @Schema(description = "Updated nickname", example = "Landlord Monthly")
    private String nickname;

    @Schema(description = "Mark as favorite", example = "true")
    private Boolean favorite;
}
