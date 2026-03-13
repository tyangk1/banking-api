package com.banking.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "beneficiaries",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "account_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user"})
public class Beneficiary extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, length = 200)
    private String accountHolderName;

    @Column(length = 200)
    @Builder.Default
    private String bankName = "Premium Banking";

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean favorite = false;

    @Builder.Default
    private int transferCount = 0;

    private LocalDateTime lastUsedAt;

    public void incrementTransferCount() {
        this.transferCount++;
        this.lastUsedAt = LocalDateTime.now();
    }
}
