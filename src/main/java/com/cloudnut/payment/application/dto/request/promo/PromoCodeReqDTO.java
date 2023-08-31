package com.cloudnut.payment.application.dto.request.promo;

import com.cloudnut.payment.utils.PaymentUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeReqDTO {
    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String promoCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date availableDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date expiredDate;

    @NotNull
    private Long totalCode;

    private PaymentUtils.PROMO_TYPE promoType;

    @NotNull
    private Long amount;
}
