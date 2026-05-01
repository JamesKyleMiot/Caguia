package caguioa.bank;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class CreateLawBank {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/?serverTimezone=UTC";
        String user = "root";
        String pass = ""; // change if your XAMPP MySQL has a password

        try {
            System.out.println("Connecting to MySQL server...");
            Connection con = DriverManager.getConnection(url, user, pass);
            Statement st = con.createStatement();

            System.out.println("Creating database 'lawbank'...");
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS lawbank CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            st.close();
            con.close();

            System.out.println("Connecting to 'lawbank' database...");
            String dbUrl = "jdbc:mysql://localhost:3306/lawbank?serverTimezone=UTC";
            Connection db = DriverManager.getConnection(dbUrl, user, pass);
            Statement s = db.createStatement();

            System.out.println("Preparing fresh schema (dropping existing tables if any) and creating tables...");

            // Drop in reverse-FK order to ensure clean recreate
            try { s.executeUpdate("DROP TABLE IF EXISTS transactions"); } catch(Exception ex) {}
            try { s.executeUpdate("DROP TABLE IF EXISTS loans"); } catch(Exception ex) {}
            try { s.executeUpdate("DROP TABLE IF EXISTS admin"); } catch(Exception ex) {}
            try { s.executeUpdate("DROP TABLE IF EXISTS users"); } catch(Exception ex) {}

            s.executeUpdate("CREATE TABLE users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "fullname VARCHAR(100) NOT NULL,"
                + "username VARCHAR(50) NOT NULL UNIQUE,"
                + "sex VARCHAR(10),"
                + "nationality VARCHAR(100),"
                + "address VARCHAR(255),"
                + "age INT,"
                + "pin VARCHAR(255) NOT NULL,"
                + "role VARCHAR(20) NOT NULL DEFAULT 'user',"
                + "balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,"
                + "savings DECIMAL(15,2) NOT NULL DEFAULT 0.00,"
                + "total_deposit DECIMAL(15,2) NOT NULL DEFAULT 0.00,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB");

            s.executeUpdate("CREATE TABLE IF NOT EXISTS transactions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "user_id INT NOT NULL,"
                + "type VARCHAR(50) NOT NULL,"
                + "amount DECIMAL(15,2) NOT NULL,"
                + "method VARCHAR(100),"
                + "details TEXT,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "KEY idx_tx_user (user_id),"
                + "CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB");

            s.executeUpdate("CREATE TABLE IF NOT EXISTS loans ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "user_id INT NOT NULL,"
                + "amount DECIMAL(15,2) NOT NULL,"
                + "interest_rate DECIMAL(5,2) NOT NULL,"
                + "total_payable DECIMAL(15,2) NOT NULL,"
                + "remaining_balance DECIMAL(15,2) NOT NULL,"
                + "status VARCHAR(50) DEFAULT 'active',"
                + "due_date DATE NOT NULL,"
                + "witness_name VARCHAR(100),"
                + "witness_contact VARCHAR(100),"
                + "witness_signature LONGBLOB,"
                + "user_signature LONGBLOB,"
                + "promissory_note_url VARCHAR(255),"
                + "is_account_blocked BOOLEAN DEFAULT FALSE,"
                + "blocked_date TIMESTAMP NULL,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "KEY idx_loans_user (user_id),"
                + "CONSTRAINT fk_loan_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB");

            s.executeUpdate("CREATE TABLE IF NOT EXISTS admin ("
                + "id INT AUTO_INCREMENT PRIMARY KEY,"
                + "username VARCHAR(50) NOT NULL UNIQUE,"
                + "password VARCHAR(255) NOT NULL,"
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ") ENGINE=InnoDB");

            // Indexes
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_loans_user ON loans(user_id)");

            // Seed default admin account if absent
            System.out.println("Seeding default admin account (if missing)...");
            PreparedStatement pst = db.prepareStatement("SELECT COUNT(*) AS cnt FROM admin WHERE username=?");
            pst.setString(1, "admin");
            java.sql.ResultSet rs = pst.executeQuery();
            boolean insertAdmin = true;
            if(rs.next()) {
                insertAdmin = (rs.getInt("cnt") == 0);
            }
            rs.close();
            pst.close();

            if(insertAdmin) {
                String hashed = SecurityUtil.hashPin("admin123");
                PreparedStatement ins = db.prepareStatement("INSERT INTO admin(username, password) VALUES(?,?)");
                ins.setString(1, "admin");
                ins.setString(2, hashed);
                ins.executeUpdate();
                ins.close();
                System.out.println("Inserted default admin (username: admin, password: admin123)");
            } else {
                System.out.println("Admin account already exists. Skipping seed.");
            }

            // Seed a sample user if users table empty
            PreparedStatement userCount = db.prepareStatement("SELECT COUNT(*) AS cnt FROM users");
            java.sql.ResultSet ur = userCount.executeQuery();
            boolean insertUser = false;
            if(ur.next()) insertUser = (ur.getInt("cnt") == 0);
            ur.close(); userCount.close();

            if(insertUser) {
                String userPin = SecurityUtil.hashPin("123456");
                PreparedStatement insUser = db.prepareStatement(
                    "INSERT INTO users(fullname, username, sex, nationality, address, age, pin, role, balance, savings, total_deposit) VALUES(?,?,?,?,?,?,?,?,?,?,?)"
                );
                insUser.setString(1, "Sample User");
                insUser.setString(2, "user1");
                insUser.setString(3, "Male");
                insUser.setString(4, "Filipino");
                insUser.setString(5, "Sample Address");
                insUser.setInt(6, 30);
                insUser.setString(7, userPin);
                insUser.setString(8, "user");
                insUser.setDouble(9, 0.00);
                insUser.setDouble(10, 0.00);
                insUser.setDouble(11, 0.00);
                insUser.executeUpdate();
                insUser.close();
                System.out.println("Inserted sample user (username: user1, pin: 123456)");
            } else {
                System.out.println("Users already present. Skipping sample user seed.");
            }

            s.close();
            db.close();

            System.out.println("All done. 'lawbank' database and tables are ready.");

        } catch (Exception e) {
            System.out.println("Error creating lawbank: " + e);
            e.printStackTrace();
        }
    }
}
