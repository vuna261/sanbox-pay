package com.cloudnut.payment.application.dto.response.common;

import com.cloudnut.payment.application.i18n.Translator;
import com.cloudnut.payment.utils.Constants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ResponseStatus implements Serializable {
    private String code;

    @JsonProperty("message")
    private String message;

    @JsonProperty("responseTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_FORMAT_ISO8601)
    private Date responseTime;

    @JsonProperty("displayMessage")
    private String displayMessage;

    public ResponseStatus(String code, boolean setMessageImplicitly) {
        setCode(code, setMessageImplicitly);
    }

    public void setCode(String code, boolean setMessageImplicitly) {
        this.code = code;
        if (setMessageImplicitly) {
            this.message = Translator.toLocale(code);
        }
        this.displayMessage = this.message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        setCode(code, true);
    }
}