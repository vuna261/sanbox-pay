package com.cloudnut.payment.application.exception;

import com.cloudnut.payment.application.dto.response.common.GeneralResponse;
import com.cloudnut.payment.application.dto.response.common.ResponseStatusCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseResponseException extends RuntimeException{
    private final ResponseStatusCode responseStatusCode;
    private GeneralResponse dataResponse;
    private Map<String, String> params;

    public BaseResponseException(ResponseStatusCode responseStatusCode) {
        this.responseStatusCode = responseStatusCode;
    }

    public BaseResponseException(GeneralResponse dataResponse, ResponseStatusCode responseStatusCode) {
        this.dataResponse = dataResponse;
        this.responseStatusCode = responseStatusCode;
    }

    public BaseResponseException(GeneralResponse dataResponse, ResponseStatusCode responseStatusCode, Map<String, String> params) {
        this.dataResponse = dataResponse;
        this.responseStatusCode = responseStatusCode;
        this.params = params;
    }
}
