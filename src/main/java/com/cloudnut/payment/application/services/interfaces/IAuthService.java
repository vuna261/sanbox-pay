package com.cloudnut.payment.application.services.interfaces;

import com.cloudnut.payment.application.exception.AuthenticationException;

public interface IAuthService {
    boolean checkAuthorization(String token, String[] roles);
    String getUserName(String token) throws AuthenticationException.MissingToken;
    String getEmail(String token) throws AuthenticationException.MissingToken;
    Long getUserId(String token) throws AuthenticationException.MissingToken;
}
