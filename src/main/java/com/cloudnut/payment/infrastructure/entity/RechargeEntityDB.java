package com.cloudnut.payment.infrastructure.entity;

import com.cloudnut.payment.utils.PaymentUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "recharge")
public class RechargeEntityDB {
    @Id
    @Column(name = "transaction_id")
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String transactionId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentUtils.PAYMENT_METHOD paymentMethod;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "price_id")
    private Long priceId;

    @Column(name = "description")
    private String description;

    @Column(name = "email")
    private String email;

    @Column(name = "bill_status")
    @Enumerated(EnumType.STRING)
    private PaymentUtils.BILL_STATUS billStatus;

    @Column(name = "updated_date")
    private Date updatedDate;

    @Column(name = "created_date")
    private Date createdDate;
}
