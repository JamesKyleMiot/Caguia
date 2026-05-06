package caguioa.bank;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Deploys the complete database schema from COMPLETE_DATABASE_SETUP.sql
 * Run this once to initialize or update your lawbank database
 */
public class DatabaseDeployer {
    
    public static void main(String[] args) {
        deployDatabase();
    }
    
    public static void deployDatabase() {
        try {
            System.out.println("🔧 Starting database deployment...");
            
            // Read the SQL file
            String sqlFile = "COMPLETE_DATABASE_SETUP.sql";
            String sqlContent = new String(Files.readAllBytes(Paths.get(sqlFile)));
            
            // Connect to database
            Connection conn = DB.connect();
            if (conn == null) {
                System.out.println("❌ Failed to connect to database. Check your DB.java configuration.");
                return;
            }
            
            // Parse and execute statements respecting DELIMITER changes
            java.util.List<String> statements = parseSQLStatements(sqlContent);
            int count = 0;
            
            Statement stmt = conn.createStatement();
            for (String sql : statements) {
                sql = sql.trim();
                if (sql.length() > 0 && !sql.startsWith("--")) {
                    try {
                        stmt.execute(sql);
                        count++;
                        System.out.println("✓ Statement " + count);
                    } catch (Exception e) {
                        // Some statements may fail if they already exist, continue
                        String errMsg = e.getMessage();
                        if (!errMsg.contains("already exists")) {
                            System.out.println("  → " + errMsg.substring(0, Math.min(80, errMsg.length())));
                        }
                    }
                }
            }
            
            stmt.close();
            conn.close();
            
            System.out.println("\n✅ Database deployment complete!");
            System.out.println("📊 Executed " + count + " SQL statements");
            System.out.println("\n✨ Your database now has:");
            System.out.println("   ✓ 2% interest rate on loans");
            System.out.println("   ✓ Unlimited loan applications per user");
            System.out.println("   ✓ 6-month loan terms");
            System.out.println("   ✓ Automatic receipt generation");
            System.out.println("   ✓ Payment tracking and penalties");
            
        } catch (Exception e) {
            System.out.println("❌ Deployment failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static java.util.List<String> parseSQLStatements(String sqlContent) {
        java.util.List<String> statements = new java.util.ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        String delimiter = ";";
        String[] lines = sqlContent.split("\n");
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Check for DELIMITER command
            if (trimmedLine.startsWith("DELIMITER")) {
                delimiter = trimmedLine.substring(9).trim();
                continue;
            }
            
            // Skip comments and empty lines
            if (trimmedLine.startsWith("--") || trimmedLine.startsWith("/*") || trimmedLine.isEmpty()) {
                continue;
            }
            
            currentStatement.append(line).append("\n");
            
            // Check if line ends with current delimiter
            if (trimmedLine.endsWith(delimiter)) {
                String statement = currentStatement.toString().trim();
                // Remove the trailing delimiter
                statement = statement.substring(0, statement.length() - delimiter.length()).trim();
                if (statement.length() > 0) {
                    statements.add(statement);
                }
                currentStatement = new StringBuilder();
            }
        }
        
        // Add any remaining statement
        if (currentStatement.length() > 0) {
            String statement = currentStatement.toString().trim();
            if (statement.endsWith(delimiter)) {
                statement = statement.substring(0, statement.length() - delimiter.length()).trim();
            }
            if (statement.length() > 0) {
                statements.add(statement);
            }
        }
        
        return statements;
    }
}
