package com.cloudnut.payment.application.dto.request.paycode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayCodeReqDTO {
    private String transactionId;
}
