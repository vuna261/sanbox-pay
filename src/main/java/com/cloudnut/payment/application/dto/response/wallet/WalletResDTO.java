package com.cloudnut.payment.application.dto.response.wallet;

import com.cloudnut.payment.infrastructure.entity.WalletEntityDB;
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
public class WalletResDTO {
    private BigDecimal amount;
    private String walletType;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Ho_Chi_Minh")
    private Date availableDate;

    public static WalletResDTO from(WalletEntityDB entityDB) {
        return WalletResDTO.builder()
                .amount(entityDB.getAmount())
                .walletType(entityDB.getWalletType().name)
                .availableDate(entityDB.getAvailableDate())
                .build();
    }
}
