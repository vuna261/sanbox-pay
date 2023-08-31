package com.cloudnut.payment.application.aop.advice;

import com.cloudnut.payment.api.rest.factory.response.ResponseFactory;
import com.cloudnut.payment.application.constant.ResponseStatusCodeEnum;
import com.cloudnut.payment.application.dto.response.common.GeneralResponse;
import com.cloudnut.payment.application.dto.response.common.ResponseStatus;
import com.cloudnut.payment.application.dto.response.common.ResponseStatusCode;
import com.cloudnut.payment.application.exception.BaseResponseException;
import com.cloudnut.payment.application.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.*;

@ControllerAdvice
@Slf4j
@SuppressWarnings({"all"})
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final Map<String, ResponseStatusCode> handleHttpMessageNotReadableListError = new HashMap<>();

    @Autowired
    ResponseFactory responseFactory;

    public GlobalExceptionHandler() {
        handleHttpMessageNotReadableListError.put("JSON parse error", ResponseStatusCodeEnum.ERROR_BODY_CLIENT);
        handleHttpMessageNotReadableListError.put("Required request body is missing", ResponseStatusCodeEnum.ERROR_BODY_REQUIRED);
    }

    @ExceptionHandler({BaseResponseException.class})
    public ResponseEntity<?> handleValidationExceptions(BaseResponseException ex) {
        try {
            if (Objects.isNull(ex.getParams())) {
                return responseFactory.fail(ex.getDataResponse(), ex.getResponseStatusCode());
            }

            if (Objects.isNull(ex.getDataResponse())) {
                return responseFactory.fail(new GeneralResponse(), ex.getResponseStatusCode(), ex.getParams());
            }
            return responseFactory.fail(ex.getDataResponse(), ex.getResponseStatusCode(), ex.getParams());
        } catch (Exception ignored) {
            return responseFactory.fail(ResponseStatusCodeEnum.INTERNAL_GENERAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler({Exception.class, RuntimeException.class})
    public final ResponseEntity<Object> handleAllExceptions(Exception ex) {
        log.error("Exception: ", ex);
        return this.createResponse(ResponseStatusCodeEnum.INTERNAL_GENERAL_SERVER_ERROR);
    }

    @ExceptionHandler({BusinessException.class})
    public final ResponseEntity<Object> handleValidationExceptions(RuntimeException ex) {
        return this.createResponse(ResponseStatusCodeEnum.BUSINESS_ERROR);
    }

    @ExceptionHandler({ResponseStatusException.class})
    public final ResponseEntity<Object> handlerAuthorizedException(RuntimeException ex) {
        return this.createResponse(ResponseStatusCodeEnum.AUTHORIZED_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> errors.put(error.getObjectName(), error.getDefaultMessage()));
        return this.createResponse(ResponseStatusCodeEnum.VALIDATION_ERROR, errors);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (Objects.nonNull(ex.getMessage())) {
            Optional<ResponseStatusCode> responseStatusCode =
                    handleHttpMessageNotReadableListError.entrySet()
                            .stream()
                            .filter(stringResponseStatusCodeEntry -> ex.getMessage()
                                    .contains(stringResponseStatusCodeEntry.getKey()))
                            .map(Map.Entry::getValue)
                            .findFirst();
            if (responseStatusCode.isPresent()) {
                return this.createResponse(responseStatusCode.get());
            }
        }

        return handleExceptionInternal(ex, null, headers, status, request);
    }

    private ResponseEntity<Object> createResponse(ResponseStatusCode response) {
        ResponseStatus responseStatus = new ResponseStatus(response.getCode(), true);
        responseStatus.setResponseTime(new Date());
        GeneralResponse<Object> responseObject = new GeneralResponse<>();
        responseObject.setStatus(responseStatus);
        return new ResponseEntity<>(responseObject, HttpStatus.valueOf(response.getHttpCode()));
    }

    private ResponseEntity<Object> createResponse(ResponseStatusCode response, Object errors) {
        ResponseStatus responseStatus = new ResponseStatus(response.getCode(), true);
        responseStatus.setResponseTime(new Date());

        GeneralResponse<Object> responseObject = new GeneralResponse<>();
        responseObject.setStatus(responseStatus);
        responseObject.setData(errors);
        return new ResponseEntity<>(responseObject, HttpStatus.valueOf(response.getHttpCode()));
    }

}