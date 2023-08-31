package com.cloudnut.payment.infrastructure.entity;

import com.cloudnut.payment.utils.PaymentUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wallet")
public class WalletEntityDB {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "wallet_type")
    @Enumerated(EnumType.STRING)
    private PaymentUtils.WALLET_TYPE walletType;

    @Column(name = "available_date")
    private Date availableDate;
}
