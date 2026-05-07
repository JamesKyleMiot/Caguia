package caguioa.bank;

import java.sql.*;

/**
 * Fix utility to add missing ID column to loan_applications table
 */
public class FixLoanApplicationsID {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║   FIXING loan_applications TABLE - ADDING ID COLUMN     ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        Connection con = DB.connect();
        if (con == null) {
            System.out.println("❌ Failed to connect to database");
            return;
        }

        try {
            System.out.println("⚙️  Adding ID column to loan_applications...\n");

            // Check if ID column already exists
            boolean idExists = columnExists(con, "loan_applications", "id");
            
            if (idExists) {
                System.out.println("✓ ID column already exists");
            } else {
                System.out.println("❌ ID column missing, adding it now...\n");
                
                // Add the ID column
                String addIdQuery = "ALTER TABLE loan_applications ADD COLUMN id INT AUTO_INCREMENT UNIQUE FIRST";
                try (Statement stmt = con.createStatement()) {
                    stmt.execute(addIdQuery);
                    System.out.println("✓ ID column added successfully\n");
                }
            }

            // Verify the fix
            System.out.println("📋 Verifying fix - First 5 columns:\n");
            String verifyQuery = "SELECT COLUMN_NAME, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                                "WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME='loan_applications' " +
                                "ORDER BY ORDINAL_POSITION LIMIT 5";
            
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(verifyQuery)) {
                while (rs.next()) {
                    System.out.println("  ✓ " + rs.getString("COLUMN_NAME") + " (" + rs.getString("COLUMN_TYPE") + ")");
                }
            }

            // Test the problematic query now
            System.out.println("\n🔍 Testing AdminDashboard query:\n");
            testAdminQuery(con);

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║   ✅ FIX COMPLETED SUCCESSFULLY                          ║");
            System.out.println("╚════════════════════════════════════════════════════════╝\n");

        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                System.err.println("Failed to close connection");
            }
        }
    }

    public static boolean columnExists(Connection con, String tableName, String columnName) throws SQLException {
        String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                      "WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME=? AND COLUMN_NAME=?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, tableName);
            pst.setString(2, columnName);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void testAdminQuery(Connection con) {
        try {
            String query = "SELECT la.id, la.user_id, la.full_name, la.requested_amount, la.purpose, " +
                          "la.employment_status, la.monthly_income, la.loan_term_months, la.created_at " +
                          "FROM loan_applications la WHERE la.status='pending' LIMIT 3";
            
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                int count = 0;
                while (rs.next() && count < 3) {
                    count++;
                    System.out.println("✓ Application #" + count);
                    System.out.println("   ID: " + rs.getInt("id"));
                    System.out.println("   User ID: " + rs.getInt("user_id"));
                    System.out.println("   Full Name: " + rs.getString("full_name"));
                    System.out.println("   Amount: " + rs.getDouble("requested_amount"));
                    System.out.println();
                }
                
                if (count == 0) {
                    System.out.println("ℹ  No pending applications to display");
                } else {
                    System.out.println("✅ Query successful! AdminDashboard should now work.");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Query Error: " + e.getMessage());
        }
    }
}
