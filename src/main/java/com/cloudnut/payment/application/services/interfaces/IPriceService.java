package com.cloudnut.payment.application.services.interfaces;

import com.cloudnut.payment.application.dto.request.price.PriceReqDTO;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.price.PriceResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.PriceException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPriceService {
    PriceResDTO createPrice(String token, PriceReqDTO priceReqDTO) throws PriceException.AlreadyExisted, AuthenticationException.MissingToken;
    PriceResDTO updatePrice(String token, Long id, PriceReqDTO priceReqDTO) throws AuthenticationException.MissingToken, PriceException.NotFound, PriceException.AlreadyExisted;
    void deletePrice(String token, Long id);
    PagingResponseDTO<PriceResDTO> searchPrice(String token, String searchText, Pageable pageable);
    List<PriceResDTO> getAllPrice();
}
