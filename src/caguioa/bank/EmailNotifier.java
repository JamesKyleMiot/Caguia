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
