package com.cloudnut.payment.application.dto.response.price;

import com.cloudnut.payment.infrastructure.entity.PriceEntityDB;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriceResDTO {
    private Long id;
    private String name;
    private String accountType;
    private String description;
    private Long amount;
    private Long price;
    private String createdBy;
    private String updatedBy;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Ho_Chi_Minh")
    private Date createdDate;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Ho_Chi_Minh")
    private Date updatedDate;

    public static PriceResDTO from(PriceEntityDB entityDB) {
        return PriceResDTO.builder()
                .id(entityDB.getId())
                .name(entityDB.getName())
                .description(entityDB.getDescription())
                .accountType(entityDB.getAccountType().name)
                .amount(entityDB.getAmount())
                .price(entityDB.getPrice())
                .createdDate(entityDB.getCreatedDate())
                .createdBy(entityDB.getCreatedBy())
                .updatedBy(entityDB.getUpdatedBy())
                .updatedDate(entityDB.getUpdatedDate())
                .build();
    }
}
