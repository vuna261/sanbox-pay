package com.cloudnut.payment.application.dto.response.paycode;

import com.cloudnut.payment.infrastructure.entity.PayCodeEntityDB;
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
public class PayCodeResDTO {
    private String transactionId;
    private String payCode;
    private Boolean used;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Ho_Chi_Minh")
    private Date createdDate;

    public static PayCodeResDTO from(PayCodeEntityDB entityDB) {
        return PayCodeResDTO.builder()
                .transactionId(entityDB.getTransactionId())
                .payCode(entityDB.getPayCode())
                .used(entityDB.getUsed())
                .createdDate(entityDB.getCreatedDate())
                .build();
    }
}
