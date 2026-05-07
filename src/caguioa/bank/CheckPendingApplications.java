package caguioa.bank;

import java.sql.*;

public class CheckPendingApplications {
    public static void main(String[] args) {
        try (Connection con = DB.connect()) {
            String query = "SELECT id, user_id, loan_amount, status FROM loan_applications ORDER BY id";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                System.out.println("\n📋 All Loan Applications:\n");
                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt(1) + " | User: " + rs.getInt(2) + 
                                     " | Amount: " + rs.getDouble(3) + " | Status: " + rs.getString(4));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
