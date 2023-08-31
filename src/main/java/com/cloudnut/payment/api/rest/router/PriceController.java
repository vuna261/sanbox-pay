package com.cloudnut.payment.api.rest.router;

import com.cloudnut.payment.api.rest.factory.response.ResponseFactory;
import com.cloudnut.payment.application.constant.ResponseStatusCodeEnum;
import com.cloudnut.payment.application.dto.request.price.PriceReqDTO;
import com.cloudnut.payment.application.dto.response.common.GeneralResponse;
import com.cloudnut.payment.application.dto.response.common.PagingResponseDTO;
import com.cloudnut.payment.application.dto.response.price.PriceResDTO;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.exception.BaseResponseException;
import com.cloudnut.payment.application.exception.PriceException;
import com.cloudnut.payment.application.services.interfaces.IPriceService;
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
@RequestMapping("${app.base-url}/items")
@Slf4j
public class PriceController {
    @Autowired
    private ResponseFactory responseFactory;

    @Autowired
    private IPriceService priceService;

    /**
     * create new course item
     * @param token
     * @param priceReqDTO
     * @return
     */
    @PostMapping()
    public ResponseEntity<GeneralResponse<PriceResDTO>> createItem(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestBody @Valid PriceReqDTO priceReqDTO
    ) {
        PriceResDTO priceResDTO;
        try {
            priceResDTO = priceService.createPrice(token, priceReqDTO);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (PriceException.AlreadyExisted alreadyExisted) {
            throw new BaseResponseException(ResponseStatusCodeEnum.ITEM_ALREADY_EXISTED);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException(ResponseStatusCodeEnum.BUSINESS_ERROR);
        }
        return responseFactory.success(priceResDTO);
    }

    /**
     * update item
     * @param token
     * @param id
     * @param priceReqDTO
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponse<PriceResDTO>> updateItem(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") Long id,
            @RequestBody @Valid PriceReqDTO priceReqDTO
    ) {
        PriceResDTO priceResDTO;
        try {
            priceResDTO = priceService.updatePrice(token, id, priceReqDTO);
        } catch (AuthenticationException.NotTokenAtFirstParam | AuthenticationException.MissingToken e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (PriceException.AlreadyExisted alreadyExisted) {
            throw new BaseResponseException(ResponseStatusCodeEnum.ITEM_ALREADY_EXISTED);
        } catch (PriceException.NotFound notFound) {
            throw new BaseResponseException(ResponseStatusCodeEnum.ITEM_NOT_FOUND);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException(ResponseStatusCodeEnum.BUSINESS_ERROR);
        }
        return responseFactory.success(priceResDTO);
    }

    /**
     * delete specific item by id
     * @param token
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponse<Object>> deleteItem(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable("id") Long id
    ) {
        try {
            priceService.deletePrice(token, id);
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

    /**
     * search item
     * @param token
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping()
    public ResponseEntity<GeneralResponse<PagingResponseDTO<PriceResDTO>>> searchItem(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @RequestParam(value = "searchText", required = false, defaultValue = "") String searchText,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer pageNum,
            @RequestParam(value = "size", required = false, defaultValue = "5") Integer pageSize
    ) {
        PagingResponseDTO<PriceResDTO> pagingResponseDTO;
        try {
            Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("name").ascending());
            pagingResponseDTO = priceService.searchPrice(token, searchText, pageable);
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
     * get all item
     * @param token
     * @return
     */
    @GetMapping("/all")
    public ResponseEntity<GeneralResponse<List<PriceResDTO>>> getAllItem(
            // @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token
    ) {
        List<PriceResDTO> priceResDTOList;
        try {
            priceResDTOList = priceService.getAllPrice();
        } catch (AuthenticationException.NotTokenAtFirstParam e) {
            throw new BaseResponseException(ResponseStatusCodeEnum.NOT_TOKEN_AT_FIRST_PARAM);
        } catch (AuthenticationException.UserDoesNotHaveAccess e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BaseResponseException(ResponseStatusCodeEnum.BUSINESS_ERROR);
        }
        return responseFactory.success(priceResDTOList);
    }
}
