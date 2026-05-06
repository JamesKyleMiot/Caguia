import os
import re

os.chdir(r'c:\Users\Acer\OneDrive\Documents\NetBeansProjects\Caguioa Bank\src\caguioa\bank')

# Fix remaining files with DB.getConnection()
files_to_fix = [
    'LoanPenaltyManager.java',
    'OnlineLoanPaymentDialog.java',
    'ReceiptGenerator.java'
]

for file in files_to_fix:
    if os.path.exists(file):
        with open(file, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()
        
        # Replace DB.getConnection() with DB.connect()
        content = content.replace('DB.getConnection()', 'DB.connect()')
        
        with open(file, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'Fixed: {file}')

# Fix LoanPenaltyManager boolean casting issue
if os.path.exists('LoanPenaltyManager.java'):
    with open('LoanPenaltyManager.java', 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
    
    # Fix the boolean casting
    content = content.replace('(boolean) penalty.get("paid") ? "PAID" : "PENDING"', '(penalty.get("paid") != null && (boolean) penalty.get("paid")) ? "PAID" : "PENDING"')
    
    with open('LoanPenaltyManager.java', 'w', encoding='utf-8') as f:
        f.write(content)
    print('Fixed boolean casting in LoanPenaltyManager')

# Add the missing method to LoanManager if needed
if os.path.exists('LoanManager.java'):
    with open('LoanManager.java', 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()
    
    if 'public static Map<String, Object> getUserActiveLoan(int userId)' not in content:
        # Add the method before the last closing brace
        method = '''
    
    /**
     * Get user's active loan
     */
    public static Map<String, Object> getUserActiveLoan(int userId) {
        try (Connection conn = DB.connect()) {
            String query = "SELECT id, amount, total_payable, remaining_balance, due_date, status FROM loans WHERE user_id = ? AND status = 'active' ORDER BY id DESC LIMIT 1";
            try (PreparedStatement pst = conn.prepareStatement(query)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> loan = new HashMap<>();
                        loan.put("id", rs.getInt("id"));
                        loan.put("amount", rs.getDouble("amount"));
                        loan.put("total_payable", rs.getDouble("total_payable"));
                        loan.put("remaining_balance", rs.getDouble("remaining_balance"));
                        loan.put("due_date", rs.getDate("due_date"));
                        loan.put("status", rs.getString("status"));
                        return loan;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error getting user active loan: " + e);
        }
        return null;
    }
'''
        # Insert before the last closing brace
        content = content.rstrip()
        if content.endswith('}'):
            content = content[:-1] + method + '\n}\n'
        with open('LoanManager.java', 'w', encoding='utf-8') as f:
            f.write(content)
        print('Added getUserActiveLoan method to LoanManager')

print('All fixes applied!')
