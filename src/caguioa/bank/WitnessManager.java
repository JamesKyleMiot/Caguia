package caguioa.bank;

import java.sql.*;
import java.util.*;

/**
 * WitnessManager - Handles witness/guarantor operations for loans
 * Manages witness information, agreements, and responsibility tracking
 */
public class WitnessManager {
    
    /**
     * Add or update witness information for a loan
     */
    public static boolean setWitnessInfo(int loanId, String witnessName, String witnessContact) {
        try (Connection conn = DB.connect()) {
            String query = "UPDATE loans SET witness_name = ?, witness_contact = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, witnessName);
            pst.setString(2, witnessContact);
            pst.setInt(3, loanId);
            int result = pst.executeUpdate();
            pst.close();
            return result > 0;
        } catch (Exception e) {
            System.out.println("Error setting witness info: " + e);
            return false;
        }
    }
    
    /**
     * Get witness information for a loan
     */
    public static Map<String, String> getWitnessInfo(int loanId) {
        Map<String, String> witnessInfo = new HashMap<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT witness_name, witness_contact FROM loans WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, loanId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                witnessInfo.put("name", rs.getString("witness_name"));
                witnessInfo.put("contact", rs.getString("witness_contact"));
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error retrieving witness info: " + e);
        }
        return witnessInfo;
    }
    
    /**
     * Store user's signature (as image bytes)
     */
    public static boolean storeUserSignature(int loanId, byte[] signatureImage) {
        try (Connection conn = DB.connect()) {
            String query = "UPDATE loans SET user_signature = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setBytes(1, signatureImage);
            pst.setInt(2, loanId);
            int result = pst.executeUpdate();
            pst.close();
            return result > 0;
        } catch (Exception e) {
            System.out.println("Error storing user signature: " + e);
            return false;
        }
    }
    
    /**
     * Store witness's signature (as image bytes)
     */
    public static boolean storeWitnessSignature(int loanId, byte[] signatureImage) {
        try (Connection conn = DB.connect()) {
            String query = "UPDATE loans SET witness_signature = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setBytes(1, signatureImage);
            pst.setInt(2, loanId);
            int result = pst.executeUpdate();
            pst.close();
            return result > 0;
        } catch (Exception e) {
            System.out.println("Error storing witness signature: " + e);
            return false;
        }
    }
    
    /**
     * Retrieve user's signature
     */
    public static byte[] getUserSignature(int loanId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT user_signature FROM loans WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, loanId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                byte[] signature = rs.getBytes("user_signature");
                rs.close();
                pst.close();
                return signature;
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error retrieving user signature: " + e);
        }
        return null;
    }
    
    /**
     * Retrieve witness's signature
     */
    public static byte[] getWitnessSignature(int loanId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT witness_signature FROM loans WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, loanId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                byte[] signature = rs.getBytes("witness_signature");
                rs.close();
                pst.close();
                return signature;
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error retrieving witness signature: " + e);
        }
        return null;
    }
    
    /**
     * Check if both user and witness have signed
     */
    public static boolean bothPartiesSigned(int loanId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT user_signature IS NOT NULL AS user_signed, " +
                    "witness_signature IS NOT NULL AS witness_signed FROM loans WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setInt(1, loanId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                boolean userSigned = rs.getBoolean("user_signed");
                boolean witnessSigned = rs.getBoolean("witness_signed");
                rs.close();
                pst.close();
                return userSigned && witnessSigned;
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error checking signatures: " + e);
        }
        return false;
    }
    
    /**
     * Get all loans with witness information
     */
    public static List<Map<String, Object>> getLoansWithWitness() {
        List<Map<String, Object>> loansWithWitness = new ArrayList<>();
        try (Connection conn = DB.connect()) {
            String query = "SELECT l.id, l.user_id, u.fullname, u.username, " +
                    "l.amount, l.due_date, l.witness_name, l.witness_contact, " +
                    "CASE WHEN l.user_signature IS NOT NULL THEN 'YES' ELSE 'NO' END as user_signed, " +
                    "CASE WHEN l.witness_signature IS NOT NULL THEN 'YES' ELSE 'NO' END as witness_signed " +
                    "FROM loans l JOIN users u ON l.user_id = u.id " +
                    "WHERE l.witness_name IS NOT NULL ORDER BY l.created_at DESC";
            
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> loan = new HashMap<>();
                loan.put("id", rs.getInt("id"));
                loan.put("user_id", rs.getInt("user_id"));
                loan.put("fullname", rs.getString("fullname"));
                loan.put("username", rs.getString("username"));
                loan.put("amount", rs.getDouble("amount"));
                loan.put("due_date", rs.getString("due_date"));
                loan.put("witness_name", rs.getString("witness_name"));
                loan.put("witness_contact", rs.getString("witness_contact"));
                loan.put("user_signed", rs.getString("user_signed"));
                loan.put("witness_signed", rs.getString("witness_signed"));
                loansWithWitness.add(loan);
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("Error fetching loans with witness: " + e);
        }
        return loansWithWitness;
    }
    
    /**
     * Generate promissory note agreement text
     */
    public static String generatePromissoryNote(int loanId, String borrowerName, String borrowerAddress,
                                                  String witnessName, String witnessAddress,
                                                  double loanAmount, double interestRate, 
                                                  String dueDate) {
        return "PROMISSORY NOTE & LOAN AGREEMENT\n" +
                "=" + "=".repeat(60) + "\n\n" +
                "DATE: " + java.time.LocalDate.now() + "\n" +
                "LOAN ID: " + loanId + "\n\n" +
                
                "BORROWER INFORMATION:\n" +
                "Name: " + borrowerName + "\n" +
                "Address: " + borrowerAddress + "\n" +
                "Borrower Signature: ________________     Date: __________\n\n" +
                
                "WITNESS/GUARANTOR INFORMATION:\n" +
                "Name: " + witnessName + "\n" +
                "Address: " + witnessAddress + "\n" +
                "Witness Signature: _________________    Date: __________\n\n" +
                
                "LOAN TERMS:\n" +
                "Principal Amount: ₱" + String.format("%.2f", loanAmount) + "\n" +
                "Interest Rate: " + interestRate + "%\n" +
                "Total Payable: ₱" + String.format("%.2f", loanAmount * (1 + interestRate/100)) + "\n" +
                "Due Date: " + dueDate + "\n\n" +
                
                "TERMS & CONDITIONS:\n" +
                "1. The borrower promises to pay the principal amount plus interest by the due date.\n" +
                "2. The witness acts as a guarantor and is responsible for payment if borrower cannot pay.\n" +
                "3. Failure to pay by due date may result in account suspension and legal action.\n" +
                "4. Both parties have read and understood the terms of this agreement.\n" +
                "5. Both signatures are legally binding.\n\n" +
                
                "Caguioa Bank\n" +
                "Admin Signature: ___________________    Date: __________\n";
    }
}
