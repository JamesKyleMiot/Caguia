package caguioa.bank;

import java.sql.*;

public class CreateAdminTable {
    public static void main(String[] args) {
        try {
            Connection con = DB.connect();
            
            if(con == null) {
                System.out.println("❌ Database connection failed!");
                return;
            }
            
            // Check if admin table exists, if not create it
            DatabaseMetaData dbm = con.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "admin", null);
            
            if(tables.next()) {
                System.out.println("✅ Admin table already exists");
            } else {
                // Create admin table
                Statement stmt = con.createStatement();
                stmt.executeUpdate(
                    "CREATE TABLE admin (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL" +
                    ")"
                );
                System.out.println("✅ Admin table created");
                
                // Insert default admin credentials
                PreparedStatement pst = con.prepareStatement(
                    "INSERT INTO admin(username, password) VALUES(?, ?)"
                );
                pst.setString(1, "admin");
                pst.setString(2, SecurityUtil.hashPin("admin123")); // Password: admin123
                pst.executeUpdate();
                System.out.println("✅ Default admin account created (username: admin, password: admin123)");
            }
            
            con.close();
            System.out.println("\n✨ Admin setup complete!");
            
        } catch(Exception e) {
            System.out.println("❌ Error: " + e);
            e.printStackTrace();
        }
    }
}
