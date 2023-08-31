package com.cloudnut.payment.api.rest.router;

import com.cloudnut.payment.api.rest.factory.response.ResponseFactory;
import com.cloudnut.payment.application.constant.ResponseStatusCodeEnum;
import com.cloudnut.payment.application.dto.response.common.GeneralResponse;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.paycode.PayCodeResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.BaseResponseException;
import com.cloudnut.payment.application.exception.BillException;
import com.cloudnut.payment.application.exception.PayCodeException;
import com.cloudnut.payment.application.services.interfaces.IPayCodeService;
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

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("${app.base-url}/code")
@Slf4j
public class PayCodeController {
    @Autowired
    private ResponseFactory responseFactory;

    @Autowired
    private IPayCodeService payCodeService;

    /**
     * generate paycode
     * @param token
     * @param transactionId
     * @return
     */
    @GetMapping("/generate")
    ResponseEntity<GeneralResponse<PayCodeResDTO>> generatePaycode(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestParam(value = "bill", required = false, defaultValue ="") String transactionId
    ) {
        PayCodeResDTO payCodeResDTO;
        try {
            payCodeResDTO = payCodeService.generatePayCode(token, transactionId);
        } catch (BillException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.BILL_NOT_FOUND);
        } catch (UnsupportedEncodingException | MessagingException e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.PAY_CODE_SEND_EMAIL_ERROR);
        } catch (PayCodeException.AlreadyExisted alreadyExisted) {
            throw new BaseResponseException(ResponseStatusCodeEnum.PAY_CODE_ALREADY_EXISTED);
        } catch (BillException.PaymentMethodNotSupport paymentMethodNotSupport) {
            throw new BaseResponseException(ResponseStatusCodeEnum.BILL_PAYMENT_NOT_SUPPORT);
        } catch (AuthenticationException.NotTokenAtFirstParam e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException(ResponseStatusCodeEnum.BUSINESS_ERROR);
        }
        return responseFactory.success(payCodeResDTO);
    }

    /**
     * get all code
     * @param token
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("")
    ResponseEntity<GeneralResponse<PagingResponseDTO<PayCodeResDTO>>> getAllPayCode(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer pageNum,
            @RequestParam(value = "size", required = false, defaultValue = "5") Integer pageSize
    ) {
        PagingResponseDTO<PayCodeResDTO> pagingResponseDTO;
        try {
            Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdDate").descending());
            pagingResponseDTO = payCodeService.getAllPayCode(token, pageable);
        } catch (AuthenticationException.NotTokenAtFirstParam e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException(ResponseStatusCodeEnum.BUSINESS_ERROR);
        }
        return responseFactory.success(pagingResponseDTO);
    }

    /**
     * delete specific paycode
     * @param token
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    ResponseEntity<GeneralResponse<Object>> deletePayCode(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") Long id
    ) {

        try {
            payCodeService.deleteCode(token,id);
        } catch (AuthenticationException.NotTokenAtFirstParam e) {
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
