package com.cloudnut.payment.api.rest.factory.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "message-response")
public class MessageResponseFactory {
    private Map<String, String> params;
}
