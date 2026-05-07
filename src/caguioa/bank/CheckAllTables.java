package caguioa.bank;

import java.sql.*;

public class CheckAllTables {
    public static void main(String[] args) {
        try (Connection con = DB.connect()) {
            String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='lawbank' ORDER BY TABLE_NAME";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                System.out.println("\n📋 All tables in lawbank database:\n");
                while (rs.next()) {
                    System.out.println("  • " + rs.getString(1));
                }
            }
            
            // Check password_reset_requests columns
            System.out.println("\n📋 password_reset_requests columns:\n");
            query = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME='password_reset_requests'";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    System.out.println("  • " + rs.getString(1) + " (" + rs.getString(2) + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
