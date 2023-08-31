package com.cloudnut.payment.application.services.interfaces;

import com.cloudnut.payment.application.dto.request.promo.PromoCodeReqDTO;
import com.cloudnut.payment.application.dto.request.promo.PromoUpdateReqDTO;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.promo.PromoResDTO;
import com.cloudnut.payment.application.dto.response.promo.PromoSumResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.PriceException;
import com.cloudnut.payment.application.exception.PromoException;
import com.cloudnut.payment.utils.PaymentUtils;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPromoService {
    PromoResDTO createPromo(String token, PromoCodeReqDTO promoCodeReqDTO) throws PromoException.AlreadyExisted, AuthenticationException.MissingToken;
    PromoResDTO updatePromo(String token, Long id, PromoUpdateReqDTO promoUpdateReqDTO) throws PromoException.NotFound;
    void deletePromo(String token, Long id);
    void inactivePromo(String token, Long id, PaymentUtils.ACTION action) throws PromoException.NotFound;
    void inactivePromo(Long id, PaymentUtils.ACTION action) throws PromoException.NotFound;
    PagingResponseDTO<PromoResDTO> searchPromo(String token, String searchText, Pageable pageable);
    List<PromoSumResDTO> getAllPromo();
    PromoSumResDTO applyPromoCode(String token, String promoCode, String transactionId) throws PromoException.NotAvailable, PromoException.OutOfTotal, AuthenticationException.MissingToken;
    void applyPromoCodeVoid(String token, String promoCode, String transactionId) throws AuthenticationException.MissingToken, PromoException.OutOfTotal, PromoException.NotAvailable;
}
