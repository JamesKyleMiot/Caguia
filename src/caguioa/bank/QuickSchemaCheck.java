package caguioa.bank;

import java.sql.*;

public class QuickSchemaCheck {
    public static void main(String[] args) {
        try (Connection con = DB.connect()) {
            String query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='lawbank' AND TABLE_NAME='loan_applications' ORDER BY ORDINAL_POSITION";
            try (Statement stmt = con.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                System.out.println("Columns in loan_applications:\n");
                while (rs.next()) {
                    System.out.println(rs.getString(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
