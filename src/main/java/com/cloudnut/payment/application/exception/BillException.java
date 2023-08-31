package com.cloudnut.payment.application.exception;

public class BillException extends Exception {
    private BillException() {}

    public static class NotCompleted extends Exception {}

    public static class NeedCancelFirst extends Exception {}

    public static class NotFound extends Exception {}

    public static class PaymentMethodNotSupport extends Exception {}

    public static class CodeInvalid extends Exception {}
}
