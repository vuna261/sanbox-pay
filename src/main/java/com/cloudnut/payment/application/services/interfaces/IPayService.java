package com.cloudnut.payment.application.services.interfaces;

import com.cloudnut.payment.application.dto.request.pay.PayReqDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.PayException;

public interface IPayService {
    void pay(String token, PayReqDTO pay) throws AuthenticationException.MissingToken, PayException.NotEnoughBalance;
    void refund(String token, long amount) throws AuthenticationException.MissingToken;
}
