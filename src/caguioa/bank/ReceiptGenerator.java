package caguioa.bank;

import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Generates payment receipts for loan payments
 */
public class ReceiptGenerator {

    /**
     * Generate a receipt for a loan payment
     * @param paymentId Payment ID
     * @param loanId Loan ID
     * @param userId User ID
     * @return Receipt ID, or -1 if failed
     */
    public static int generateReceipt(int paymentId, int loanId, int userId) {
        try (Connection conn = DB.connect();
             CallableStatement stmt = conn.prepareCall("{? = call generate_loan_receipt(?, ?, ?)}")) {
            
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, paymentId);
            stmt.setInt(3, loanId);
            stmt.setInt(4, userId);
            stmt.execute();
            
            int receiptId = stmt.getInt(1);
            System.out.println("✓ Receipt generated: ID=" + receiptId);
            return receiptId;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Get receipt details
     * @param receiptId Receipt ID
     * @return Receipt information
     */
    public static Map<String, Object> getReceiptDetails(int receiptId) {
        String query = "SELECT id, payment_id, loan_id, user_id, receipt_number, amount_paid, " +
                       "previous_balance, new_balance, payment_method, receipt_details, generated_at " +
                       "FROM loan_receipts WHERE id = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, receiptId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> receipt = new HashMap<>();
                    receipt.put("id", rs.getInt("id"));
                    receipt.put("payment_id", rs.getInt("payment_id"));
                    receipt.put("loan_id", rs.getInt("loan_id"));
                    receipt.put("user_id", rs.getInt("user_id"));
                    receipt.put("receipt_number", rs.getString("receipt_number"));
                    receipt.put("amount_paid", rs.getDouble("amount_paid"));
                    receipt.put("previous_balance", rs.getDouble("previous_balance"));
                    receipt.put("new_balance", rs.getDouble("new_balance"));
                    receipt.put("payment_method", rs.getString("payment_method"));
                    receipt.put("receipt_details", rs.getString("receipt_details"));
                    receipt.put("generated_at", rs.getTimestamp("generated_at"));
                    return receipt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Format receipt as printable text
     * @param receiptId Receipt ID
     * @return Formatted receipt text
     */
    public static String formatReceiptAsText(int receiptId) {
        Map<String, Object> receipt = getReceiptDetails(receiptId);
        if (receipt == null) return "Receipt not found";
        
        DecimalFormat df = new DecimalFormat("0.00");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════╗\n");
        sb.append("║       CAGUIOA BANK LOAN RECEIPT       ║\n");
        sb.append("╚════════════════════════════════════════╝\n\n");
        
        sb.append("Receipt #: ").append(receipt.get("receipt_number")).append("\n");
        sb.append("Date: ").append(sdf.format((Timestamp) receipt.get("generated_at"))).append("\n");
        sb.append("─────────────────────────────────────────\n");
        
        sb.append("Loan ID: ").append(receipt.get("loan_id")).append("\n");
        sb.append("Amount Paid: PHP ").append(df.format(receipt.get("amount_paid"))).append("\n");
        sb.append("Payment Method: ").append(receipt.get("payment_method")).append("\n");
        sb.append("─────────────────────────────────────────\n");
        
        sb.append("Previous Balance: PHP ").append(df.format(receipt.get("previous_balance"))).append("\n");
        sb.append("New Balance: PHP ").append(df.format(receipt.get("new_balance"))).append("\n");
        sb.append("─────────────────────────────────────────\n");
        
        double newBalance = (double) receipt.get("new_balance");
        if (newBalance <= 0) {
            sb.append("Status: ✓ LOAN FULLY PAID\n");
        } else {
            sb.append("Remaining Balance: PHP ").append(df.format(newBalance)).append("\n");
        }
        
        sb.append("\nThank you for your payment!\n");
        sb.append("For inquiries: Caguioa Bank Customer Service\n");
        sb.append("╚════════════════════════════════════════╝\n");
        
        return sb.toString();
    }

    /**
     * Get all receipts for a user
     * @param userId User ID
     * @return List of receipts
     */
    public static List<Map<String, Object>> getUserReceipts(int userId) {
        List<Map<String, Object>> receipts = new ArrayList<>();
        String query = "SELECT id, receipt_number, loan_id, amount_paid, payment_method, " +
                       "new_balance, generated_at FROM loan_receipts WHERE user_id = ? " +
                       "ORDER BY generated_at DESC";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> receipt = new HashMap<>();
                    receipt.put("id", rs.getInt("id"));
                    receipt.put("receipt_number", rs.getString("receipt_number"));
                    receipt.put("loan_id", rs.getInt("loan_id"));
                    receipt.put("amount_paid", rs.getDouble("amount_paid"));
                    receipt.put("payment_method", rs.getString("payment_method"));
                    receipt.put("new_balance", rs.getDouble("new_balance"));
                    receipt.put("generated_at", rs.getTimestamp("generated_at"));
                    receipts.add(receipt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return receipts;
    }

    /**
     * Get receipt by receipt number
     * @param receiptNumber Receipt number (e.g., RECEIPT-20260506-12345)
     * @return Receipt information
     */
    public static Map<String, Object> getReceiptByNumber(String receiptNumber) {
        String query = "SELECT id, receipt_number, loan_id, user_id, amount_paid, " +
                       "previous_balance, new_balance, payment_method, generated_at " +
                       "FROM loan_receipts WHERE receipt_number = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, receiptNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> receipt = new HashMap<>();
                    receipt.put("id", rs.getInt("id"));
                    receipt.put("receipt_number", rs.getString("receipt_number"));
                    receipt.put("loan_id", rs.getInt("loan_id"));
                    receipt.put("user_id", rs.getInt("user_id"));
                    receipt.put("amount_paid", rs.getDouble("amount_paid"));
                    receipt.put("previous_balance", rs.getDouble("previous_balance"));
                    receipt.put("new_balance", rs.getDouble("new_balance"));
                    receipt.put("payment_method", rs.getString("payment_method"));
                    receipt.put("generated_at", rs.getTimestamp("generated_at"));
                    return receipt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create HTML receipt for web display or printing
     * @param receiptId Receipt ID
     * @return HTML receipt string
     */
    public static String formatReceiptAsHTML(int receiptId) {
        Map<String, Object> receipt = getReceiptDetails(receiptId);
        if (receipt == null) return "<p>Receipt not found</p>";
        
        DecimalFormat df = new DecimalFormat("0.00");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        double newBalance = (double) receipt.get("new_balance");
        String statusColor = newBalance <= 0 ? "green" : "orange";
        String statusText = newBalance <= 0 ? "LOAN FULLY PAID ✓" : "Remaining Balance";
        
        return "<html><body style='font-family: Arial; margin: 20px;'>" +
               "<div style='border: 2px solid #333; padding: 20px; max-width: 600px;'>" +
               "<h2 style='text-align: center;'>CAGUIOA BANK - LOAN RECEIPT</h2>" +
               "<hr>" +
               "<p><strong>Receipt #:</strong> " + receipt.get("receipt_number") + "</p>" +
               "<p><strong>Date:</strong> " + sdf.format((Timestamp) receipt.get("generated_at")) + "</p>" +
               "<hr>" +
               "<p><strong>Loan ID:</strong> " + receipt.get("loan_id") + "</p>" +
               "<p><strong>Amount Paid:</strong> PHP " + df.format(receipt.get("amount_paid")) + "</p>" +
               "<p><strong>Payment Method:</strong> " + receipt.get("payment_method") + "</p>" +
               "<hr>" +
               "<p><strong>Previous Balance:</strong> PHP " + df.format(receipt.get("previous_balance")) + "</p>" +
               "<p><strong>" + statusText + ":</strong> <span style='color: " + statusColor + ";'>" +
               "PHP " + df.format(receipt.get("new_balance")) + "</span></p>" +
               "<hr>" +
               "<p style='text-align: center;'><em>Thank you for your payment!</em></p>" +
               "</div></body></html>";
    }
}
