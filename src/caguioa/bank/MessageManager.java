package caguioa.bank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageManager {

    public static void ensureTable() {
        try {
            Connection con = DB.connect();
            PreparedStatement pst = con.prepareStatement(
                "CREATE TABLE IF NOT EXISTS user_messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "admin_id INT, " +
                "subject VARCHAR(255), " +
                "body TEXT, " +
                "is_read BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB"
            );
            pst.execute();
            pst.close();
        } catch (Exception e) {
            System.out.println("MessageManager.ensureTable error: " + e);
        }
    }

    public static boolean sendMessageToUser(int userId, int adminId, String subject, String body) {
        if (userId <= 0) return false;
        ensureTable();
        try {
            Connection con = DB.connect();
            PreparedStatement pst = con.prepareStatement(
                "INSERT INTO user_messages(user_id, admin_id, subject, body) VALUES(?,?,?,?)"
            );
            pst.setInt(1, userId);
            pst.setInt(2, adminId);
            pst.setString(3, subject);
            pst.setString(4, body);
            pst.executeUpdate();
            pst.close();
            return true;
        } catch (Exception e) {
            System.out.println("MessageManager.sendMessageToUser error: " + e);
            return false;
        }
    }

    public static List<Map<String, Object>> getUserMessages(int userId, int limit) {
        List<Map<String, Object>> out = new ArrayList<>();
        try {
            Connection con = DB.connect();
            PreparedStatement pst = con.prepareStatement(
                "SELECT id, admin_id, subject, body, is_read, created_at FROM user_messages WHERE user_id=? ORDER BY id DESC LIMIT ?"
            );
            pst.setInt(1, userId);
            pst.setInt(2, limit);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("admin_id", rs.getInt("admin_id"));
                m.put("subject", rs.getString("subject"));
                m.put("body", rs.getString("body"));
                m.put("is_read", rs.getBoolean("is_read"));
                m.put("created_at", rs.getTimestamp("created_at"));
                out.add(m);
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("MessageManager.getUserMessages error: " + e);
        }
        return out;
    }

    public static List<Map<String, Object>> getAllUserMessages(int userId) {
        List<Map<String, Object>> out = new ArrayList<>();
        try {
            Connection con = DB.connect();
            PreparedStatement pst = con.prepareStatement(
                "SELECT id, admin_id, subject, body, is_read, created_at FROM user_messages WHERE user_id=? ORDER BY id DESC"
            );
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("admin_id", rs.getInt("admin_id"));
                m.put("subject", rs.getString("subject"));
                m.put("body", rs.getString("body"));
                m.put("is_read", rs.getBoolean("is_read"));
                m.put("created_at", rs.getTimestamp("created_at"));
                out.add(m);
            }
            rs.close();
            pst.close();
        } catch (Exception e) {
            System.out.println("MessageManager.getAllUserMessages error: " + e);
        }
        return out;
    }

    public static void markAsRead(int messageId) {
        try {
            Connection con = DB.connect();
            PreparedStatement pst = con.prepareStatement("UPDATE user_messages SET is_read=TRUE WHERE id=?");
            pst.setInt(1, messageId);
            pst.executeUpdate();
            pst.close();
        } catch (Exception e) {
            System.out.println("MessageManager.markAsRead error: " + e);
        }
    }
}
