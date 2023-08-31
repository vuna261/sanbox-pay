package com.cloudnut.payment.application.services.interfaces;

import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.transaction_his.TransactionHisResDTO;
import com.cloudnut.payment.application.dto.response.wallet.WalletResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IWalletService {
    WalletResDTO getMyWallet(String token) throws AuthenticationException.MissingToken;
    PagingResponseDTO<TransactionHisResDTO> getMyTransactionHis(String token, Pageable pageable) throws AuthenticationException.MissingToken;
}
