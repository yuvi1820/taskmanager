package com.taskmanager.util;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class OTPUtil {
    private static final SecureRandom random = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 10;

    public static String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public static Timestamp getOTPExpiration() {
        long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(OTP_EXPIRATION_MINUTES);
        return new Timestamp(expirationTime);
    }

    public static boolean isOTPExpired(Timestamp expiration) {
        return expiration.before(new Timestamp(System.currentTimeMillis()));
    }

    public static boolean validateOTP(String inputOTP, String storedOTP, Timestamp expiration) {
        if (storedOTP == null || expiration == null) {
            return false;
        }
        if (isOTPExpired(expiration)) {
            return false;
        }
        return inputOTP.equals(storedOTP);
    }
}
