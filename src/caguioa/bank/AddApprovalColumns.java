package caguioa.bank;

import java.sql.*;

/**
 * Add missing approval workflow columns to loan_applications table
 */
public class AddApprovalColumns {
    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║   ADDING APPROVAL COLUMNS TO loan_applications           ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        try (Connection con = DB.connect()) {
            String[] alterStatements = {
                "ALTER TABLE loan_applications ADD COLUMN admin_id INT NULL AFTER status",
                "ALTER TABLE loan_applications ADD COLUMN admin_comments VARCHAR(500) NULL AFTER admin_id",
                "ALTER TABLE loan_applications ADD COLUMN approved_at TIMESTAMP NULL AFTER admin_comments",
                "ALTER TABLE loan_applications ADD COLUMN rejected_at TIMESTAMP NULL AFTER approved_at",
                "ALTER TABLE loan_applications ADD COLUMN rejection_reason VARCHAR(500) NULL AFTER rejected_at",
                "ALTER TABLE loan_applications ADD COLUMN approved_amount DECIMAL(15,2) DEFAULT 0 AFTER rejection_reason"
            };

            System.out.println("⚙️  Adding columns...\n");
            
            for (String sql : alterStatements) {
                try (Statement stmt = con.createStatement()) {
                    stmt.execute(sql);
                    System.out.println("✓ " + sql.substring(sql.lastIndexOf("ADD"), sql.lastIndexOf("AFTER")));
                } catch (SQLException e) {
                    if (e.getMessage().contains("Duplicate column")) {
                        System.out.println("ℹ  Column already exists (skipped)");
                    } else {
                        System.out.println("❌ Error: " + e.getMessage());
                    }
                }
            }

            System.out.println("\n📋 Verifying columns were added:\n");
            String verifyQuery = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME='loan_applications' AND COLUMN_NAME IN ('admin_id', 'admin_comments', 'approved_at', 'rejected_at', 'rejection_reason', 'approved_amount') ORDER BY ORDINAL_POSITION";
            
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(verifyQuery)) {
                int count = 0;
                while (rs.next()) {
                    System.out.println("✓ " + rs.getString(1));
                    count++;
                }
                if (count == 6) {
                    System.out.println("\n✅ All 6 columns added successfully!");
                } else {
                    System.out.println("\n⚠️  Only " + count + " of 6 columns found");
                }
            }

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║   ✅ COLUMNS ADDED SUCCESSFULLY                         ║");
            System.out.println("╚════════════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
