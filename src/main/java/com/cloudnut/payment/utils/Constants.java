package com.cloudnut.payment.utils;

public class Constants {
    private Constants() {}

    public static final String DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String AUTHORITY = "authorities";
    public static final String EMAIL = "email";
    public static final String USERNAME = "username";
    public static final String SYSTEM = "system";
    public static final String ATTRIBUTES = "attributes";
    public static final String CLOUD_NUT_SALT = "CLOUDNUTSANDBOX";
    public static final String ROLE_PREFIX = "ROLE_";

    public static final String START_LAB = "Pay [[amount]] coins for start lab [[status]]";
    public static final String REFUND = "Refund [[amount]] coins [[status]]";
    public static final String RECHARGE = "Recharge [[amount]] coins [[status]]";
    public static final String RECHARGE_COMMON_FAILURE = "Recharge for [[email]] failure";


    public static final String PAY_DESCRIPTION = "Thanh toán gói [[transactionId]]";
}
