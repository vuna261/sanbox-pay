package com.cloudnut.payment.utils;

import java.util.HashMap;
import java.util.Map;

public class UserUtils {
    private UserUtils() {}

    public static String ROLE_ADMIN = "ADMIN";
    public static String ROLE_TRAINER = "TRAINER";


    public enum ROLE {
        ADMIN("ROLE_ADMIN"),
        TRAINER("ROLE_TRAINER"),
        TRAINEE("ROLE_TRAINEE");

        private static final Map<String, ROLE> BY_CODE = new HashMap<>();

        public final String grantName;

        ROLE(String grantName) {
            this.grantName = grantName;
        }

        static {
            for (ROLE e : values()) {
                BY_CODE.put(e.grantName, e);
            }
        }

        public static ROLE getByGrant(String grantName) {
            return BY_CODE.get(grantName);
        }

        public String grantType() {
            return this.grantName;
        }
    }
}
