package caguioa.bank;

import java.sql.*;
import java.util.*;

public class LoanManager {

    private static void ensureLoanSchema(Connection conn) {
        // First, ensure the loans table exists
        ensureLoanTableExists(conn);
        ensureColumn(conn, "loans", "interest_rate", "ALTER TABLE loans ADD COLUMN interest_rate DOUBLE DEFAULT 0.02 AFTER amount");
        ensureColumn(conn, "loans", "total_payable", "ALTER TABLE loans ADD COLUMN total_payable DOUBLE AFTER interest_rate");
        ensureColumn(conn, "loans", "remaining_balance", "ALTER TABLE loans ADD COLUMN remaining_balance DOUBLE DEFAULT 0 AFTER total_payable");
        ensureColumn(conn, "loans", "due_date", "ALTER TABLE loans ADD COLUMN due_date DATE NULL AFTER remaining_balance");
        ensureColumn(conn, "loans", "status", "ALTER TABLE loans ADD COLUMN status VARCHAR(50) DEFAULT 'active' AFTER due_date");
        ensureColumn(conn, "loans", "witness_name", "ALTER TABLE loans ADD COLUMN witness_name VARCHAR(255) NULL AFTER status");
        ensureColumn(conn, "loans", "witness_contact", "ALTER TABLE loans ADD COLUMN witness_contact VARCHAR(255) NULL AFTER witness_name");
        ensureColumn(conn, "loans", "witness_signature", "ALTER TABLE loans ADD COLUMN witness_signature LONGBLOB NULL AFTER witness_contact");
        ensureColumn(conn, "loans", "user_signature", "ALTER TABLE loans ADD COLUMN user_signature LONGBLOB NULL AFTER witness_signature");
        ensureColumn(conn, "loans", "promissory_note_url", "ALTER TABLE loans ADD COLUMN promissory_note_url VARCHAR(1024) NULL AFTER user_signature");
        ensureColumn(conn, "loans", "is_account_blocked", "ALTER TABLE loans ADD COLUMN is_account_blocked BOOLEAN DEFAULT FALSE AFTER promissory_note_url");
        ensureColumn(conn, "loans", "blocked_date", "ALTER TABLE loans ADD COLUMN blocked_date TIMESTAMP NULL AFTER is_account_blocked");
        ensureColumn(conn, "loans", "created_at", "ALTER TABLE loans ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER blocked_date");
        
        // Ensure loan_payments table has transaction_id column
        ensureColumn(conn, "loan_payments", "transaction_id", "ALTER TABLE loan_payments ADD COLUMN transaction_id INT NULL AFTER transaction_reference");
        
        // Ensure loan_applications table has all required columns
        ensureColumn(conn, "loan_applications", "full_name", "ALTER TABLE loan_applications ADD COLUMN full_name VARCHAR(255) NULL AFTER rejection_reason");
        ensureColumn(conn, "loan_applications", "employment_status", "ALTER TABLE loan_applications ADD COLUMN employment_status VARCHAR(50) NULL AFTER full_name");
        ensureColumn(conn, "loan_applications", "monthly_income", "ALTER TABLE loan_applications ADD COLUMN monthly_income DOUBLE NULL AFTER employment_status");
        ensureColumn(conn, "loan_applications", "loan_term_months", "ALTER TABLE loan_applications ADD COLUMN loan_term_months INT NULL AFTER monthly_income");

        // Backfill remaining balance for old active rows after column migrations.
        try (PreparedStatement pst = conn.prepareStatement(
                "UPDATE loans SET remaining_balance = COALESCE(total_payable, amount, 0) " +
                "WHERE status = 'active' AND (remaining_balance IS NULL OR remaining_balance = 0)")) {
            pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error backfilling remaining_balance: " + e);
        }
    }
    
    private static void ensureLoanTableExists(Connection conn) {
        try (PreparedStatement pst = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS loans (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  amount DOUBLE NOT NULL," +
                "  interest_rate DOUBLE DEFAULT 0.02," +
                "  total_payable DOUBLE," +
                "  remaining_balance DOUBLE DEFAULT 0," +
                "  due_date DATE," +
                "  status VARCHAR(50) DEFAULT 'active'," +
                "  witness_name VARCHAR(255)," +
                "  witness_contact VARCHAR(255)," +
                "  witness_signature LONGBLOB," +
                "  user_signature LONGBLOB," +
                "  promissory_note_url VARCHAR(1024)," +
                "  is_account_blocked BOOLEAN DEFAULT FALSE," +
                "  blocked_date TIMESTAMP NULL," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB")) {
            pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error creating loans table: " + e);
        }
    }

    private static void ensureColumn(Connection conn, String table, String column, String alterSql) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet columns = metaData.getColumns(conn.getCatalog(), null, table, column)) {
                if (!columns.next()) {
                    try (PreparedStatement pst = conn.prepareStatement(alterSql)) {
                        pst.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error ensuring column " + table + "." + column + ": " + e);
        }
    }

    private static boolean usersHasEmailColumn(Connection conn) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet columns = metaData.getColumns(conn.getCatalog(), null, "users", "email")) {
                return columns.next();
            }
        } catch (Exception e) {
            System.out.println("Error checking users.email column: " + e);
            return false;
        }
    }
    
    /**
     * Get all active overdue loans
     */
    public static List<Map<String, Object>> getOverdueLoans() {
        List<Map<String, Object>> overdueLoans = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            ensureLoanSchema(conn);
            boolean hasEmailColumn = usersHasEmailColumn(conn);
            String query = "SELECT l.id, l.user_id, u.fullname, u.username, " +
                    (hasEmailColumn ? "u.email, " : "") +
                    "l.amount, l.total_payable, l.remaining_balance, l.due_date, " +
                    "l.is_account_blocked, u.balance FROM loans l " +
                    "JOIN users u ON l.user_id = u.id " +
                    "WHERE l.status = 'active' AND l.is_account_blocked = FALSE " +
                    "AND l.due_date < CURDATE() ORDER BY l.due_date ASC";
            
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> loan = new HashMap<>();
                loan.put("id", rs.getInt("id"));
                loan.put("user_id", rs.getInt("user_id"));
                loan.put("fullname", rs.getString("fullname"));
                loan.put("username", rs.getString("username"));
                loan.put("email", hasEmailColumn ? rs.getString("email") : null);
                loan.put("amount", rs.getDouble("amount"));
                loan.put("total_payable", rs.getDouble("total_payable"));
                loan.put("remaining_balance", rs.getDouble("remaining_balance"));
                loan.put("due_date", rs.getString("due_date"));
                loan.put("is_blocked", rs.getBoolean("is_account_blocked"));
                loan.put("balance", rs.getDouble("balance"));
                overdueLoans.add(loan);
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error fetching overdue loans: " + e);
        }
        return overdueLoans;
    }
    
    /**
     * Deactivate user account due to unpaid loan
     */
    public static boolean deactivateAccountForUnpaidLoan(int loanId, int userId) {
        try (Connection conn = DB.connect()) {
            ensureLoanSchema(conn);
            // Update loan status
            String updateLoanQuery = "UPDATE loans SET is_account_blocked = TRUE, " +
                    "blocked_date = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(updateLoanQuery);
            pst.setInt(1, loanId);
            pst.executeUpdate();
            pst.close();
            
            // Update user account status (set role to 'suspended')
            String updateUserQuery = "UPDATE users SET role = 'suspended' WHERE id = ?";
            pst = conn.prepareStatement(updateUserQuery);
            pst.setInt(1, userId);
            int result = pst.executeUpdate();
            pst.close();
            
            return result > 0;
        } catch (Exception e) {
            System.out.println("Error deactivating account: " + e);
            return false;
        }
    }
    
    /**
     * Reactivate user account after loan payment
     */
    public static boolean reactivateAccountAfterPayment(int loanId, int userId) {
        try (Connection conn = DB.connect()) {
            ensureLoanSchema(conn);
            // Update loan status
            String updateLoanQuery = "UPDATE loans SET is_account_blocked = FALSE WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(updateLoanQuery);
            pst.setInt(1, loanId);
            pst.executeUpdate();
            pst.close();
            
            // Update user account status (set role back to 'user')
            String updateUserQuery = "UPDATE users SET role = 'user' WHERE id = ?";
            pst = conn.prepareStatement(updateUserQuery);
            pst.setInt(1, userId);
            int result = pst.executeUpdate();
            pst.close();
            
            return result > 0;
        } catch (Exception e) {
            System.out.println("Error reactivating account: " + e);
            return false;
        }
    }
    
    /**
     * Get loan details with witness information
     */
    public static Map<String, Object> getLoanDetails(int loanId) {
        Map<String, Object> loanDetails = new HashMap<>();
        try (Connection conn = DB.connect()) {
            ensureLoanSchema(conn);
            boolean hasEmailColumn = usersHasEmailColumn(conn);
            String query = "SELECT l.id, l.user_id, u.fullname, u.username, " +
                    (hasEmailColumn ? "u.email, " : "") +
                    "l.amount, l.interest_rate, l.total_payable, l.remaining_balance, " +
                    "l.due_date, l.status, l.witness_name, l.witness_contact, " +
                    "l.promissory_note_url, l.is_account_blocked, l.created_at " +
                    "FROM loans l JOIN users u ON l.user_id = u.id WHERE l.id = ?";
            
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, loanId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                loanDetails.put("id", rs.getInt("id"));
                loanDetails.put("user_id", rs.getInt("user_id"));
                loanDetails.put("fullname", rs.getString("fullname"));
                loanDetails.put("username", rs.getString("username"));
                loanDetails.put("email", hasEmailColumn ? rs.getString("email") : null);
                loanDetails.put("amount", rs.getDouble("amount"));
                loanDetails.put("interest_rate", rs.getDouble("interest_rate"));
                loanDetails.put("total_payable", rs.getDouble("total_payable"));
                loanDetails.put("remaining_balance", rs.getDouble("remaining_balance"));
                loanDetails.put("due_date", rs.getString("due_date"));
                loanDetails.put("status", rs.getString("status"));
                loanDetails.put("witness_name", rs.getString("witness_name"));
                loanDetails.put("witness_contact", rs.getString("witness_contact"));
                loanDetails.put("promissory_note_url", rs.getString("promissory_note_url"));
                loanDetails.put("is_blocked", rs.getBoolean("is_account_blocked"));
                loanDetails.put("created_at", rs.getString("created_at"));
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error fetching loan details: " + e);
        }
        return loanDetails;
    }
    
    /**
     * Update witness information for a loan
     */
    public static boolean updateWitnessInfo(int loanId, String witnessName, String witnessContact) {
        try (Connection conn = DB.connect()) {
            ensureLoanSchema(conn);
            String query = "UPDATE loans SET witness_name = ?, witness_contact = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, witnessName);
            pst.setString(2, witnessContact);
            pst.setInt(3, loanId);
            int result = pst.executeUpdate();
            pst.close();
            return result > 0;
        } catch (Exception e) {
            System.out.println("Error updating witness info: " + e);
            return false;
        }
    }
    
    /**
     * Save signatures for loan (user and witness)
     */
    public static boolean saveSignatures(int loanId, byte[] userSignature, byte[] witnessSignature) {
        try (Connection conn = DB.connect()) {
            ensureLoanSchema(conn);
            String query = "UPDATE loans SET user_signature = ?, witness_signature = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setBytes(1, userSignature);
            pst.setBytes(2, witnessSignature);
            pst.setInt(3, loanId);
            int result = pst.executeUpdate();
            pst.close();
            return result > 0;
        } catch (Exception e) {
            System.out.println("Error saving signatures: " + e);
            return false;
        }
    }
    
    /**
     * Process loan payment and update remaining balance
     */
    public static boolean processLoanPayment(int loanId, double paymentAmount) {
        try (Connection conn = DB.connect()) {
            ensureLoanSchema(conn);
            // Get current remaining balance
            String selectQuery = "SELECT remaining_balance, user_id FROM loans WHERE id = ?";
            PreparedStatement selectPst = conn.prepareStatement(selectQuery);
            selectPst.setInt(1, loanId);
            ResultSet rs = selectPst.executeQuery();
            
            double remainingBalance = 0;
            int userId = 0;
            if (rs.next()) {
                remainingBalance = rs.getDouble("remaining_balance");
                userId = rs.getInt("user_id");
            }
            rs.close();
            selectPst.close();
            
            // Calculate new balance
            double newBalance = remainingBalance - paymentAmount;
            String status = newBalance <= 0 ? "paid" : "active";
            
            // Update loan
            String updateQuery = "UPDATE loans SET remaining_balance = ?, status = ? WHERE id = ?";
            PreparedStatement updatePst = conn.prepareStatement(updateQuery);
            updatePst.setDouble(1, Math.max(0, newBalance));
            updatePst.setString(2, status);
            updatePst.setInt(3, loanId);
            updatePst.executeUpdate();
            updatePst.close();
            
            // Reactivate account if loan is paid
            if (status.equals("paid")) {
                reactivateAccountAfterPayment(loanId, userId);
            }
            
            return true;
        } catch (Exception e) {
            System.out.println("Error processing loan payment: " + e);
            return false;
        }
    }
    
    /**
     * Get all loans for a specific user
     */
    public static List<Map<String, Object>> getUserLoans(int userId) {
        List<Map<String, Object>> userLoans = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            ensureLoanSchema(conn);
            String query = "SELECT id, amount, interest_rate, total_payable, remaining_balance, " +
                    "status, due_date, witness_name, is_account_blocked FROM loans " +
                    "WHERE user_id = ? ORDER BY created_at DESC";
            
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> loan = new HashMap<>();
                loan.put("id", rs.getInt("id"));
                loan.put("amount", rs.getDouble("amount"));
                loan.put("interest_rate", rs.getDouble("interest_rate"));
                loan.put("total_payable", rs.getDouble("total_payable"));
                loan.put("remaining_balance", rs.getDouble("remaining_balance"));
                loan.put("status", rs.getString("status"));
                loan.put("due_date", rs.getString("due_date"));
                loan.put("witness_name", rs.getString("witness_name"));
                loan.put("is_blocked", rs.getBoolean("is_account_blocked"));
                userLoans.add(loan);
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error fetching user loans: " + e);
        }
        return userLoans;
    }

    /**
     * Get the status string for a loan by its ID.
     */
    public static String getLoanStatus(int loanId) {
        try (Connection conn = DB.connect()) {
            ensureLoanSchema(conn);
            String query = "SELECT status FROM loans WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, loanId);
            ResultSet rs = pst.executeQuery();
            String status = null;
            if (rs.next()) {
                status = rs.getString("status");
            }
            rs.close();
            pst.close();
            return status;
        } catch (Exception e) {
            System.out.println("Error getting loan status: " + e);
            return null;
        }
    }

    /**
     * Update the status for a loan by its ID.
     */
    public static boolean setLoanStatus(int loanId, String status) {
        if (status == null) return false;
        try (Connection conn = DB.connect()) {
            ensureLoanSchema(conn);
            String query = "UPDATE loans SET status = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, status);
            pst.setInt(2, loanId);
            int updated = pst.executeUpdate();
            pst.close();
            return updated > 0;
        } catch (Exception e) {
            System.out.println("Error setting loan status: " + e);
            return false;
        }
    }

    
    /**
     * Get user's active loan
     */
    public static Map<String, Object> getUserActiveLoan(int userId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT id, amount, total_payable, remaining_balance, due_date, status FROM loans WHERE user_id = ? AND status = 'active' ORDER BY id DESC LIMIT 1";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> loan = new HashMap<>();
                        loan.put("id", rs.getInt("id"));
                        loan.put("amount", rs.getDouble("amount"));
                        loan.put("total_payable", rs.getDouble("total_payable"));
                        loan.put("remaining_balance", rs.getDouble("remaining_balance"));
                        loan.put("due_date", rs.getDate("due_date"));
                        loan.put("status", rs.getString("status"));
                        return loan;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting user active loan: " + e);
        }
        return null;
    }

}
