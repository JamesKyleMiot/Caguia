package caguioa.bank;

/**
 * Test utility to save bank account information for user
 */
public class SaveBankAccountForUser {

    public static void main(String[] args) {
        // Save bank account for user ID 11 (miot@gmail.com)
        int userId = 11;
        String accountNumber = "100234567890";  // Generated based on user ID
        String accountHolderName = "JamesKyle Miot";
        String bankName = "Caguioa Bank";
        String accountType = "Savings";
        String branchCode = "CJK001";

        System.out.println("\n========== SAVE BANK ACCOUNT ==========");
        System.out.println("User ID: " + userId);
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Account Holder: " + accountHolderName);
        System.out.println("Bank: " + bankName);
        System.out.println("=========================================\n");

        // Ensure table exists
        UserBankAccountManager.ensureTableExists();

        // Save bank account
        boolean saved = UserBankAccountManager.saveBankAccount(userId, accountNumber, accountHolderName, bankName, accountType, branchCode);
        
        if (saved) {
            System.out.println("✓ Bank account saved successfully!\n");
            
            // Retrieve and display
            var account = UserBankAccountManager.getUserBankAccount(userId);
            if (account != null) {
                System.out.println("Retrieved Bank Account:");
                System.out.println("  - Account Number: " + account.get("account_number"));
                System.out.println("  - Account Holder: " + account.get("account_holder_name"));
                System.out.println("  - Bank: " + account.get("bank_name"));
                System.out.println("  - Type: " + account.get("account_type"));
                System.out.println("  - Branch Code: " + account.get("branch_code"));
                System.out.println("  - Verified: " + account.get("verified"));
            }
        } else {
            System.out.println("❌ Failed to save bank account");
        }
        
        System.out.println("\n=========================================\n");
    }
}
