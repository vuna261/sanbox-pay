package com.cloudnut.payment.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public class JwtUtils {
    private JwtUtils() {}

    /**
     * generate JWT token
     * @param privateKey
     * @param claims
     * @param subject
     * @param timeValidate
     * @return
     */
    public static String generateJwt(PrivateKey privateKey,
                                     Map<String, Object> claims, String subject, Long timeValidate) {
        return Jwts.builder().setClaims(claims).setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + timeValidate))
                .signWith(SignatureAlgorithm.RS256, privateKey).compact();
    }

    /**
     * parse all claims from token
     * @param token
     * @param publicKey
     * @return
     */
    public static Claims getAllClaimsFromToken(String token, PublicKey publicKey) {
        token = token.replace("Bearer", "").trim();
        return (Claims) Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();
    }

    /**
     * get specific claim from token
     * @param token
     * @param publicKey
     * @param claimsResolver
     * @param <T>
     * @return
     */
    public static <T> T getClaimFromToken(String token, PublicKey publicKey, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token, publicKey);
        return claimsResolver.apply(claims);
    }

    /**
     * validate token
     * @param token
     * @param publicKey
     * @return
     */
    public static boolean validateToken(String token, PublicKey publicKey) {
        Date expiredDate = getClaimFromToken(token, publicKey, Claims::getExpiration);
        return expiredDate.before(new Date());
    }
}
