package caguioa.bank;

import java.sql.*;

/**
 * Database Schema Inspector
 * Checks what columns actually exist in loan_applications table
 */
public class SchemaInspector {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║   DATABASE SCHEMA INSPECTOR                             ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        Connection con = DB.connect();
        if (con == null) {
            System.out.println("❌ Failed to connect to database");
            return;
        }

        try {
            // Check loan_applications table columns
            System.out.println("📋 Checking loan_applications table columns:\n");
            showTableColumns(con, "loan_applications");

            // Try the problematic query
            System.out.println("\n🔍 Testing AdminDashboard query:\n");
            testAdminQuery(con);

        } catch (Exception e) {
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

    public static void showTableColumns(Connection con, String tableName) throws SQLException {
        String query = "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME=?";
        
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, tableName);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.isBeforeFirst()) {
                    System.out.println("⚠️  Table '" + tableName + "' not found!");
                    return;
                }

                System.out.println("Columns in " + tableName + ":");
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String colType = rs.getString("COLUMN_TYPE");
                    String nullable = rs.getString("IS_NULLABLE");
                    System.out.printf("  • %-30s %-20s %s\n", colName, colType, nullable);
                }
            }
        }
    }

    public static void testAdminQuery(Connection con) {
        try {
            // First, check if table exists and has data
            String checkQuery = "SELECT COUNT(*) FROM loan_applications WHERE status='pending'";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(checkQuery)) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("✓ Pending applications count: " + count);
                }
            }

            // Try the admin query
            String adminQuery = "SELECT la.id, la.user_id, la.full_name, la.requested_amount, la.purpose, " +
                                "la.employment_status, la.monthly_income, la.loan_term_months, la.created_at " +
                                "FROM loan_applications la WHERE la.status='pending' LIMIT 1";
            
            System.out.println("\nExecuting Admin query...");
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(adminQuery)) {
                if (rs.next()) {
                    System.out.println("✓ Query successful!");
                    System.out.println("  ID: " + rs.getObject("id"));
                    System.out.println("  User ID: " + rs.getObject("user_id"));
                    System.out.println("  Full Name: " + rs.getObject("full_name"));
                } else {
                    System.out.println("ℹ No pending applications found");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ Query Error: " + e.getMessage());
            System.out.println("\nTrying alternative queries to find valid columns...\n");

            try {
                // Try simpler query
                String simpleQuery = "SELECT * FROM loan_applications LIMIT 1";
                try (Statement stmt = con.createStatement();
                     ResultSet rs = stmt.executeQuery(simpleQuery)) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    System.out.println("Available columns in loan_applications:");
                    for (int i = 1; i <= metaData.getColumnCount(); i++) {
                        System.out.println("  • " + metaData.getColumnName(i) + " (" + metaData.getColumnTypeName(i) + ")");
                    }
                }
            } catch (SQLException e2) {
                System.out.println("❌ Error getting column list: " + e2.getMessage());
            }
        }
    }
}
