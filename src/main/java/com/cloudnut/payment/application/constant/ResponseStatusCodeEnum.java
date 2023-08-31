package com.cloudnut.payment.application.constant;


import com.cloudnut.payment.application.dto.response.common.ResponseStatusCode;

public class ResponseStatusCodeEnum {
    private ResponseStatusCodeEnum() {}
    public static final ResponseStatusCode SUCCESS = ResponseStatusCode.builder().code("00").httpCode(200).build();
    public static final ResponseStatusCode BUSINESS_ERROR = ResponseStatusCode.builder().code("BSA0001").httpCode(500).build();
    public static final ResponseStatusCode VALIDATION_ERROR = ResponseStatusCode.builder().code("BSA0002").httpCode(400).build();
    public static final ResponseStatusCode INTERNAL_GENERAL_SERVER_ERROR = ResponseStatusCode.builder().code("BSA0003").httpCode(500).build();
    public static final ResponseStatusCode ERROR_BODY_CLIENT = ResponseStatusCode.builder().code("BSA0004").httpCode(400).build();
    public static final ResponseStatusCode ERROR_BODY_REQUIRED = ResponseStatusCode.builder().code("BSA0005").httpCode(400).build();
    public static final ResponseStatusCode AUTHORIZED_ERROR = ResponseStatusCode.builder().code("AU0001").httpCode(403).build();
    public static final ResponseStatusCode NOT_TOKEN_AT_FIRST_PARAM = ResponseStatusCode.builder().code("AU0002").httpCode(401).build();

    public static final ResponseStatusCode USER_ALREADY_EXISTED = ResponseStatusCode.builder().code("US40901").httpCode(200).build();
    public static final ResponseStatusCode GEN_TOKEN_EXCEPTION = ResponseStatusCode.builder().code("TK50001").httpCode(200).build();
    public static final ResponseStatusCode CREDENTIAL_ERROR = ResponseStatusCode.builder().code("US40001").httpCode(200).build();
    public static final ResponseStatusCode USER_LOCKED = ResponseStatusCode.builder().code("US40101").httpCode(200).build();
    public static final ResponseStatusCode USER_NOT_VERIFY = ResponseStatusCode.builder().code("US40102").httpCode(200).build();
    public static final ResponseStatusCode USER_NOT_FOUND = ResponseStatusCode.builder().code("US40401").httpCode(200).build();

    // item
    public static final ResponseStatusCode ITEM_NOT_FOUND = ResponseStatusCode.builder().code("IT40401").httpCode(200).build();
    public static final ResponseStatusCode ITEM_ALREADY_EXISTED = ResponseStatusCode.builder().code("IT40901").httpCode(200).build();

    // promo
    public static final ResponseStatusCode PROMO_ALREADY_EXISTED = ResponseStatusCode.builder().code("PROMO40901").httpCode(200).build();
    public static final ResponseStatusCode PROMO_NOT_FOUND = ResponseStatusCode.builder().code("PROMO40401").httpCode(200).build();
    public static final ResponseStatusCode PROMO_NOT_AVAILABLE = ResponseStatusCode.builder().code("PROMO40001").httpCode(200).build();
    public static final ResponseStatusCode PROMO_OUT_OF_TOTAL = ResponseStatusCode.builder().code("PROMO40002").httpCode(200).build();

    // bill
    public static final ResponseStatusCode BILL_NOT_COMPLETE = ResponseStatusCode.builder().code("BILL40901").httpCode(200).build();
    public static final ResponseStatusCode BILL_NEED_CANCEL = ResponseStatusCode.builder().code("BILL40902").httpCode(200).build();
    public static final ResponseStatusCode BILL_NOT_FOUND = ResponseStatusCode.builder().code("BILL40401").httpCode(200).build();
    public static final ResponseStatusCode BILL_PAYMENT_NOT_SUPPORT = ResponseStatusCode.builder().code("BILL40801").httpCode(200).build();


    // pay code
    public static final ResponseStatusCode PAY_CODE_INVALID = ResponseStatusCode.builder().code("PAYCODE40001").httpCode(200).build();
    public static final ResponseStatusCode PAY_CODE_ALREADY_EXISTED = ResponseStatusCode.builder().code("PAYCODE40901").httpCode(200).build();

    // email
    public static final ResponseStatusCode PAY_CODE_SEND_EMAIL_ERROR = ResponseStatusCode.builder().code("PAYCODE50301").httpCode(200).build();

    // pay
    public static final ResponseStatusCode PAY_NOT_ENOUGH_BALANCE_ERROR = ResponseStatusCode.builder().code("PAY40003").httpCode(200).build();
}
