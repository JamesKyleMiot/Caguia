package caguioa.bank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class VerifyAndFixAdmin {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/lawbank?serverTimezone=UTC";
        String user = "root";
        String pass = ""; // change if your XAMPP MySQL has a password

        try {
            System.out.println("Connecting to 'lawbank' database...");
            Connection con = DriverManager.getConnection(url, user, pass);

            // Check if admin table exists and has records
            System.out.println("\n✓ Checking admin table...");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) AS cnt FROM admin");
            rs.next();
            int adminCount = rs.getInt("cnt");
            System.out.println("  Admin records found: " + adminCount);

            if(adminCount > 0) {
                rs = st.executeQuery("SELECT id, username FROM admin");
                while(rs.next()) {
                    System.out.println("    - ID: " + rs.getInt("id") + ", Username: " + rs.getString("username"));
                }
            }

            // Delete old admin if exists
            if(adminCount > 0) {
                System.out.println("\n✓ Clearing existing admin accounts...");
                st.executeUpdate("DELETE FROM admin");
                System.out.println("  Old admin records deleted.");
            }

            // Insert fresh admin
            System.out.println("\n✓ Inserting fresh admin account...");
            String hashedPassword = SecurityUtil.hashPin("admin123");
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO admin(username, password) VALUES(?,?)"
            );
            pst.setString(1, "admin");
            pst.setString(2, hashedPassword);
            pst.executeUpdate();
            pst.close();
            System.out.println("  ✓ Admin inserted: username='admin', password='admin123' (hashed)");

            // Verify insertion
            System.out.println("\n✓ Verifying admin was inserted...");
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM admin WHERE username='admin'");
            if(rs.next()) {
                System.out.println("  ✓ SUCCESS! Admin found:");
                System.out.println("    - ID: " + rs.getInt("id"));
                System.out.println("    - Username: " + rs.getString("username"));
                System.out.println("    - Password (hashed): " + rs.getString("password").substring(0, 20) + "...");
            } else {
                System.out.println("  ✗ ERROR: Admin not found after insertion!");
            }

            st.close();
            con.close();
            System.out.println("\n✅ Admin account fixed and verified!");

        } catch (Exception e) {
            System.out.println("\n❌ Error: " + e);
            e.printStackTrace();
        }
    }
}
