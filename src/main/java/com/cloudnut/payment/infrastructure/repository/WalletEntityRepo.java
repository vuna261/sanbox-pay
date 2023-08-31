package com.cloudnut.payment.infrastructure.repository;

import com.cloudnut.payment.infrastructure.entity.WalletEntityDB;
import com.cloudnut.payment.utils.PaymentUtils;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface WalletEntityRepo extends PagingAndSortingRepository<WalletEntityDB, String>,
        JpaSpecificationExecutor<WalletEntityDB> {
    Optional<WalletEntityDB> findByUserId(Long userId);
    List<WalletEntityDB> findAllByWalletType(PaymentUtils.WALLET_TYPE walletType);
}
