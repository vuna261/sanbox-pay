package com.cloudnut.payment.application.exception;

public class PayCodeException extends Exception {
    private PayCodeException() {}

    public static class AlreadyExisted extends Exception {}

}
