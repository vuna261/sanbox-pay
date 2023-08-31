package com.cloudnut.payment.application.dto.response.promo;

import com.cloudnut.payment.infrastructure.entity.PromoEntityDB;
import com.cloudnut.payment.utils.PaymentUtils;
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
public class PromoResDTO {
    private Long id;
    private String name;
    private String description;
    private String promoteCode;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Ho_Chi_Minh")
    private Date availableDate;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Ho_Chi_Minh")
    private Date expiredDate;

    private Long totalCode;
    private Long usedCode;
    private PaymentUtils.PROMO_TYPE promoteType;
    private Long amount;
    private Boolean active;

    public static PromoResDTO from(PromoEntityDB entityDB, Long usedCode) {
        return PromoResDTO.builder()
                .id(entityDB.getId())
                .name(entityDB.getName())
                .description(entityDB.getDescription())
                .promoteCode(entityDB.getPromoCode())
                .promoteType(entityDB.getPromoType())
                .availableDate(entityDB.getAvailableDate())
                .expiredDate(entityDB.getExpiredDate())
                .totalCode(entityDB.getTotalCode())
                .amount(entityDB.getAmount())
                .active(entityDB.getActive())
                .usedCode(usedCode)
                .build();
    }
}
