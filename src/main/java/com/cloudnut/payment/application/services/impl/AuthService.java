package com.cloudnut.payment.application.services.impl;

import com.cloudnut.payment.application.exception.AuthenticationException;
import com.cloudnut.payment.application.services.interfaces.IAuthService;
import com.cloudnut.payment.utils.Constants;
import com.cloudnut.payment.utils.FileUtils;
import com.cloudnut.payment.utils.JwtUtils;
import com.cloudnut.payment.utils.UserUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@Slf4j
public class AuthService implements IAuthService {

    @Value("${jwt.public-key}")
    private String PUBLIC_KEY;

    /**
     * validate token and role
     * @param token
     * @param roles
     * @return
     */
    @Override
    public boolean checkAuthorization(String token, String[] roles) {
        try {
            List<String> claimRole = getRoles(token);
            if ((roles.length == 0 && claimRole.size() > 0) || claimRole.contains(UserUtils.ROLE_ADMIN)) {
                return true;
            }

            boolean roleCheck = false;
            if (roles.length > 0) {
                for (int i = 0; i < roles.length; i++) {
                    if (claimRole.contains(roles[i].toUpperCase())) {
                        roleCheck = true;
                        break;
                    }
                }
            }
            return roleCheck;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * get username from token
     * @param token
     * @return
     * @throws AuthenticationException.MissingToken
     */
    @Override
    public String getUserName(String token) throws AuthenticationException.MissingToken {
        Claims claims;
        try {
            claims = JwtUtils.getAllClaimsFromToken(token, FileUtils.getPublicKeyFromFile(PUBLIC_KEY));
        } catch (Exception e) {
            throw new AuthenticationException.MissingToken();
        }
        return (String) claims.get(Constants.USERNAME);
    }

    /**
     * get user email from token
     * @param token
     * @return
     * @throws AuthenticationException.MissingToken
     */
    @Override
    public String getEmail(String token) throws AuthenticationException.MissingToken {
        Claims claims;
        try {
            claims = JwtUtils.getAllClaimsFromToken(token, FileUtils.getPublicKeyFromFile(PUBLIC_KEY));
        } catch (Exception e) {
            throw new AuthenticationException.MissingToken();
        }
        return (String) claims.get(Constants.EMAIL);
    }


    /**
     * get user id from token
     * @param token
     * @return
     * @throws AuthenticationException.MissingToken
     */
    @Override
    public Long getUserId(String token) throws AuthenticationException.MissingToken {
        Claims claims;
        try {
            claims = JwtUtils.getAllClaimsFromToken(token, FileUtils.getPublicKeyFromFile(PUBLIC_KEY));
        } catch (Exception e) {
            throw new AuthenticationException.MissingToken();
        }
        return Long.parseLong(claims.getSubject());
    }

    /**
     * get list role from token
     * @param token
     * @return
     * @throws AuthenticationException.MissingToken
     */
    private List<String> getRoles(String token) throws AuthenticationException.MissingToken {
        Claims claims = getClaimsFromToken(token);
        List<LinkedHashMap<String, String>> roles = (List<LinkedHashMap<String, String>>) claims.get(Constants.AUTHORITY);
        List<String> claimRoles = new ArrayList<>();
        for (LinkedHashMap<String, String> role: roles) {
            String roleTmp = role.get("authority");
            roleTmp = roleTmp.replaceAll(Constants.ROLE_PREFIX, "").toUpperCase();
            claimRoles.add(roleTmp);
        }
        return claimRoles;
    }

    /**
     * get claims from token and validate this token
     * @param token
     * @return
     * @throws AuthenticationException.MissingToken
     */
    private Claims getClaimsFromToken(String token) throws AuthenticationException.MissingToken {
        Claims claims;
        try {
            // validate token
            if (JwtUtils.validateToken(token, FileUtils.getPublicKeyFromFile(PUBLIC_KEY))) {
                throw new AuthenticationException.MissingToken();
            }
            // get all claims from token
            claims = (Claims) JwtUtils.getAllClaimsFromToken(token, FileUtils.getPublicKeyFromFile(PUBLIC_KEY));
        } catch (Exception e) {
            throw new AuthenticationException.MissingToken();
        }
        return claims;
    }
}