package com.cloudnut.payment.infrastructure.repository;

import com.cloudnut.payment.infrastructure.entity.TransactionHistoryEntityDB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TransactionHistoryEntityRepo extends PagingAndSortingRepository<TransactionHistoryEntityDB, String>,
        JpaSpecificationExecutor<TransactionHistoryEntityDB> {
    Page<TransactionHistoryEntityDB> findAllByUserId(Long userId, Pageable pageable);
}
