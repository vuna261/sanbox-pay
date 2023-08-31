package com.cloudnut.payment.application.dto.request.recharge;

import com.cloudnut.payment.utils.PaymentUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayMethodReqDTO {
    @NotEmpty
    @NotBlank
    @NotNull
    private PaymentUtils.PAYMENT_METHOD method;
}
