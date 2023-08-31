package com.cloudnut.payment.api.rest.router;

import com.cloudnut.payment.api.rest.factory.response.ResponseFactory;
import com.cloudnut.payment.application.constant.ResponseStatusCodeEnum;
import com.cloudnut.payment.application.dto.request.pay.PayReqDTO;
import com.cloudnut.payment.application.dto.response.common.GeneralResponse;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.BaseResponseException;
import com.cloudnut.payment.application.exception.PayException;
import com.cloudnut.payment.application.services.interfaces.IPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("${app.base-url}")
@Slf4j
public class PayController {
    @Autowired
    private ResponseFactory responseFactory;

    @Autowired
    private IPayService payService;

    /**
     * pay coins for start an lab
     * @param token
     * @param payReqDTO
     * @return
     */
    @PostMapping("/pay")
    public ResponseEntity<GeneralResponse<Object>> payForLab(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestBody @Valid PayReqDTO payReqDTO
    ) {
        try {
            payService.pay(token, payReqDTO);
        } catch (PayException.NotEnoughBalance notEnoughBalance) {
            throw new BaseResponseException(ResponseStatusCodeEnum.PAY_NOT_ENOUGH_BALANCE_ERROR);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException(ResponseStatusCodeEnum.BUSINESS_ERROR);
        }
        return responseFactory.success(new GeneralResponse<>());
    }

    /**
     * refund coins when start lab error
     * @param token
     * @param payReqDTO
     * @return
     */
    @PostMapping("/refund")
    public ResponseEntity<GeneralResponse<Object>> refundLab(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestBody @Valid PayReqDTO payReqDTO
    ) {
        try {
            payService.refund(token, payReqDTO.getAmount());
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException(ResponseStatusCodeEnum.BUSINESS_ERROR);
        }
        return responseFactory.success(new GeneralResponse<>());
    }
}
