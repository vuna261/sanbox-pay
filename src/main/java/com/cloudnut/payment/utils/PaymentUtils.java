package com.cloudnut.payment.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentUtils {
    private PaymentUtils() {}

    public enum PROMO_TYPE {
        Percent,
        Claim
    }

    public enum BILL_STATUS {
        Initial,
        Pending,
        Success,
        Failure,
        Refund,
        Cancel
    }

    public enum REFUND_METHOD {
        Manual,
        Vnpay
    }

    public enum ACTION {
        active,
        de_active
    }

    public enum TRAN_METHOD {
        Recharge,
        Pay,
        Refund
    }

    public enum TRAN_STATUS {
        Successful,
        Failure
    }

    public enum PAYMENT_METHOD {
        CODE,
        VNPAY
    }

    public enum WALLET_TYPE {
        PAYG("Pay as you go"),
        PREMIUM("Premium");

        private static final Map<String, WALLET_TYPE> BY_NAME = new HashMap<>();
        public final String name;

        WALLET_TYPE(String name) {
            this.name = name;
        }

        static {
            for (WALLET_TYPE e : values()) {
                BY_NAME.put(e.name, e);
            }
        }

        public static WALLET_TYPE getByName(String name) {
            return BY_NAME.get(name);
        }
    }

    /**
     * generate pay_code
     * @param transactionId
     * @return
     */
    public static String generatePayCode(String transactionId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            Date date = new Date();
            Long timeStamp = date.getTime();
            md.update(Constants.CLOUD_NUT_SALT.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md.digest((transactionId + timeStamp).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    /**
     * format input to UTC+7
     * @param date
     * @return
     */
    public static Date formatUTC(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, -7);
        return calendar.getTime();
    }
}