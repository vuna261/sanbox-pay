package com.cloudnut.payment.application.exception;

public class PriceException extends Exception {
    private PriceException() {}

    public static class NotFound extends Exception {}

    public static class AlreadyExisted extends Exception {}
}
