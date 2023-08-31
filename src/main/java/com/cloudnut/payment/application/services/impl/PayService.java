package com.cloudnut.payment.application.services.impl;

import com.cloudnut.payment.application.aop.annotation.AuthenticationAOP;
import com.cloudnut.payment.application.dto.request.pay.PayReqDTO;
import com.cloudnut.payment.application.dto.response.wallet.WalletResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.PayException;
import com.cloudnut.payment.application.services.interfaces.IAuthService;
import com.cloudnut.payment.application.services.interfaces.IPayService;
import com.cloudnut.payment.application.services.interfaces.IWalletService;
import com.cloudnut.payment.infrastructure.entity.TransactionHistoryEntityDB;
import com.cloudnut.payment.infrastructure.entity.WalletEntityDB;
import com.cloudnut.payment.infrastructure.repository.TransactionHistoryEntityRepo;
import com.cloudnut.payment.infrastructure.repository.WalletEntityRepo;
import com.cloudnut.payment.utils.Constants;
import com.cloudnut.payment.utils.PaymentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class PayService implements IPayService {
    @Autowired
    private IAuthService authService;

    @Autowired
    private WalletEntityRepo walletEntityRepo;

    @Autowired
    private IWalletService walletService;

    @Autowired
    private TransactionHistoryEntityRepo transactionHistoryEntityRepo;

    /**
     * pay for a lab
     * @param token
     * @param pay
     * @throws AuthenticationException.MissingToken
     * @throws PayException.NotEnoughBalance
     */
    @Override
    @AuthenticationAOP()
    @Transactional
    public void pay(String token, PayReqDTO pay) throws AuthenticationException.MissingToken,
            PayException.NotEnoughBalance {
        Long userId = authService.getUserId(token);
        String email = authService.getEmail(token);

        // check pay as you go
        BigDecimal amountReq = convertAmount(pay);

        Optional<WalletEntityDB> walletEntityDB = walletEntityRepo.findByUserId(userId);

        WalletEntityDB entityDB = WalletEntityDB.builder()
                .userId(userId)
                .walletType(PaymentUtils.WALLET_TYPE.PAYG)
                .amount(BigDecimal.ZERO)
                .build();
        if (walletEntityDB.isPresent()) {
            entityDB = walletEntityDB.get();
        } else {
            entityDB = walletEntityRepo.save(entityDB);
        }


        // get wallet
        WalletResDTO walletResDTO = walletService.getMyWallet(token);


        TransactionHistoryEntityDB transactionHistoryEntityDB = TransactionHistoryEntityDB.builder()
                .transactionStatus(PaymentUtils.TRAN_STATUS.Successful)
                .email(email)
                .method(PaymentUtils.TRAN_METHOD.Pay)
                .amount(amountReq)
                .transactionDate(new Date())
                .walletId(entityDB.getId())
                .userId(userId)
                .detail(Constants.START_LAB.replace("[[amount]]", amountReq.toString())
                        .replace("[[status]]", PaymentUtils.TRAN_STATUS.Successful.toString()))
                .build();


        if (PaymentUtils.WALLET_TYPE.getByName(walletResDTO.getWalletType()) == PaymentUtils.WALLET_TYPE.PREMIUM
                || amountReq.compareTo(BigDecimal.ZERO) == 0) {
            transactionHistoryEntityDB.setAmount(BigDecimal.ZERO);
            transactionHistoryEntityDB.setDetail(Constants.START_LAB.replace("[[amount]]", "0")
                    .replace("[[status]]", PaymentUtils.TRAN_STATUS.Successful.toString()));
            transactionHistoryEntityRepo.save(transactionHistoryEntityDB);
            return;
        }

        if (amountReq.compareTo(walletResDTO.getAmount()) > 0) {
            transactionHistoryEntityDB.setTransactionStatus(PaymentUtils.TRAN_STATUS.Failure);
            transactionHistoryEntityDB.setDetail(Constants.START_LAB.replace("[[amount]]", amountReq.toString())
                    .replace("[[status]]", PaymentUtils.TRAN_STATUS.Failure.toString()));
            transactionHistoryEntityRepo.save(transactionHistoryEntityDB);
            throw new PayException.NotEnoughBalance();
        }

        // update amount
        BigDecimal newAmount = entityDB.getAmount().subtract(amountReq);
        entityDB.setAmount(newAmount);
        walletEntityRepo.save(entityDB);

        transactionHistoryEntityRepo.save(transactionHistoryEntityDB);
    }

    /**
     * refund cost
     * @param token
     * @param amount
     * @throws AuthenticationException.MissingToken
     */
    @Override
    @AuthenticationAOP()
    @Transactional
    public void refund(String token, long amount) throws AuthenticationException.MissingToken {
        Long userId = authService.getUserId(token);
        String email = authService.getEmail(token);
        Optional<WalletEntityDB> walletEntityDB = walletEntityRepo.findByUserId(userId);

        WalletEntityDB entityDB = walletEntityDB.get();
        // get wallet
        WalletResDTO walletResDTO = walletService.getMyWallet(token);

        TransactionHistoryEntityDB transactionHistoryEntityDB = TransactionHistoryEntityDB.builder()
                .transactionStatus(PaymentUtils.TRAN_STATUS.Successful)
                .email(email)
                .method(PaymentUtils.TRAN_METHOD.Refund)
                .amount(new BigDecimal(amount))
                .transactionDate(new Date())
                .walletId(entityDB.getId())
                .userId(userId)
                .detail(Constants.REFUND.replace("[[amount]]", amount + "")
                        .replace("[[status]]", PaymentUtils.TRAN_STATUS.Successful.toString()))
                .build();

        if (PaymentUtils.WALLET_TYPE.getByName(walletResDTO.getWalletType()) == PaymentUtils.WALLET_TYPE.PREMIUM
                || amount == 0L) {
            transactionHistoryEntityDB.setAmount(BigDecimal.ZERO);
            transactionHistoryEntityDB.setDetail(Constants.REFUND.replace("[[amount]]", "0")
                    .replace("[[status]]", PaymentUtils.TRAN_STATUS.Successful.toString()));
            transactionHistoryEntityRepo.save(transactionHistoryEntityDB);
            return;
        }

        BigDecimal newAmount = entityDB.getAmount().add(new BigDecimal(amount));
        entityDB.setAmount(newAmount);
        walletEntityRepo.save(entityDB);

        transactionHistoryEntityRepo.save(transactionHistoryEntityDB);
    }

    /**
     * convert amount
     * @param payReqDTO
     * @return
     */
    private BigDecimal convertAmount(PayReqDTO payReqDTO) {
        if (payReqDTO == null || payReqDTO.getAmount() == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(payReqDTO.getAmount());
    }
}
