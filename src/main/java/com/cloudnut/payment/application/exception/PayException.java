package com.cloudnut.payment.application.exception;

public class PayException extends Exception {
    private PayException() {}

    public static class NotEnoughBalance extends Exception {}
}
