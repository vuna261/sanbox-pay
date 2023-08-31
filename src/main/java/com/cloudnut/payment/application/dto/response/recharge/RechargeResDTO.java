package com.cloudnut.payment.application.dto.response.recharge;

import com.cloudnut.payment.infrastructure.entity.RechargeEntityDB;
import com.cloudnut.payment.utils.PaymentUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RechargeResDTO {
    private String transactionId;
    // add item info
    private Long itemId;
    private String itemName;
    private Long itemAmount;
    private Long itemPrice;
    // end

    private String email;
    private String priceName;
    private Long price;
    private String promoCode;
    private PaymentUtils.BILL_STATUS billStatus;
    private PaymentUtils.PAYMENT_METHOD payMethod;

    public static RechargeResDTO from(RechargeEntityDB entityDB) {
        return RechargeResDTO.builder()
                .transactionId(entityDB.getTransactionId())
                .email(entityDB.getEmail())
                .price(entityDB.getAmount())
                .payMethod(entityDB.getPaymentMethod())
                .billStatus(entityDB.getBillStatus())
                .build();
    }
}