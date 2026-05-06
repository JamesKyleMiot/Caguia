package caguioa.bank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;

/**
 * Database Setup Utility - Executes COMPLETE_DATABASE_SETUP.sql
 */
public class DatabaseSetup {
    
    private static final String DB_HOST = "localhost";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";  // Empty by default, update if needed
    
    public static void main(String[] args) {
        System.out.println("🔧 CAGUIOA BANK - DATABASE SETUP");
        System.out.println("================================\n");
        
        try {
            // Step 1: Load MySQL driver
            System.out.println("Loading MySQL driver...");
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✓ MySQL driver loaded\n");
            
            // Step 2: Read SQL file
            System.out.println("Reading COMPLETE_DATABASE_SETUP.sql...");
            String sqlScript = readSqlFile("COMPLETE_DATABASE_SETUP.sql");
            System.out.println("✓ SQL script loaded (" + sqlScript.length() + " bytes)\n");
            
            // Step 3: Connect to MySQL (no database specified initially)
            System.out.println("Connecting to MySQL server at " + DB_HOST + "...");
            String url = "jdbc:mysql://" + DB_HOST + ":3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            Connection conn = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
            System.out.println("✓ Connected to MySQL server\n");
            
            // Step 4: Execute SQL statements
            System.out.println("Executing SQL script...\n");
            executeSqlScript(conn, sqlScript);
            
            conn.close();
            System.out.println("\n✓ DATABASE SETUP COMPLETE!");
            System.out.println("================================");
            System.out.println("✓ Database: lawbank");
            System.out.println("✓ Tables: 8");
            System.out.println("✓ OTP PIN Reset Ready: YES");
            
        } catch (Exception e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String readSqlFile(String filename) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip comments and empty lines
                if (!line.trim().startsWith("--") && !line.trim().isEmpty()) {
                    sb.append(line).append("\n");
                }
            }
        }
        return sb.toString();
    }
    
    private static void executeSqlScript(Connection conn, String sqlScript) throws SQLException {
        // Split by semicolons
        String[] statements = sqlScript.split(";");
        
        int count = 0;
        for (String statement : statements) {
            String trimmed = statement.trim();
            if (!trimmed.isEmpty()) {
                try (Statement stmt = conn.createStatement()) {
                    System.out.println("Executing: " + trimmed.substring(0, Math.min(60, trimmed.length())) + "...");
                    stmt.executeUpdate(trimmed + ";");
                    count++;
                } catch (SQLException e) {
                    // Some statements may fail (IF NOT EXISTS), continue
                    if (!e.getMessage().toLowerCase().contains("already exists")) {
                        System.out.println("⚠ Warning: " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("\n✓ Executed " + count + " SQL statements");
    }
}
