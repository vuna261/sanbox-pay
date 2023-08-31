package com.cloudnut.payment.application.dto.response.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GeneralResponse<T> {

    @JsonProperty("status")
    private ResponseStatus status;

    @JsonProperty("data")
    private T data;

    public GeneralResponse(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "{" + "status=" + status +
                ", data=" + data.toString() +
                '}';
    }
}