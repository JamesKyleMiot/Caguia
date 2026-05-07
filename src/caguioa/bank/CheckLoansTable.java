package caguioa.bank;

import java.sql.*;

public class CheckLoansTable {
    public static void main(String[] args) {
        try (Connection con = DB.connect()) {
            String query = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME='loans' ORDER BY ORDINAL_POSITION";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                System.out.println("\nLoans table columns:\n");
                while (rs.next()) {
                    String nullable = rs.getString(3).equals("YES") ? "✓ NULL" : "✗ NOT NULL";
                    System.out.println(rs.getString(1) + " (" + rs.getString(2) + ") - " + nullable);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
