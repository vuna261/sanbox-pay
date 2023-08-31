package com.cloudnut.payment.api.rest.router;

import com.cloudnut.payment.api.rest.factory.response.ResponseFactory;
import com.cloudnut.payment.application.constant.ResponseStatusCodeEnum;
import com.cloudnut.payment.application.dto.request.recharge.PayReqDTO;
import com.cloudnut.payment.application.dto.request.recharge.RechargeReqDTO;
import com.cloudnut.payment.application.dto.response.common.GeneralResponse;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.recharge.RechargeResDTO;
import com.cloudnut.payment.application.exception.*;
import com.cloudnut.payment.application.services.interfaces.IRechargeService;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("${app.base-url}/bill")
@Slf4j
public class RechargeController {
    @Autowired
    private ResponseFactory responseFactory;

    @Autowired
    private IRechargeService rechargeService;

    /**
     * create new bill
     * @param token
     * @param rechargeReqDTO
     * @return
     */
    @PostMapping()
    public ResponseEntity<GeneralResponse<RechargeResDTO>> createBill(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestBody @Valid RechargeReqDTO rechargeReqDTO
    ) {
        RechargeResDTO rechargeResDTO;
        try {
            rechargeResDTO = rechargeService.createBill(token, rechargeReqDTO);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (BillException.NotCompleted notCompleted) {
            throw new BaseResponseException(ResponseStatusCodeEnum.BILL_NOT_COMPLETE);
        } catch (PriceException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.ITEM_NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(rechargeResDTO);
    }

    /**
     * update bill
     * @param token
     * @param id
     * @param rechargeReqDTO
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<RechargeResDTO>> updateBill (
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") String id,
            @RequestBody @Valid RechargeReqDTO rechargeReqDTO
    ) {
        RechargeResDTO rechargeResDTO;
        try {
            rechargeResDTO = rechargeService.updateBill(token, id, rechargeReqDTO);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (BillException.NeedCancelFirst needCancelFirst) {
            throw new BaseResponseException(ResponseStatusCodeEnum.BILL_NEED_CANCEL);
        } catch (PriceException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.ITEM_NOT_FOUND);
        } catch (BillException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.BILL_NOT_FOUND);
        } catch (PromoException.NotAvailable notAvailable) {
            throw new BaseResponseException(ResponseStatusCodeEnum.PROMO_NOT_AVAILABLE);
        } catch (PromoException.OutOfTotal outOfTotal) {
            throw new BaseResponseException(ResponseStatusCodeEnum.PROMO_OUT_OF_TOTAL);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(rechargeResDTO);
    }

    /**
     * get bill detail
     * @param token
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponse<RechargeResDTO>> getBill (
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") String id
    ) {
        RechargeResDTO rechargeResDTO;
        try {
            rechargeResDTO = rechargeService.getDetailBill(token, id);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (PriceException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.ITEM_NOT_FOUND);
        } catch (BillException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.BILL_NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(rechargeResDTO);
    }

    /**
     * cancel bill
     * @param token
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<Object>> cancelBill(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") String id
    ) {
        try {
            rechargeService.cancelBill(token, id);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (BillException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.BILL_NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(new GeneralResponse<>());
    }

    /**
     * get all my bill
     * @param token
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping()
    public ResponseEntity<GeneralResponse<PagingResponseDTO<RechargeResDTO>>> getMyBill(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer pageNum,
            @RequestParam(value = "size", required = false, defaultValue = "5") Integer pageSize
    ) {
        PagingResponseDTO<RechargeResDTO> pagingResponseDTO;
        try {
            Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("updatedDate").descending());
            pagingResponseDTO = rechargeService.searchBill(token, pageable);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(pagingResponseDTO);
    }

    /**
     * pay bill by pay_code
     * @param token
     * @param id
     * @param payReqDTO
     * @return
     */
    @PostMapping("/{id}/pay-code")
    public ResponseEntity<GeneralResponse<Object>> payBillByCode(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") String id,
            @RequestBody @Valid PayReqDTO payReqDTO
    ) {
        try {
            rechargeService.payBillCode(token, id, payReqDTO);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (BillException.CodeInvalid codeInvalid) {
            throw new BaseResponseException(ResponseStatusCodeEnum.PAY_CODE_INVALID);
        } catch (BillException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.BILL_NOT_FOUND);
        } catch (PriceException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.ITEM_NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(new GeneralResponse<>());
    }

    /**
     * pay by vnpay
     * @param request
     * @param token
     * @param id
     * @throws IOException
     */
    @GetMapping("/{id}/pay-vnpay")
    public ResponseEntity<GeneralResponse<String>> payVnpay(
            HttpServletRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") String id
    ) {
        String VNPAY_REDIRECT_URL;
        try {
            VNPAY_REDIRECT_URL = rechargeService.payBillVnpay(token, id, request);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (BillException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.BILL_NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(VNPAY_REDIRECT_URL);
    }
}
