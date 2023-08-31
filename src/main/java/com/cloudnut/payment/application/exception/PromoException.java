package com.cloudnut.payment.application.exception;

public class PromoException extends Exception {
    private PromoException() {}
    public static class NotFound extends Exception {}
    public static class AlreadyExisted extends Exception {}
    public static class OutOfTotal extends Exception {}
    public static class NotAvailable extends Exception {}
}
