package com.cloudnut.payment.application.dto.response.promo;

import com.cloudnut.payment.infrastructure.entity.PromoEntityDB;
import com.cloudnut.payment.utils.PaymentUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromoSumResDTO {
    private String name;
    private String promoCode;
    private String description;
    private PaymentUtils.PROMO_TYPE promoteType;
    private Long amount;

    public static PromoSumResDTO from(PromoEntityDB entityDB) {
        return PromoSumResDTO.builder()
                .name(entityDB.getName())
                .promoCode(entityDB.getPromoCode())
                .description(entityDB.getDescription())
                .promoteType(entityDB.getPromoType())
                .amount(entityDB.getAmount())
                .build();
    }
}
