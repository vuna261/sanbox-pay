package com.cloudnut.payment.application.dto.request.promo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromoApplyReqDTO {
    String promoteCode;

    @NotBlank
    String transactionId;
}
