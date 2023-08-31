package com.cloudnut.payment.application.dto.request.recharge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayReqDTO {
    private String transactionId;
    private String payCode;
}
