package com.cloudnut.payment.application.dto.response.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseStatusCode {
    private final String code;
    private final int httpCode;

    public ResponseStatusCode(String code, int httpCode) {
        this.code = code;
        this.httpCode = httpCode;
    }
    @Override
    public String toString() {
        return "ResponseStatus{" +
                "code='" + code + '\'' +
                "httpCode='" + httpCode + '\'' +
                '}';
    }

}