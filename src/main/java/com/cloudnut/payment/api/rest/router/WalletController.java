package com.cloudnut.payment.api.rest.router;

import com.cloudnut.payment.api.rest.factory.response.ResponseFactory;
import com.cloudnut.payment.application.constant.ResponseStatusCodeEnum;
import com.cloudnut.payment.application.dto.response.common.GeneralResponse;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.transaction_his.TransactionHisResDTO;
import com.cloudnut.payment.application.dto.response.wallet.WalletResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.BaseResponseException;
import com.cloudnut.payment.application.services.interfaces.IWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("${app.base-url}/wallet")
@Slf4j
public class WalletController {
    @Autowired
    private ResponseFactory responseFactory;

    @Autowired
    private IWalletService walletService;

    /**
     * get my wallet information
     * @param token
     * @return
     */
    @GetMapping()
    public ResponseEntity<GeneralResponse<WalletResDTO>> getMyWallet(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token
    ) {
        WalletResDTO walletResDTO;
        try {
            walletResDTO = walletService.getMyWallet(token);
        } catch (AuthenticationException.MissingToken | AuthenticationException.NotTokenAtFirstParam missingToken) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException(ResponseStatusCodeEnum.BUSINESS_ERROR);
        }
        return responseFactory.success(walletResDTO);
    }

    /**
     * get my transaction history
     * @param token
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/history")
    public ResponseEntity<GeneralResponse<PagingResponseDTO<TransactionHisResDTO>>> getTransactionHis(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer pageNum,
            @RequestParam(value = "size", required = false, defaultValue = "5") Integer pageSize
    ) {
        PagingResponseDTO<TransactionHisResDTO> pagingResponseDTO;
        try {
            Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("transactionDate").descending());
            pagingResponseDTO = walletService.getMyTransactionHis(token, pageable);
        } catch (AuthenticationException.MissingToken | AuthenticationException.NotTokenAtFirstParam missingToken) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException(ResponseStatusCodeEnum.BUSINESS_ERROR);
        }
        return responseFactory.success(pagingResponseDTO);
    }
}
