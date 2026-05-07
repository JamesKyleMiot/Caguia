package caguioa.bank;

import java.sql.*;

public class CheckUsersSchema {
    public static void main(String[] args) {
        try (Connection con = DB.connect()) {
            String query = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME='users' ORDER BY ORDINAL_POSITION";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                System.out.println("\n📋 Users table columns:\n");
                while (rs.next()) {
                    System.out.println("  • " + rs.getString(1) + " (" + rs.getString(2) + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
