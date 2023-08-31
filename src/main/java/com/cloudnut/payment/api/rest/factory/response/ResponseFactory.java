package com.cloudnut.payment.api.rest.factory.response;

import com.cloudnut.payment.api.rest.factory.config.MessageResponseFactory;
import com.cloudnut.payment.application.constant.ResponseStatusCodeEnum;
import com.cloudnut.payment.application.dto.response.common.GeneralResponse;
import com.cloudnut.payment.application.dto.response.common.ResponseStatus;
import com.cloudnut.payment.application.dto.response.common.ResponseStatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class ResponseFactory {
    @Autowired
    private MessageResponseFactory messageResponseConfig;

    private String replaceParams(String message, Map<String, String> params) {
        if (!CollectionUtils.isEmpty(params)) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                message = message.replaceAll("%%" + param.getKey() + "%%", param.getValue());
            }
        }
        if (!CollectionUtils.isEmpty(messageResponseConfig.getParams())) {
            for (Map.Entry<String, String> param : messageResponseConfig.getParams().entrySet()) {
                message = message.replaceAll("%%" + param.getKey() + "%%", param.getValue());
            }
        }
        return message;
    }

    private ResponseStatus parseResponseStatus(String code,
                                               Map<String, String> params) {
        ResponseStatus responseStatus = new ResponseStatus(code, true);
        responseStatus.setMessage(replaceParams(responseStatus.getMessage(), params));
        responseStatus.setDisplayMessage(responseStatus.getMessage());
        responseStatus.setResponseTime(new Date());
        return responseStatus;
    }

    public <T> ResponseEntity<GeneralResponse<T>> success(T data) {
        GeneralResponse<T> responseObject = new GeneralResponse<>();
        responseObject.setData(data);
        return success(responseObject);
    }

    public <T> ResponseEntity<GeneralResponse<T>> success(GeneralResponse<T> responseObject) {
        ResponseStatus responseStatus = parseResponseStatus(ResponseStatusCodeEnum.SUCCESS.getCode(), null);

        responseObject.setStatus(responseStatus);
        return ResponseEntity.ok().body(responseObject);
    }

    /**
     * Response SUCCESS with header
     */
    public <T> ResponseEntity<GeneralResponse<T>> successWithHeader(MultiValueMap<String, String> header, T data) {
        GeneralResponse<T> responseObject = new GeneralResponse<>();
        responseObject.setData(data);

        ResponseStatus responseStatus = parseResponseStatus(ResponseStatusCodeEnum.SUCCESS.getCode(), null);

        responseObject.setStatus(responseStatus);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.addAll(header);
        return ResponseEntity.ok().headers(responseHeaders).body(responseObject);
    }

    /**
     * Response with data and code
     */
    public <T> ResponseEntity<GeneralResponse<T>> fail(T data, ResponseStatusCode code) {
        GeneralResponse<T> responseObject = new GeneralResponse<>();
        responseObject.setData(data);
        return fail(responseObject, code, null);
    }

    /**
     * Response with  code
     */
    public <T> ResponseEntity<GeneralResponse<T>> fail(ResponseStatusCode code) {
        GeneralResponse<T> responseObject = new GeneralResponse<>();
        return fail(responseObject, code, null);
    }

    /**
     * Response with  GeneralResponse and code
     */
    public <T> ResponseEntity<GeneralResponse<T>> fail(GeneralResponse<T> responseObject, ResponseStatusCode code) {
        if (Objects.isNull(responseObject)) {
            responseObject = new GeneralResponse<>();
        }
        return fail(responseObject, code, null);
    }

    /**
     * Response with  GeneralResponse and code and params for msg
     */
    public <T> ResponseEntity<GeneralResponse<T>> fail(GeneralResponse<T> responseObject, ResponseStatusCode code,
                                                       Map<String, String> params) {
        ResponseStatus responseStatus = parseResponseStatus(code.getCode(), params);
        if (Objects.isNull(responseObject)) {
            responseObject = new GeneralResponse<>();
        }
        responseObject.setStatus(responseStatus);
        return ResponseEntity.status(code.getHttpCode()).body(responseObject);
    }

    /**
     * Response on filter
     */
    public <T> void httpServletResponseToClient(HttpServletResponse httpServletResponse, T data,
                                                ResponseStatusCode statusCode) throws IOException {
        httpServletResponseToClient(httpServletResponse, data, statusCode, null);
    }

    public <T> void httpServletResponseToClient(HttpServletResponse httpServletResponse, T data,
                                                ResponseStatusCode statusCode, Map<String, String> params) throws IOException {
        GeneralResponse<T> response = new GeneralResponse<>();
        response.setData(data);
        ResponseStatus responseStatus = parseResponseStatus(statusCode.getCode(), params);
        response.setStatus(responseStatus);
        writeToHttpServletResponse(httpServletResponse, response, statusCode);
    }

    public void writeToHttpServletResponse(HttpServletResponse httpServletResponse, Object response,
                                           ResponseStatusCode statusCode) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String responseString = mapper.writeValueAsString(response);
        httpServletResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        httpServletResponse.setStatus(statusCode.getHttpCode());
        httpServletResponse.setHeader(HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_JSON_VALUE);
        httpServletResponse.getWriter().write(responseString);
        httpServletResponse.getWriter().flush();
        httpServletResponse.getWriter().close();
    }
}
