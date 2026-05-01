package caguioa.bank;

import java.sql.*;

public class ResetDB {
    public static void main(String[] args) {
        try {
            Connection con = DB.connect();
            
            if(con == null) {
                System.out.println("❌ Database connection failed!");
                return;
            }
            
            // Get all users
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id FROM users ORDER BY id");
            
            int totalUsers = 0;
            while(rs.next()) {
                totalUsers++;
            }
            
            System.out.println("📊 Total users in database: " + totalUsers);
            
            if(totalUsers <= 1) {
                System.out.println("⚠️  Already have 1 or fewer users. Resetting balances to 0...");
            } else {
                // Delete extra users (keep only 1, delete the rest)
                int usersToDelete = totalUsers - 1;
                PreparedStatement deleteStmt = con.prepareStatement(
                    "DELETE FROM users WHERE id NOT IN (SELECT MIN(id) FROM (SELECT id FROM users) AS temp)"
                );
                int deleted = deleteStmt.executeUpdate();
                System.out.println("✅ Deleted " + deleted + " users. Keeping the first user.");
            }
            
            // Reset all remaining users' balances to 0
            PreparedStatement resetStmt = con.prepareStatement(
                "UPDATE users SET balance=0, savings=0, total_deposit=0"
            );
            int updated = resetStmt.executeUpdate();
            System.out.println("✅ Reset " + updated + " user(s): balance=0, savings=0, total_deposit=0");
            
            // Clear all transactions
            PreparedStatement clearTrans = con.prepareStatement("DELETE FROM transactions");
            int transCleaned = clearTrans.executeUpdate();
            System.out.println("✅ Cleared " + transCleaned + " transaction records");
            
            // Clear all loans
            PreparedStatement clearLoans = con.prepareStatement("DELETE FROM loans");
            int loansCleaned = clearLoans.executeUpdate();
            System.out.println("✅ Cleared " + loansCleaned + " loan records");
            
            System.out.println("\n✨ Database reset complete! System ready for single-user mode.");
            
            con.close();
            
        } catch(Exception e) {
            System.out.println("❌ Error: " + e);
            e.printStackTrace();
        }
    }
}
