package com.banking.api.model.dto.response;

import com.banking.api.model.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Notification response")
public class NotificationResponse {

    @Schema(description = "Notification ID")
    private String id;

    @Schema(description = "Notification type")
    private NotificationType type;

    @Schema(description = "Notification title")
    private String title;

    @Schema(description = "Notification message")
    private String message;

    @Schema(description = "Whether notification has been read")
    private boolean read;

    @Schema(description = "Reference ID (e.g. transaction ref)")
    private String referenceId;

    @Schema(description = "Creation time")
    private LocalDateTime createdAt;
}
