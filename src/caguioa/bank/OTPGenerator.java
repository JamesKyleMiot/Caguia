package caguioa.bank;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * OTP Generator and Validator for PIN Reset functionality
 */
public class OTPGenerator {
    
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 10;

    /**
     * Generate a random 6-digit OTP
     * @return 6-digit OTP as String
     */
    public static String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(RANDOM.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Validate OTP
     * @param generatedOTP The OTP generated and sent to user
     * @param enteredOTP The OTP entered by user
     * @return true if OTP matches
     */
    public static boolean validateOTP(String generatedOTP, String enteredOTP) {
        if (generatedOTP == null || enteredOTP == null) {
            return false;
        }
        return generatedOTP.equals(enteredOTP.trim());
    }

    /**
     * Check if OTP has expired
     * @param generatedTime The time when OTP was generated
     * @return true if OTP has expired
     */
    public static boolean isOTPExpired(LocalDateTime generatedTime) {
        if (generatedTime == null) {
            return true;
        }
        LocalDateTime expiryTime = generatedTime.plus(OTP_VALIDITY_MINUTES, ChronoUnit.MINUTES);
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Get OTP validity period in minutes
     * @return Validity period
     */
    public static int getOTPValidityMinutes() {
        return OTP_VALIDITY_MINUTES;
    }
}
