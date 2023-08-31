package com.cloudnut.payment.api.rest.router;

import com.cloudnut.payment.api.rest.factory.response.ResponseFactory;
import com.cloudnut.payment.application.constant.ResponseStatusCodeEnum;
import com.cloudnut.payment.application.dto.request.price.PriceReqDTO;
import com.cloudnut.payment.application.dto.request.promo.PromoApplyReqDTO;
import com.cloudnut.payment.application.dto.request.promo.PromoCodeReqDTO;
import com.cloudnut.payment.application.dto.request.promo.PromoUpdateReqDTO;
import com.cloudnut.payment.application.dto.response.common.GeneralResponse;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.promo.PromoResDTO;
import com.cloudnut.payment.application.dto.response.promo.PromoSumResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.BaseResponseException;
import com.cloudnut.payment.application.exception.PriceException;
import com.cloudnut.payment.application.exception.PromoException;
import com.cloudnut.payment.application.services.interfaces.IPriceService;
import com.cloudnut.payment.application.services.interfaces.IPromoService;
import com.cloudnut.payment.utils.PaymentUtils;
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

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("${app.base-url}/promote")
@Slf4j
public class PromoController {
    @Autowired
    private ResponseFactory responseFactory;

    @Autowired
    private IPromoService promoService;

    /**
     * create promote code
     * @param token
     * @param promoCodeReqDTO
     * @return
     */
    @PostMapping()
    public ResponseEntity<GeneralResponse<PromoResDTO>> createPromo(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestBody @Valid PromoCodeReqDTO promoCodeReqDTO
    ) {
        PromoResDTO promoResDTO;
        try {
            promoResDTO = promoService.createPromo(token, promoCodeReqDTO);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (PromoException.AlreadyExisted alreadyExisted) {
            throw new BaseResponseException(ResponseStatusCodeEnum.PROMO_ALREADY_EXISTED);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(promoResDTO);
    }

    /**
     * update promote code
     * @param token
     * @param id
     * @param promoUpdateReqDTO
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<PromoResDTO>> updatePromo(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") Long id,
            @RequestBody @Valid PromoUpdateReqDTO promoUpdateReqDTO
    ) {
        PromoResDTO promoResDTO;
        try {
            promoResDTO = promoService.updatePromo(token, id, promoUpdateReqDTO);
        } catch (AuthenticationException.NotTokenAtFirstParam e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (PromoException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.PROMO_NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(promoResDTO);
    }

    /**
     * delete promote code
     * @param token
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<Object>> deletePromo (
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") Long id
    ) {
        try {
            promoService.deletePromo(token, id);
        } catch (AuthenticationException.NotTokenAtFirstParam e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(new GeneralResponse<>());
    }

    /**
     * active or de_active promote code
     * @param token
     * @param id
     * @param action
     * @return
     */
    @PutMapping("/{id}/action")
    public ResponseEntity<GeneralResponse<Object>> activeInactivePromoCode(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") Long id,
            @RequestParam(value = "action", required = false, defaultValue ="active") PaymentUtils.ACTION action
    ) {
        try {
            promoService.inactivePromo(token, id, action);
        } catch (AuthenticationException.NotTokenAtFirstParam e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (PromoException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.PROMO_NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(new GeneralResponse<>());
    }

    /**
     * search promote code
     * @param token
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping()
    public ResponseEntity<GeneralResponse<PagingResponseDTO<PromoResDTO>>> searchPromoCode(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestParam(value = "searchText", required = false, defaultValue = "") String searchText,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer pageNum,
            @RequestParam(value = "size", required = false, defaultValue = "5") Integer pageSize
    ) {
        PagingResponseDTO<PromoResDTO> pagingResponseDTO;
        try {
            Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("name").ascending());
            pagingResponseDTO = promoService.searchPromo(token, searchText, pageable);
        } catch (AuthenticationException.NotTokenAtFirstParam e) {
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
     * get all promote code
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity<GeneralResponse<List<PromoSumResDTO>>> getAllPromoCode (
            // @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token
    ) {
        List<PromoSumResDTO> promoResDTOS;
        try {
            promoResDTOS = promoService.getAllPromo();
        } catch (AuthenticationException.NotTokenAtFirstParam e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException((ResponseStatusCodeEnum.BUSINESS_ERROR));
        }
        return responseFactory.success(promoResDTOS);
    }
}
