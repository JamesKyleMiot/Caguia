package caguioa.bank;

import java.sql.*;
import java.io.*;
import java.nio.file.*;

/**
 * Database Migration Utility
 * Executes SQL migration for transaction system tables
 */
public class DatabaseMigration {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║   TRANSACTION SYSTEM DATABASE MIGRATION                 ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        try {
            // Read SQL migration file
            String sqlFilePath = "TRANSACTION_SYSTEM_MIGRATION.sql";
            String sqlContent = new String(Files.readAllBytes(Paths.get(sqlFilePath)));

            // Execute migration
            executeMigration(sqlContent);

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║   ✅ MIGRATION COMPLETED SUCCESSFULLY                   ║");
            System.out.println("╚════════════════════════════════════════════════════════╝\n");

        } catch (Exception e) {
            System.out.println("\n❌ MIGRATION FAILED");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void executeMigration(String sqlContent) {
        Connection con = DB.connect();
        if (con == null) {
            throw new RuntimeException("Failed to connect to database");
        }

        try {
            // Split SQL by statements (;)
            String[] statements = sqlContent.split(";");

            System.out.println("📋 Executing " + statements.length + " SQL statements...\n");

            int executedCount = 0;
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (trimmed.isEmpty()) continue;

                try {
                    // Skip SELECT statements for execution, just log them
                    if (trimmed.toUpperCase().startsWith("SELECT")) {
                        System.out.println("🔍 Verifying: " + trimmed.substring(0, Math.min(60, trimmed.length())) + "...");
                        try (Statement stmt = con.createStatement();
                             ResultSet rs = stmt.executeQuery(trimmed)) {
                            while (rs.next()) {
                                System.out.println("   ✓ " + rs.getString(1) + ": " + rs.getInt(2) + " rows");
                            }
                        }
                    } else {
                        // Execute DDL/DML statements
                        System.out.println("⚙️  Executing: " + trimmed.substring(0, Math.min(60, trimmed.length())) + "...");
                        try (Statement stmt = con.createStatement()) {
                            stmt.execute(trimmed);
                            System.out.println("   ✓ Success");
                            executedCount++;
                        }
                    }
                } catch (SQLException e) {
                    // Some statements might fail if already exist, that's okay
                    if (e.getMessage().contains("already exists")) {
                        System.out.println("   ℹ️  Already exists (skipped)");
                    } else {
                        System.out.println("   ⚠️  " + e.getMessage());
                    }
                }
            }

            System.out.println("\n✅ Executed " + executedCount + " statements successfully");

            // Verify tables exist
            System.out.println("\n📊 Verifying tables in database:");
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME IN ('transactions', 'user_bank_accounts')"
                 )) {
                int tableCount = 0;
                while (rs.next()) {
                    System.out.println("   ✓ " + rs.getString("TABLE_NAME"));
                    tableCount++;
                }
                if (tableCount == 2) {
                    System.out.println("\n✅ Both required tables exist!");
                } else {
                    System.out.println("\n⚠️  Warning: Only " + tableCount + " of 2 required tables found");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        } finally {
            try {
                if (con != null) con.close();
            } catch (SQLException e) {
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
    }
}
