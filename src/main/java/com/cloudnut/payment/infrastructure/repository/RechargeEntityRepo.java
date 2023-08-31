package com.cloudnut.payment.infrastructure.repository;

import com.cloudnut.payment.infrastructure.entity.RechargeEntityDB;
import com.cloudnut.payment.utils.PaymentUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface RechargeEntityRepo extends PagingAndSortingRepository<RechargeEntityDB, String>,
        JpaSpecificationExecutor<RechargeEntityDB> {
    long countByUserIdAndBillStatus(Long userId, PaymentUtils.BILL_STATUS status);
    Optional<RechargeEntityDB> findByUserIdAndBillStatus(Long userId, PaymentUtils.BILL_STATUS status);
    Optional<RechargeEntityDB> findByTransactionId(String transactionId);
    Page<RechargeEntityDB> findAllByUserId(Long userId, Pageable pageable);
}
