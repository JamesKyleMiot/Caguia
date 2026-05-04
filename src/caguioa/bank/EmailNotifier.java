package caguioa.bank;

public class EmailNotifier {

    private static String SENDER_EMAIL = "your_email@gmail.com";
    private static String SENDER_PASSWORD = "your_app_password";

    public static void setSenderCredentials(String email, String password) {
        if (email != null && !email.trim().isEmpty()) {
            SENDER_EMAIL = email;
        }
        if (password != null && !password.trim().isEmpty()) {
            SENDER_PASSWORD = password;
        }
    }

    public static boolean sendLoanDueReminder(String recipientEmail, String userName,
                                               String loanAmount, String dueDate) {
        return logUnsupportedEmail(recipientEmail, "Loan reminder", userName, loanAmount, dueDate);
    }

    public static boolean sendAccountSuspensionWarning(String recipientEmail, String userName,
                                                        String loanAmount, String daysOverdue) {
        return logUnsupportedEmail(recipientEmail, "Suspension warning", userName, loanAmount, daysOverdue);
    }

    public static boolean sendAccountReactivationEmail(String recipientEmail, String userName,
                                                        String paidAmount) {
        return logUnsupportedEmail(recipientEmail, "Reactivation notice", userName, paidAmount, "");
    }

    public static boolean sendLoanCreationConfirmation(String recipientEmail, String userName,
                                                        String loanAmount, String interestRate,
                                                        String totalPayable, String dueDate,
                                                        String witnessName, String witnessContact) {
        return logUnsupportedEmail(recipientEmail, "Loan confirmation", userName, loanAmount, totalPayable);
    }

    /**
     * Send PIN Reset OTP to user email
     * @param recipientEmail User's email address
     * @param userName User's name
     * @param otp The OTP code to send
     * @return true if email sent successfully
     */
    public static boolean sendPINResetOTP(String recipientEmail, String userName, String otp) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            System.out.println("EmailNotifier: empty recipient for PIN Reset OTP");
            return false;
        }

        String subject = "Caguioa Bank - PIN Reset OTP";
        String body = "Dear " + userName + ",\n\n" +
                      "Your PIN Reset request has been approved.\n\n" +
                      "Your One-Time Password (OTP) is: " + otp + "\n\n" +
                      "This OTP will expire in 10 minutes.\n\n" +
                      "Please enter this OTP to verify your identity and reset your PIN.\n\n" +
                      "If you did not request this PIN reset, please contact the admin immediately.\n\n" +
                      "Best regards,\n" +
                      "Caguioa Bank Administration";

        System.out.println("EmailNotifier: PIN Reset OTP Email");
        System.out.println("Recipient: " + recipientEmail);
        System.out.println("Sender: " + SENDER_EMAIL);
        System.out.println("Subject: " + subject);
        System.out.println("OTP: " + otp);
        System.out.println("Note: To enable actual email sending, add javax.mail / Jakarta Mail to the project libraries.");
        return false;
    }

    /**
     * Send PIN Reset Approval Notification
     * @param recipientEmail User's email address
     * @param userName User's name
     * @return true if email sent successfully
     */
    public static boolean sendPINResetApprovalNotification(String recipientEmail, String userName) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            System.out.println("EmailNotifier: empty recipient for PIN Reset Approval");
            return false;
        }

        String subject = "Caguioa Bank - PIN Reset Request Approved";
        String body = "Dear " + userName + ",\n\n" +
                      "Your PIN Reset request has been approved by our administration.\n\n" +
                      "Please login to your account to receive your OTP and complete the PIN reset process.\n\n" +
                      "If you have any questions, please contact our support team.\n\n" +
                      "Best regards,\n" +
                      "Caguioa Bank Administration";

        System.out.println("EmailNotifier: PIN Reset Approval Notification");
        System.out.println("Recipient: " + recipientEmail);
        System.out.println("Sender: " + SENDER_EMAIL);
        System.out.println("Subject: " + subject);
        System.out.println("Note: To enable actual email sending, add javax.mail / Jakarta Mail to the project libraries.");
        return false;
    }

    /**
     * Send PIN Reset Denial Notification
     * @param recipientEmail User's email address
     * @param userName User's name
     * @param reason Reason for denial
     * @return true if email sent successfully
     */
    public static boolean sendPINResetDenialNotification(String recipientEmail, String userName, String reason) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            System.out.println("EmailNotifier: empty recipient for PIN Reset Denial");
            return false;
        }

        String subject = "Caguioa Bank - PIN Reset Request Status";
        String body = "Dear " + userName + ",\n\n" +
                      "Your PIN Reset request has been reviewed.\n\n" +
                      "Status: DENIED\n" +
                      "Reason: " + reason + "\n\n" +
                      "If you believe this is an error or need further assistance, please contact our support team.\n\n" +
                      "Best regards,\n" +
                      "Caguioa Bank Administration";

        System.out.println("EmailNotifier: PIN Reset Denial Notification");
        System.out.println("Recipient: " + recipientEmail);
        System.out.println("Sender: " + SENDER_EMAIL);
        System.out.println("Subject: " + subject);
        System.out.println("Note: To enable actual email sending, add javax.mail / Jakarta Mail to the project libraries.");
        return false;
    }

    public static String testSmtpConnection() {
        return "Email sending is disabled because javax.mail is not on the project classpath. "
            + "Add a Mail library to enable EmailNotifier. Current sender email is " + SENDER_EMAIL + ".";
    }

    private static boolean logUnsupportedEmail(String recipientEmail, String label, String value1, String value2, String value3) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            System.out.println("EmailNotifier: empty recipient for " + label);
            return false;
        }

        System.out.println("EmailNotifier: " + label + " not sent because the Mail library is missing.");
        System.out.println("Recipient: " + recipientEmail);
        System.out.println("Sender: " + SENDER_EMAIL);
        if (value1 != null && !value1.isEmpty()) System.out.println("Value1: " + value1);
        if (value2 != null && !value2.isEmpty()) System.out.println("Value2: " + value2);
        if (value3 != null && !value3.isEmpty()) System.out.println("Value3: " + value3);
        System.out.println("To enable actual email sending, add javax.mail / Jakarta Mail to the project libraries.");
        return false;
    }
}
