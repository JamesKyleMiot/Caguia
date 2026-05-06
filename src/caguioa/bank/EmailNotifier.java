package caguioa.bank;

import java.io.*;

/**
 * Email Notifier for PIN Reset OTP
 * Sends real emails via Gmail SMTP using PowerShell
 */
public class EmailNotifier {

    private static String SENDER_EMAIL = "your_email@gmail.com";
    private static String SENDER_PASSWORD = "your_app_password";
    private static boolean credentialsSet = false;

    /**
     * Set Gmail credentials for SMTP
     * @param email Your Gmail address
     * @param password Gmail App Password
     */
    public static void setSenderCredentials(String email, String password) {
        if (email != null && !email.trim().isEmpty()) {
            SENDER_EMAIL = email;
            credentialsSet = true;
        }
        if (password != null && !password.trim().isEmpty()) {
            SENDER_PASSWORD = password;
            credentialsSet = true;
        }
        if (credentialsSet) {
            System.out.println("✓ Gmail credentials configured: " + SENDER_EMAIL);
        }
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
            System.out.println("❌ ERROR: empty recipient for PIN Reset OTP");
            return false;
        }

        String subject = "🔐 Caguioa Bank - PIN Reset OTP";
        String body = "Dear " + userName + ",\n\n" +
                      "Your PIN Reset request has been received.\n\n" +
                      "============================================\n" +
                      "Your One-Time Password (OTP) is:\n" +
                      "\n" +
                      "     🔑 " + otp + "\n" +
                      "\n" +
                      "============================================\n\n" +
                      "⏱️ IMPORTANT: This OTP will expire in 10 minutes.\n\n" +
                      "📝 Steps to Reset Your PIN:\n" +
                      "1. Return to Caguioa Bank Application\n" +
                      "2. Enter the OTP in the verification dialog\n" +
                      "3. Create your new 6-digit PIN\n" +
                      "4. Confirm your new PIN\n" +
                      "5. Login with your new PIN\n\n" +
                      "⚠️ SECURITY NOTICE:\n" +
                      "- Never share this OTP with anyone\n" +
                      "- Caguioa Bank staff will never ask for your OTP\n" +
                      "- If you did not request this, contact support immediately\n\n" +
                      "Best regards,\n" +
                      "Caguioa Bank Security Team";

        return sendEmail(recipientEmail, subject, body);
    }

    /**
     * Core email sending method - sends via Gmail SMTP using PowerShell
     */
    private static boolean sendEmail(String recipientEmail, String subject, String body) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("📧 SENDING EMAIL VIA GMAIL SMTP");
        System.out.println("=".repeat(70));
        System.out.println("To: " + recipientEmail);
        System.out.println("From: " + SENDER_EMAIL);
        System.out.println("Subject: " + subject);
        System.out.println("-".repeat(70));
        System.out.println(body);
        System.out.println("=".repeat(70));

        // Check if credentials are configured
        if (!credentialsSet || SENDER_EMAIL.equals("your_email@gmail.com")) {
            System.out.println("\n⚠️ WARNING: Gmail credentials NOT configured!");
            System.out.println("   Email was NOT sent.");
            System.out.println("   To enable real email sending, configure credentials first:");
            System.out.println("   EmailNotifier.setSenderCredentials(\"your_email@gmail.com\", \"app_password\");");
            System.out.println("=".repeat(70) + "\n");
            return false;
        }

        // Try to send via PowerShell
        try {
            return sendViaGmailPowerShell(recipientEmail, subject, body);
        } catch (Exception e) {
            System.out.println("\n❌ ERROR sending email: " + e.getMessage());
            System.out.println("=".repeat(70) + "\n");
            return false;
        }
    }

    /**
     * Send email via PowerShell and Gmail SMTP
     */
    private static boolean sendViaGmailPowerShell(String recipientEmail, String subject, String body) throws Exception {
        // Escape special characters for PowerShell
        String escapedBody = body.replace("\"", "\\\"").replace("\n", "`n");
        String escapedSubject = subject.replace("\"", "\\\"");

        // PowerShell script to send email via Gmail SMTP
        String psScript = "$EmailFrom = \"" + SENDER_EMAIL + "\"\n" +
                         "$EmailTo = \"" + recipientEmail + "\"\n" +
                         "$Subject = \"" + escapedSubject + "\"\n" +
                         "$Body = @\"\n" + escapedBody + "\n\"@\n" +
                         "$SMTPServer = \"smtp.gmail.com\"\n" +
                         "$SMTPPort = 587\n" +
                         "$SMTPClient = New-Object Net.Mail.SmtpClient($SMTPServer, $SMTPPort)\n" +
                         "$SMTPClient.EnableSsl = $true\n" +
                         "$SMTPClient.Credentials = New-Object System.Net.NetworkCredential(\"" + SENDER_EMAIL + "\", \"" + SENDER_PASSWORD + "\")\n" +
                         "try {\n" +
                         "    $SMTPClient.Send($EmailFrom, $EmailTo, $Subject, $Body)\n" +
                         "    Write-Host \"Email sent successfully!\"\n" +
                         "} catch {\n" +
                         "    Write-Host \"Error: $_.Exception.Message\"\n" +
                         "    exit 1\n" +
                         "}\n";

        // Write script to temporary file
        String tempScriptPath = System.getProperty("java.io.tmpdir") + "send_email_" + System.currentTimeMillis() + ".ps1";
        try (FileWriter fw = new FileWriter(tempScriptPath)) {
            fw.write(psScript);
        }

        try {
            // Execute PowerShell
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-File", tempScriptPath
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            // Clean up temp file
            new File(tempScriptPath).delete();

            if (exitCode == 0) {
                System.out.println("\n✅ EMAIL SENT SUCCESSFULLY!");
                System.out.println("   Recipient: " + recipientEmail);
                System.out.println("=".repeat(70) + "\n");
                return true;
            } else {
                System.out.println("\n❌ SMTP Error: " + output.toString());
                System.out.println("=".repeat(70) + "\n");
                return false;
            }

        } catch (Exception e) {
            // Clean up temp file
            new File(tempScriptPath).delete();
            throw e;
        }
    }

    public static boolean sendLoanDueReminder(String recipientEmail, String userName,
                                               String loanAmount, String dueDate) {
        return logUnsupported("Loan Reminder", recipientEmail);
    }

    public static boolean sendAccountSuspensionWarning(String recipientEmail, String userName,
                                                        String loanAmount, String daysOverdue) {
        return logUnsupported("Account Suspension Warning", recipientEmail);
    }

    public static boolean sendAccountReactivationEmail(String recipientEmail, String userName,
                                                        String paidAmount) {
        return logUnsupported("Account Reactivation", recipientEmail);
    }

    public static boolean sendLoanCreationConfirmation(String recipientEmail, String userName,
                                                        String loanAmount, String interestRate,
                                                        String totalPayable, String dueDate,
                                                        String witnessName, String witnessContact) {
        return logUnsupported("Loan Confirmation", recipientEmail);
    }

    public static boolean sendPINResetApprovalNotification(String recipientEmail, String userName) {
        return logUnsupported("PIN Reset Approval", recipientEmail);
    }

    public static boolean sendPINResetDenialNotification(String recipientEmail, String userName, String reason) {
        return logUnsupported("PIN Reset Denial", recipientEmail);
    }

    private static boolean logUnsupported(String emailType, String recipientEmail) {
        System.out.println("⚠️ " + emailType + " - Not yet implemented");
        return false;
    }

    public static String testSmtpConnection() {
        if (!credentialsSet || SENDER_EMAIL.equals("your_email@gmail.com")) {
            return "❌ Gmail credentials NOT configured.";
        }
        return "✅ Gmail credentials configured. Ready to send emails.";
    }
}
