package com.cloudnut.payment.infrastructure.entity;

import com.cloudnut.payment.utils.PaymentUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transaction_history")
@IdClass(TransactionHistoryEntityDB.TranHisKey.class)
public class TransactionHistoryEntityDB {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "wallet_id")
    private Long walletId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "method")
    @Enumerated(EnumType.STRING)
    private PaymentUtils.TRAN_METHOD method;

    @Id
    @Column(name = "transaction_date")
    private Date transactionDate;

    @Column(name = "transaction_status")
    @Enumerated(EnumType.STRING)
    private PaymentUtils.TRAN_STATUS transactionStatus;

    @Column(name = "detail")
    private String detail;

    @Column(name = "email")
    private String email;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TranHisKey implements Serializable {
        private Long userId;
        private Long walletId;
        private Date transactionDate;
    }
}
