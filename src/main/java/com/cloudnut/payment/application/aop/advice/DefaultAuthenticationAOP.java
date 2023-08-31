package com.cloudnut.payment.application.aop.advice;

import com.cloudnut.payment.application.aop.annotation.AuthenticationAOP;
import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.services.interfaces.IAuthService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class DefaultAuthenticationAOP {
    @Autowired
    private IAuthService authService;

    @Pointcut("@annotation(authenticationAOP)")
    public void pointcutAnnotationAuthentication(AuthenticationAOP authenticationAOP) {
        throw new UnsupportedOperationException();
    }

    @Around("pointcutAnnotationAuthentication(authenticationAOP)")
    public Object aroundProcessAnnotation(ProceedingJoinPoint joinPoint,
                                          AuthenticationAOP authenticationAOP) throws Throwable {
        String token = (String) joinPoint.getArgs()[0];
        if (token == null) {
            throw new AuthenticationException.NotTokenAtFirstParam();
        }
        String[] roles = authenticationAOP.roles();
        boolean isAuthenticate = authService.checkAuthorization(token, roles);
        if (isAuthenticate) {
            return joinPoint.proceed();
        } else {
            throw new AuthenticationException.UserDoesNotHaveAccess();
        }
    }
}
