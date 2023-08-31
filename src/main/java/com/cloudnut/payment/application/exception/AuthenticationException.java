package com.cloudnut.payment.application.exception;

import lombok.Data;

public class AuthenticationException {
    public static class NotTokenAtFirstParam extends RuntimeException {

    }

    public static class MissingToken extends Exception {}

    public static class RequestVerifyToAuthenticationServiceError extends Exception {
        public RequestVerifyToAuthenticationServiceError() {
        }

        public RequestVerifyToAuthenticationServiceError(String message) {
            super(message);
        }
    }

    public static class RequestVerifyBodyResponseIsNull extends RuntimeException {
        public RequestVerifyBodyResponseIsNull() {
        }

        public RequestVerifyBodyResponseIsNull(String message) {
            super(message);
        }
    }

    public static class RequestVerifyBodyStatusIsNull extends RuntimeException {
        public RequestVerifyBodyStatusIsNull() {
        }

        public RequestVerifyBodyStatusIsNull(String message) {
            super(message);
        }
    }

    @Data
    public static class RequestVerifyTokenError extends RuntimeException {
        private final String code;

        public RequestVerifyTokenError(String code, String message) {
            super(message);
            this.code = code;
        }
    }

    public static class ReadTokenError extends RuntimeException {
        public ReadTokenError() {
        }

        public ReadTokenError(String message) {
            super(message);
        }
    }

    public static class UserDoesNotHaveAccess extends RuntimeException {
        public UserDoesNotHaveAccess() {
        }

        public UserDoesNotHaveAccess(String message) {
            super(message);
        }
    }

    public static class UserNotExist extends Exception {
        public UserNotExist(String message) {
            super(message);
        }
    }
}
