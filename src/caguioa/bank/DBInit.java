package caguioa.bank;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DBInit {

    public static boolean ensureAllTables() {
        try (Connection con = DB.connect()) {
            if (con == null) {
                System.out.println("DBInit: cannot connect to DB");
                return false;
            }

            // Create users table
            PreparedStatement pst = con.prepareStatement(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) UNIQUE NOT NULL, " +
                "fullname VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) UNIQUE, " +
                "password VARCHAR(255), " +
                "balance DOUBLE DEFAULT 0, " +
                "savings DOUBLE DEFAULT 0, " +
                "total_deposit DOUBLE DEFAULT 0, " +
                "role VARCHAR(50) DEFAULT 'user', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB"
            );
            pst.execute();
            pst.close();

            // admin
            pst = con.prepareStatement(
                "CREATE TABLE IF NOT EXISTS admin (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) UNIQUE NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB"
            );
            pst.execute();
            pst.close();

            // transactions
            pst = con.prepareStatement(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "type VARCHAR(50), " +
                "amount DOUBLE, " +
                "method VARCHAR(255), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB"
            );
            pst.execute();
            pst.close();

            // loans
            pst = con.prepareStatement(
                "CREATE TABLE IF NOT EXISTS loans (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "amount DOUBLE NOT NULL, " +
                "interest_rate DOUBLE DEFAULT 0.02, " +
                "total_payable DOUBLE, " +
                "remaining_balance DOUBLE DEFAULT 0, " +
                "due_date DATE, " +
                "status VARCHAR(50) DEFAULT 'active', " +
                "witness_name VARCHAR(255), " +
                "witness_contact VARCHAR(255), " +
                "witness_signature LONGBLOB, " +
                "user_signature LONGBLOB, " +
                "promissory_note_url VARCHAR(1024), " +
                "is_account_blocked BOOLEAN DEFAULT FALSE, " +
                "blocked_date TIMESTAMP NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB"
            );
            pst.execute();
            pst.close();

            // Repair older loans tables that may be missing the primary key or newer columns.

            // account_audit_log
            pst = con.prepareStatement(
                "CREATE TABLE IF NOT EXISTS account_audit_log (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "action VARCHAR(50) NOT NULL, " +
                "reason VARCHAR(255), " +
                "admin_id INT, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB"
            );
            pst.execute();
            pst.close();

            // user_messages
            pst = con.prepareStatement(
                "CREATE TABLE IF NOT EXISTS user_messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "admin_id INT, " +
                "subject VARCHAR(255), " +
                "body TEXT, " +
                "is_read BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB"
            );
            pst.execute();
            pst.close();

            return true;
        } catch (Exception e) {
            System.out.println("DBInit.ensureAllTables error: " + e);
            return false;
        }
    }
}
