package com.cloudnut.payment.application.dto.response.wallet;

import com.cloudnut.payment.utils.PaymentUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HandlerVnpayResDTO {
    private String transactionId;
    private PaymentUtils.TRAN_STATUS status;
}
