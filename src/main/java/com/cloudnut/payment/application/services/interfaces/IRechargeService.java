package com.cloudnut.payment.application.services.interfaces;

import com.cloudnut.payment.application.dto.request.recharge.PayMethodReqDTO;
import com.cloudnut.payment.application.dto.request.recharge.PayReqDTO;
import com.cloudnut.payment.application.dto.request.recharge.RechargeReqDTO;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.recharge.RechargeResDTO;
import com.cloudnut.payment.application.dto.response.wallet.HandlerVnpayResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.BillException;
import com.cloudnut.payment.application.exception.PriceException;
import com.cloudnut.payment.application.exception.PromoException;
import com.cloudnut.payment.utils.PaymentUtils;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public interface IRechargeService {
    RechargeResDTO createBill(String token, RechargeReqDTO rechargeReqDTO) throws AuthenticationException.MissingToken, PriceException.NotFound, BillException.NotCompleted;
    RechargeResDTO updateBill(String token, String transactionId, RechargeReqDTO rechargeReqDTO) throws AuthenticationException.MissingToken, PriceException.NotFound, BillException.NeedCancelFirst, BillException.NotFound, PromoException.OutOfTotal, PromoException.NotAvailable;
    RechargeResDTO getDetailBill(String token, String transactionId) throws AuthenticationException.MissingToken, PriceException.NotFound, BillException.NotFound;
    PagingResponseDTO<RechargeResDTO> searchBill(String token, Pageable pageable) throws AuthenticationException.MissingToken;
    void cancelBill(String token, String transactionId) throws AuthenticationException.MissingToken, BillException.NotFound;
    void payBillCode(String token, String transactionId, PayReqDTO payReqDTO) throws AuthenticationException.MissingToken, BillException.NotFound, BillException.CodeInvalid, PriceException.NotFound;
    String payBillVnpay(String token, String transactionId, HttpServletRequest request) throws AuthenticationException.MissingToken, BillException.NotFound;
    HandlerVnpayResDTO handlerVnPay(HashMap<String, String> queryParam) throws UnsupportedEncodingException;
    void refund(String token, String transactionId, PaymentUtils.REFUND_METHOD refundMethod);
}
