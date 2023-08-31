package com.cloudnut.payment.infrastructure.repository;

import com.cloudnut.payment.infrastructure.entity.PayCodeEntityDB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface PayCodeEntityRepo extends PagingAndSortingRepository<PayCodeEntityDB, Long>,
        JpaSpecificationExecutor<PayCodeEntityDB> {
    Optional<PayCodeEntityDB> findByPayCodeAndUsedIs(String payCode, Boolean used);
    Optional<PayCodeEntityDB> findByTransactionId(String transactionId);
    Page<PayCodeEntityDB> findAll(Pageable pageable);

    @Modifying
    @Query("DELETE FROM PayCodeEntityDB e WHERE e.id = ?1")
    void deleteById(Long id);
}
