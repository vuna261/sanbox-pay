package com.cloudnut.payment.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "promo_history")
@IdClass(PromoHistoryEntityDB.PromoHisKey.class)
public class PromoHistoryEntityDB {
    @Id
    @Column(name = "campaign_id")
    private Long campaignId;

    @Id
    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "apply_date")
    private Date applyDate;

    @Column(name = "apply")
    private Boolean apply;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PromoHisKey implements Serializable {
        private Long campaignId;
        private String transactionId;
    }
}
