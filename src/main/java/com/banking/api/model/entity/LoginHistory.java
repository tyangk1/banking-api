package com.banking.api.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "login_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(length = 100)
    private String device;

    @Column(length = 100)
    private String location;

    @Column(nullable = false)
    @Builder.Default
    private boolean success = true;

    @Column(length = 200)
    private String failureReason;
}
