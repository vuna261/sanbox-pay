package com.cloudnut.payment.infrastructure.entity;

import com.cloudnut.payment.utils.PaymentUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "promo")
public class PromoEntityDB {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "promote_code")
    private String promoCode;

    @Column(name = "available_date")
    private Date availableDate;

    @Column(name = "expired_date")
    private Date expiredDate;

    @Column(name = "total_code")
    private Long totalCode;

    @Column(name = "promote_type")
    @Enumerated(EnumType.STRING)
    private PaymentUtils.PROMO_TYPE promoType;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "active")
    private Boolean active;
}
