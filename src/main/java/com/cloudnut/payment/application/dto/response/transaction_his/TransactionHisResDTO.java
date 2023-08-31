package com.cloudnut.payment.application.dto.response.transaction_his;

import com.cloudnut.payment.infrastructure.entity.TransactionHistoryEntityDB;
import com.cloudnut.payment.utils.PaymentUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHisResDTO {
    private Long userId;
    private String email;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Ho_Chi_Minh")
    private Date transactionDate;

    private String detail;
    private PaymentUtils.TRAN_METHOD method;
    private PaymentUtils.TRAN_STATUS status;

    public static TransactionHisResDTO from(TransactionHistoryEntityDB entityDB) {
        return TransactionHisResDTO.builder()
                .userId(entityDB.getUserId())
                .email(entityDB.getEmail())
                .transactionDate(entityDB.getTransactionDate())
                .method(entityDB.getMethod())
                .status(entityDB.getTransactionStatus())
                .detail(entityDB.getDetail())
                .build();
    }
}
