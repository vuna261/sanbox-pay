package com.cloudnut.payment.application.dto.request.recharge;

import com.cloudnut.payment.utils.PaymentUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RechargeReqDTO {
    private String promoCode;

    private String email;

    @NotNull
    private Long priceId;

    private PaymentUtils.PAYMENT_METHOD payMethod;
}
