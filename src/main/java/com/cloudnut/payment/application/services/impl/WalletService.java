package com.cloudnut.payment.application.services.impl;

import com.cloudnut.payment.application.aop.annotation.AuthenticationAOP;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.transaction_his.TransactionHisResDTO;
import com.cloudnut.payment.application.dto.response.wallet.WalletResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.services.interfaces.IAuthService;
import com.cloudnut.payment.application.services.interfaces.IWalletService;
import com.cloudnut.payment.infrastructure.entity.TransactionHistoryEntityDB;
import com.cloudnut.payment.infrastructure.entity.WalletEntityDB;
import com.cloudnut.payment.infrastructure.repository.TransactionHistoryEntityRepo;
import com.cloudnut.payment.infrastructure.repository.WalletEntityRepo;
import com.cloudnut.payment.utils.PaymentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WalletService implements IWalletService {
    @Autowired
    private WalletEntityRepo walletEntityRepo;

    @Autowired
    private TransactionHistoryEntityRepo transactionHistoryEntityRepo;

    @Autowired
    IAuthService authService;

    /**
     * get my account wallet
     * @param token
     * @return
     * @throws AuthenticationException.MissingToken
     */
    @Override
    @AuthenticationAOP()
    public WalletResDTO getMyWallet(String token) throws
            AuthenticationException.MissingToken {
        Long userId = authService.getUserId(token);
        Optional<WalletEntityDB> entityDBOptional =
                walletEntityRepo.findByUserId(userId);
        WalletEntityDB walletEntityDB;
        if (entityDBOptional.isPresent()) {
            walletEntityDB = entityDBOptional.get();
        } else {
            walletEntityDB = WalletEntityDB.builder()
                    .walletType(PaymentUtils.WALLET_TYPE.PAYG)
                    .userId(userId)
                    .amount(BigDecimal.ZERO)
                    .build();
            walletEntityDB = walletEntityRepo.save(walletEntityDB);
        }
        return WalletResDTO.from(walletEntityDB);
    }

    /**
     * get all my transaction
     * @param token
     * @param pageable
     * @return
     * @throws AuthenticationException.MissingToken
     */
    @Override
    @AuthenticationAOP()
    public PagingResponseDTO<TransactionHisResDTO> getMyTransactionHis(String token, Pageable pageable)
            throws AuthenticationException.MissingToken {
        Long userId = authService.getUserId(token);
        Page<TransactionHistoryEntityDB> transactionHistoryEntityDBPage =
                transactionHistoryEntityRepo.findAllByUserId(userId, pageable);
        List<TransactionHistoryEntityDB> transactionHistoryEntityDBS =
                transactionHistoryEntityDBPage.getContent();

        List<TransactionHisResDTO> transactionHisResDTOS = transactionHistoryEntityDBS.stream()
                .map(TransactionHisResDTO::from)
                .collect(Collectors.toList());
        return PagingResponseDTO.from(transactionHisResDTOS, transactionHistoryEntityDBPage.getTotalPages(),
                transactionHistoryEntityDBPage.getTotalElements());
    }
}
