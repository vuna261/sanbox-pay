package com.cloudnut.payment.application.i18n;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class Translator {
    private static MessageSource messageSource;

    @Autowired
    Translator(MessageSource messageSource) {
        Translator.messageSource = messageSource;
    }

    public static String toLocale(String messageCode) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(messageCode, null, locale);
    }
}