package com.cloudnut.payment.infrastructure.repository;

import com.cloudnut.payment.infrastructure.entity.PromoHistoryEntityDB;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface PromoHistoryEntityRepo extends PagingAndSortingRepository<PromoHistoryEntityDB, String>,
        JpaSpecificationExecutor<PromoHistoryEntityDB> {

    @Modifying
    @Query("DELETE FROM PromoHistoryEntityDB e WHERE e.campaignId = ?1")
    void deleteByCampaignId(Long id);

    @Modifying
    @Query("DELETE FROM PromoHistoryEntityDB e WHERE e.transactionId = ?1")
    void deleteByTransactionId(String transactionId);

    long countByCampaignId(Long id);

    Optional<PromoHistoryEntityDB> findByCampaignIdAndTransactionId(Long promoId, String transactionId);

    Optional<PromoHistoryEntityDB> findByTransactionId(String transactionId);

    void deleteByTransactionIdAndApplyIs(String userId, Boolean apply);
}
