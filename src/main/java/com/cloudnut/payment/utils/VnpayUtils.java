package com.cloudnut.payment.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VnpayUtils {
    private VnpayUtils() {}

    public static final String DATE_FORMAT_VNPAY = "yyyyMMddHHmmss";
    public static final String VNPAY_VERSION = "2.1.0";
    public static final String VNPAY_PAY = "pay";
    public static final String VNPAY_CURRENT_CODE = "VND";
    public static final String VNPAY_ORDER_TYPE = "other";
    public static final String VNPAY_LOCALE = "vn";


    // vnpay key
    public static final String VNPAY_VERSION_KEY = "vnp_Version";
    public static final String VNPAY_COMMAND_KEY = "vnp_Command";
    public static final String VNPAY_TMN_KEY = "vnp_TmnCode";
    public static final String VNPAY_LOCALE_KEY = "vnp_Locale";
    public static final String VNPAY_CURRENT_CODE_KEY = "vnp_CurrCode";
    public static final String VNPAY_TXN_KEY = "vnp_TxnRef";
    public static final String VNPAY_ORDER_INFO_KEY = "vnp_OrderInfo";
    public static final String VNPAY_ORDER_TYPE_KEY = "vnp_OrderType";
    public static final String VNPAY_AMOUNT_KEY = "vnp_Amount";
    public static final String VNPAY_RETURN_URL_KEY = "vnp_ReturnUrl";
    public static final String VNPAY_IP_KEY = "vnp_IpAddr";
    public static final String VNPAY_CREATE_DATE_KEY = "vnp_CreateDate";
    public static final String VNPAY_HASHED_KEY = "vnp_SecureHash";
    public static final String VNPAY_HASHED_TYPE_KEY = "vnp_SecureHashType";
    public static final String VNPAY_EXPIRED_KEY= "vnp_ExpireDate";
    public static final String VNPAY_TRANSACTION_STATUS_KEY = "vnp_TransactionStatus";

    /**
     * generate hmac
     * @param key
     * @param data
     * @return
     */
    public static String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * query param generator
     * @param params
     * @return
     */
    public static String queryGenerator(HashMap<String, String> params) {
        try {
            StringBuilder sb = new StringBuilder();
            List fieldNames = new ArrayList(params.keySet());
            Collections.sort(fieldNames);
            Iterator itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = (String) itr.next();
                String fieldValue = (String) params.get(fieldName);
                sb.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                        .append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    sb.append('&');
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * generate hashed data
     * @param key
     * @param params
     * @return
     */
    public static String hashedData(String key, HashMap<String, String> params) {
        try {
            StringBuilder sb = new StringBuilder();
            List fieldNames = new ArrayList(params.keySet());
            Collections.sort(fieldNames);
            Iterator itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = (String) itr.next();
                String fieldValue = (String) params.get(fieldName);
                sb.append(fieldName).append('=').append(URLEncoder.encode(fieldValue,
                        StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    sb.append('&');
                }
            }
            String hashData = sb.toString();
            return hmacSHA512(key, hashData);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * hashed all key
     * @param key
     * @param fields
     * @return
     */
    public static String hashedAllFields(String key, HashMap<String, String> fields) {
        List fieldNames = new ArrayList(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return hmacSHA512(key,sb.toString());
    }

    /**
     * get ip address
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAdress;
        try {
            ipAdress = request.getHeader("X-FORWARDED-FOR");
            if (ipAdress == null) {
                ipAdress = request.getLocalAddr();
            }
        } catch (Exception e) {
            ipAdress = "Invalid IP:" + e.getMessage();
        }
        return ipAdress;
    }
}
