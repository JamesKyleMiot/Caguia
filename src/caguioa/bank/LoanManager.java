package caguioa.bank;

import java.sql.*;
import java.util.*;

public class LoanManager {

    public static void ensureLoanSystemSchema() {
        try (Connection conn = DB.connect()) {
            if (conn != null) {
                ensureLoanSchema(conn);
            }
        } catch (Exception e) {
            System.out.println("Error ensuring loan system schema: " + e);
        }
    }

    private static void ensureLoanSchema(Connection conn) {
        // First, ensure the loans table exists
        ensureLoanTableExists(conn);
        ensureLoanApplicationsTableExists(conn);
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
        ensureColumn(conn, "loan_applications", "requested_amount", "ALTER TABLE loan_applications ADD COLUMN requested_amount DOUBLE NULL AFTER user_id");
        ensureColumn(conn, "loan_applications", "loan_amount", "ALTER TABLE loan_applications ADD COLUMN loan_amount DOUBLE NULL AFTER requested_amount");
        ensureColumn(conn, "loan_applications", "purpose", "ALTER TABLE loan_applications ADD COLUMN purpose VARCHAR(255) NULL AFTER requested_amount");
        ensureColumn(conn, "loan_applications", "full_name", "ALTER TABLE loan_applications ADD COLUMN full_name VARCHAR(255) NULL AFTER rejection_reason");
        ensureColumn(conn, "loan_applications", "date_of_birth", "ALTER TABLE loan_applications ADD COLUMN date_of_birth DATE NULL AFTER full_name");
        ensureColumn(conn, "loan_applications", "gender", "ALTER TABLE loan_applications ADD COLUMN gender VARCHAR(20) NULL AFTER date_of_birth");
        ensureColumn(conn, "loan_applications", "address", "ALTER TABLE loan_applications ADD COLUMN address VARCHAR(255) NULL AFTER gender");
        ensureColumn(conn, "loan_applications", "contact_number", "ALTER TABLE loan_applications ADD COLUMN contact_number VARCHAR(30) NULL AFTER address");
        ensureColumn(conn, "loan_applications", "email_address", "ALTER TABLE loan_applications ADD COLUMN email_address VARCHAR(255) NULL AFTER contact_number");
        ensureColumn(conn, "loan_applications", "employment_status", "ALTER TABLE loan_applications ADD COLUMN employment_status VARCHAR(50) NULL AFTER full_name");
        ensureColumn(conn, "loan_applications", "company_name", "ALTER TABLE loan_applications ADD COLUMN company_name VARCHAR(255) NULL AFTER employment_status");
        ensureColumn(conn, "loan_applications", "monthly_income", "ALTER TABLE loan_applications ADD COLUMN monthly_income DOUBLE NULL AFTER employment_status");
        ensureColumn(conn, "loan_applications", "work_address", "ALTER TABLE loan_applications ADD COLUMN work_address VARCHAR(255) NULL AFTER monthly_income");
        ensureColumn(conn, "loan_applications", "loan_amount_requested", "ALTER TABLE loan_applications ADD COLUMN loan_amount_requested DOUBLE NULL AFTER work_address");
        ensureColumn(conn, "loan_applications", "loan_purpose", "ALTER TABLE loan_applications ADD COLUMN loan_purpose VARCHAR(255) NULL AFTER loan_amount_requested");
        ensureColumn(conn, "loan_applications", "loan_term_months", "ALTER TABLE loan_applications ADD COLUMN loan_term_months INT NULL AFTER monthly_income");
        ensureColumn(conn, "loan_applications", "account_number", "ALTER TABLE loan_applications ADD COLUMN account_number VARCHAR(100) NULL AFTER loan_term_months");
        ensureColumn(conn, "loan_applications", "account_type", "ALTER TABLE loan_applications ADD COLUMN account_type VARCHAR(100) NULL AFTER account_number");
        ensureColumn(conn, "loan_applications", "valid_id_submitted", "ALTER TABLE loan_applications ADD COLUMN valid_id_submitted BOOLEAN DEFAULT FALSE AFTER account_type");
        ensureColumn(conn, "loan_applications", "proof_of_income_submitted", "ALTER TABLE loan_applications ADD COLUMN proof_of_income_submitted BOOLEAN DEFAULT FALSE AFTER valid_id_submitted");
        ensureColumn(conn, "loan_applications", "proof_of_address_submitted", "ALTER TABLE loan_applications ADD COLUMN proof_of_address_submitted BOOLEAN DEFAULT FALSE AFTER proof_of_income_submitted");
        ensureColumn(conn, "loan_applications", "declaration_accepted", "ALTER TABLE loan_applications ADD COLUMN declaration_accepted BOOLEAN DEFAULT TRUE AFTER proof_of_address_submitted");

        // Keep old/new amount/purpose columns in sync for compatibility.
        try (PreparedStatement pst = conn.prepareStatement(
                "UPDATE loan_applications SET " +
                "loan_amount = COALESCE(loan_amount, loan_amount_requested, requested_amount), " +
                "loan_amount_requested = COALESCE(loan_amount_requested, requested_amount), " +
                "requested_amount = COALESCE(requested_amount, loan_amount_requested), " +
                "loan_purpose = COALESCE(loan_purpose, purpose), " +
                "purpose = COALESCE(purpose, loan_purpose) " +
                "WHERE loan_amount IS NULL OR loan_amount_requested IS NULL OR requested_amount IS NULL OR loan_purpose IS NULL OR purpose IS NULL")) {
            pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error syncing loan application compatibility columns: " + e);
        }

        // Backfill remaining balance for old active rows after column migrations.
        try (PreparedStatement pst = conn.prepareStatement(
                "UPDATE loans SET remaining_balance = COALESCE(total_payable, amount, 0) " +
                "WHERE status = 'active' AND (remaining_balance IS NULL OR remaining_balance = 0)")) {
            pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error backfilling remaining_balance: " + e);
        }
    }

    private static void ensureLoanApplicationsTableExists(Connection conn) {
        try (PreparedStatement pst = conn.prepareStatement(
                "CREATE TABLE IF NOT EXISTS loan_applications (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  requested_amount DOUBLE," +
                "  loan_amount DOUBLE," +
                "  purpose VARCHAR(255)," +
                "  status VARCHAR(50) DEFAULT 'pending'," +
                "  admin_id INT," +
                "  admin_notes VARCHAR(255)," +
                "  approved_amount DOUBLE," +
                "  rejection_reason VARCHAR(255)," +
                "  full_name VARCHAR(255)," +
                "  date_of_birth DATE," +
                "  gender VARCHAR(20)," +
                "  address VARCHAR(255)," +
                "  contact_number VARCHAR(30)," +
                "  email_address VARCHAR(255)," +
                "  employment_status VARCHAR(100)," +
                "  company_name VARCHAR(255)," +
                "  monthly_income DOUBLE," +
                "  work_address VARCHAR(255)," +
                "  loan_amount_requested DOUBLE," +
                "  loan_purpose VARCHAR(255)," +
                "  loan_term_months INT," +
                "  account_number VARCHAR(100)," +
                "  account_type VARCHAR(100)," +
                "  valid_id_submitted BOOLEAN DEFAULT FALSE," +
                "  proof_of_income_submitted BOOLEAN DEFAULT FALSE," +
                "  proof_of_address_submitted BOOLEAN DEFAULT FALSE," +
                "  declaration_accepted BOOLEAN DEFAULT TRUE," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  reviewed_at TIMESTAMP NULL," +
                "  expires_at TIMESTAMP NULL," +
                "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB")) {
            pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error creating loan_applications table: " + e);
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
        try (Connection conn = DB.connect();
             PreparedStatement selectPst = conn.prepareStatement(
                 "SELECT user_id FROM loans WHERE id = ?")) {
            ensureLoanSchema(conn);

            selectPst.setInt(1, loanId);
            try (ResultSet rs = selectPst.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                int userId = rs.getInt("user_id");
                return processLoanPayment(loanId, userId, paymentAmount, "Loan Payment", null) > 0;
            }
        } catch (Exception e) {
            System.out.println("Error processing loan payment: " + e);
            return false;
        }
    }

    /**
     * Process loan payment, store the loan payment row, and record the matching transaction.
     * Returns the inserted loan_payment ID, or -1 on failure.
     */
    public static int processLoanPayment(int loanId, int userId, double paymentAmount,
                                         String paymentMethod, String transactionReference) {
        if (paymentAmount <= 0) {
            return -1;
        }

        Connection conn = null;
        try {
            conn = DB.connect();
            ensureLoanSchema(conn);
            conn.setAutoCommit(false);

            int loanUserId = 0;
            double remainingBalance = 0;

            try (PreparedStatement selectPst = conn.prepareStatement(
                    "SELECT user_id, remaining_balance FROM loans WHERE id = ? FOR UPDATE")) {
                selectPst.setInt(1, loanId);
                try (ResultSet rs = selectPst.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return -1;
                    }

                    loanUserId = rs.getInt("user_id");
                    remainingBalance = rs.getDouble("remaining_balance");
                }
            }

            if (userId > 0 && loanUserId != userId) {
                conn.rollback();
                return -1;
            }

            double appliedAmount = Math.min(paymentAmount, remainingBalance);
            if (appliedAmount <= 0) {
                conn.rollback();
                return -1;
            }

            double newBalance = Math.max(0, remainingBalance - appliedAmount);
            // Always set status to 'paid' if fully paid, otherwise 'active'
            String status = newBalance <= 0 ? "paid" : "active";
            String normalizedMethod = normalizePaymentMethod(paymentMethod);
            String normalizedReference = normalizeTransactionReference(transactionReference, loanId);

            int paymentId;
            try (PreparedStatement paymentStmt = conn.prepareStatement(
                    "INSERT INTO loan_payments (loan_id, user_id, payment_amount, payment_method, payment_status, transaction_reference, notes) " +
                    "VALUES (?, ?, ?, ?, 'completed', ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                paymentStmt.setInt(1, loanId);
                paymentStmt.setInt(2, loanUserId);
                paymentStmt.setDouble(3, appliedAmount);
                paymentStmt.setString(4, normalizedMethod);
                paymentStmt.setString(5, normalizedReference);
                paymentStmt.setString(6, "Recorded via loan payment flow");
                paymentStmt.executeUpdate();

                try (ResultSet generatedKeys = paymentStmt.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        conn.rollback();
                        return -1;
                    }
                    paymentId = generatedKeys.getInt(1);
                }
            }

            int transactionId;
            try (PreparedStatement transactionStmt = conn.prepareStatement(
                    "INSERT INTO transactions (user_id, type, amount, method) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                transactionStmt.setInt(1, loanUserId);
                transactionStmt.setString(2, "Loan Payment");
                transactionStmt.setDouble(3, appliedAmount);
                transactionStmt.setString(4, normalizedMethod);
                transactionStmt.executeUpdate();

                try (ResultSet generatedKeys = transactionStmt.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        conn.rollback();
                        return -1;
                    }
                    transactionId = generatedKeys.getInt(1);
                }
            }

            try (PreparedStatement linkStmt = conn.prepareStatement(
                    "UPDATE loan_payments SET transaction_id = ? WHERE id = ?")) {
                linkStmt.setInt(1, transactionId);
                linkStmt.setInt(2, paymentId);
                linkStmt.executeUpdate();
            }

            try (PreparedStatement updateLoanStmt = conn.prepareStatement(
                    "UPDATE loans SET remaining_balance = ?, status = ?, is_account_blocked = CASE WHEN ? <= 0 THEN FALSE ELSE is_account_blocked END WHERE id = ?")) {
                updateLoanStmt.setDouble(1, newBalance);
                updateLoanStmt.setString(2, status);
                updateLoanStmt.setDouble(3, newBalance);
                updateLoanStmt.setInt(4, loanId);
                updateLoanStmt.executeUpdate();
            }

            if (newBalance <= 0) {
                try (PreparedStatement userStmt = conn.prepareStatement(
                        "UPDATE users SET role = 'user' WHERE id = ?")) {
                    userStmt.setInt(1, loanUserId);
                    userStmt.executeUpdate();
                }

                try (PreparedStatement unblockStmt = conn.prepareStatement(
                        "UPDATE loans SET is_account_blocked = FALSE WHERE id = ?")) {
                    unblockStmt.setInt(1, loanId);
                    unblockStmt.executeUpdate();
                }
            }

            conn.commit();
            return paymentId;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ignored) {
                }
            }
            System.out.println("Error processing loan payment: " + e);
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            return "Loan Payment";
        }

        String method = paymentMethod.trim();
        if (method.contains("Online Banking")) return "Online Banking";
        if (method.contains("Mobile App")) return "Mobile App";
        if (method.contains("Bank Counter")) return "Bank Counter";
        if (method.contains("Payment Center")) return "Payment Center";
        if (method.contains("Auto-debit")) return "Auto-debit";
        if (method.contains("ATM")) return "ATM";
        return method.replaceAll("^[^A-Za-z0-9]+", "").trim();
    }

    private static String normalizeTransactionReference(String transactionReference, int loanId) {
        if (transactionReference != null && !transactionReference.trim().isEmpty()) {
            return transactionReference.trim();
        }

        return "LOAN-" + loanId + "-" + System.currentTimeMillis();
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
