# 📱 TRANSACTION SYSTEM - QUICK REFERENCE

## ⚡ Most Common Operations

### Record a Transaction
```java
int txnId = TransactionManager.recordTransaction(
    userId, 
    TransactionManager.TYPE_DEPOSIT,  // Transaction type
    5000.00,                          // Amount
    "GCash"                           // Payment method
);
```

### Get All User Transactions
```java
List<Map<String, Object>> transactions = 
    TransactionManager.getUserTransactions(userId);
    
// Access transaction details:
for (Map<String, Object> txn : transactions) {
    int id = (Integer) txn.get("id");
    String type = (String) txn.get("type");
    double amount = (Double) txn.get("amount");
    Timestamp date = (Timestamp) txn.get("created_at");
}
```

### Get Transaction Summary
```java
Map<String, Object> summary = 
    TransactionManager.getUserTransactionSummary(userId);
    
Integer totalCount = (Integer) summary.get("total_transactions");
Double totalAmount = (Double) summary.get("total_amount");
Double deposits = (Double) summary.get("total_deposits");
Double loanPayments = (Double) summary.get("total_loan_payments");
Timestamp lastDate = (Timestamp) summary.get("last_transaction_date");
```

### Get Transactions by Type
```java
List<Map<String, Object>> deposits = 
    TransactionManager.getUserTransactionsByType(
        userId, 
        TransactionManager.TYPE_DEPOSIT
    );
```

### View Formatted Transaction Report
```java
ViewUserTransactions.viewAllTransactions(userId);
```

---

## 🏷️ Transaction Types

| Constant | Type | Usage |
|----------|------|-------|
| `TYPE_DEPOSIT` | Deposit | Account deposits |
| `TYPE_WITHDRAWAL` | Withdrawal | Cash withdrawals |
| `TYPE_LOAN_DISBURSEMENT` | Loan Disbursement | Loan applications |
| `TYPE_LOAN_PAYMENT` | Loan Payment | Loan payments |
| `TYPE_LOAN_APPLICATION` | Loan Application | Loan applications |
| `TYPE_TRANSFER` | Transfer | Bank transfers |
| `TYPE_SAVINGS_TRANSFER` | Savings Transfer | Savings transfers |
| `TYPE_BILL_PAYMENT` | Bill Payment | Bill payments |
| `TYPE_PENALTY` | Penalty | Account penalties |
| `TYPE_INTEREST` | Interest | Interest credits |
| `TYPE_ACCOUNT_FEE` | Account Fee | Bank fees |

---

## 💳 Bank Account Management

### Save User Bank Account
```java
UserBankAccountManager.saveBankAccount(
    userId,
    "100234567890",           // Account number
    "JamesKyle Miot",         // Account holder name
    "Caguioa Bank",           // Bank name
    "Savings",                // Account type
    "MAIN-01"                 // Branch code
);
```

### Get User Bank Account
```java
Map<String, Object> account = 
    UserBankAccountManager.getUserBankAccount(userId);
    
String accountNumber = (String) account.get("account_number");
String holder = (String) account.get("account_holder_name");
String bank = (String) account.get("bank_name");
String type = (String) account.get("account_type");
```

### Get All Verified Accounts (Admin)
```java
List<Map<String, Object>> verifiedAccounts = 
    UserBankAccountManager.getAllVerifiedBankAccounts();
```

---

## 📊 Reports & Analytics

### Monthly Transaction Summary
```java
List<Map<String, Object>> monthly = 
    TransactionManager.getMonthlyTransactionSummary(
        userId, 
        3  // Last 3 months
    );
    
// Returns: month, year, transaction_count, total_amount
```

### Transaction Stats by Type
```java
Map<String, Object> stats = 
    TransactionManager.getTransactionStatsByType(userId);
    
// Returns: {type -> {count, total}, ...}
for (Map.Entry<String, Object> entry : stats.entrySet()) {
    Map<String, Object> data = (Map<String, Object>) entry.getValue();
    Integer count = (Integer) data.get("count");
    Double total = (Double) data.get("total");
    System.out.println(entry.getKey() + ": " + count + " txns, ₱" + total);
}
```

### All Transactions (Admin)
```java
List<Map<String, Object>> allTxns = 
    TransactionManager.getAllTransactions(100);  // Get last 100
```

---

## 🔄 Integration Points

### In UserDashboard (Already Integrated)
```java
// When user deposits money:
TransactionManager.recordTransaction(
    Session.userId, 
    TransactionManager.TYPE_DEPOSIT, 
    depositAmount, 
    paymentMethod
);

// When user withdraws:
TransactionManager.recordTransaction(
    Session.userId, 
    TransactionManager.TYPE_WITHDRAWAL, 
    withdrawalAmount, 
    paymentMethod
);
```

### In LoanPaymentDialog
```java
// When user pays loan:
TransactionManager.recordTransaction(
    userId, 
    TransactionManager.TYPE_LOAN_PAYMENT, 
    paymentAmount, 
    paymentMethod
);
```

### In LoanApplicationDialog
```java
// When user applies for loan:
TransactionManager.recordTransaction(
    userId, 
    TransactionManager.TYPE_LOAN_DISBURSEMENT, 
    loanAmount, 
    "Loan Application"
);
```

---

## 🗄️ Database Queries

### View All Transactions for a User
```sql
SELECT * FROM transactions 
WHERE user_id = ? 
ORDER BY created_at DESC 
LIMIT 100;
```

### Transaction Summary
```sql
SELECT 
    COUNT(*) as total_transactions,
    SUM(amount) as total_amount,
    SUM(CASE WHEN type='Deposit' THEN amount ELSE 0 END) as total_deposits,
    SUM(CASE WHEN type='Loan Payment' THEN amount ELSE 0 END) as total_loan_payments,
    MAX(created_at) as last_transaction_date
FROM transactions 
WHERE user_id = ?;
```

### Transactions by Type
```sql
SELECT 
    type,
    COUNT(*) as count,
    SUM(amount) as total
FROM transactions
WHERE user_id = ?
GROUP BY type;
```

---

## 🛠️ Troubleshooting

### No Transactions Showing
```java
// Check if transactions exist for user
List<Map<String, Object>> txns = 
    TransactionManager.getUserTransactions(userId);
System.out.println("Total transactions: " + txns.size());

// If empty, record one:
int txnId = TransactionManager.recordTransaction(
    userId, 
    TransactionManager.TYPE_DEPOSIT, 
    1000.00, 
    "Test"
);
System.out.println("Transaction recorded: " + txnId);
```

### Verify Bank Account
```java
Map<String, Object> account = 
    UserBankAccountManager.getUserBankAccount(userId);
if (account != null && !account.isEmpty()) {
    System.out.println("✓ Account found: " + account.get("account_number"));
} else {
    System.out.println("✗ No account for user: " + userId);
}
```

### Run Transaction View
```bash
cd "c:\Users\Acer\OneDrive\Documents\NetBeansProjects\Caguioa Bank"
java -cp "lib/*;src" caguioa.bank.ViewUserTransactions
```

---

## 📋 Method Reference

### TransactionManager
```
recordTransaction(userId, type, amount, method) → int (txn ID)
getUserTransactions(userId) → List<Map>
getUserTransactionsByType(userId, type) → List<Map>
getUserTransactionSummary(userId) → Map
getTransactionStatsByType(userId) → Map
getAllTransactions(limit) → List<Map>
getMonthlyTransactionSummary(userId, monthsBack) → List<Map>
```

### UserBankAccountManager
```
saveBankAccount(userId, accountNumber, holderName, bankName, type, branch) → void
getUserBankAccount(userId) → Map
getAllVerifiedBankAccounts() → List<Map>
```

### ViewUserTransactions
```
viewAllTransactions(userId) → void (prints formatted report)
```

---

## 🎯 Common Use Cases

### Use Case 1: User Makes Deposit
```java
// UserDashboard.depositAmount()
TransactionManager.recordTransaction(userId, 
    TransactionManager.TYPE_DEPOSIT, 5000, "GCash");
```

### Use Case 2: Admin Views All User Transactions
```java
// AdminDashboard.viewUserTransactions()
List<Map<String, Object>> txns = 
    TransactionManager.getUserTransactions(userId);
ViewUserTransactions.viewAllTransactions(userId);
```

### Use Case 3: Generate Monthly Report
```java
// ReportingModule
List<Map<String, Object>> monthly = 
    TransactionManager.getMonthlyTransactionSummary(userId, 12);
// Display as chart/table
```

### Use Case 4: Audit Trail
```java
// ComplianceModule
List<Map<String, Object>> allTxns = 
    TransactionManager.getAllTransactions(1000);
// Export to CSV/PDF
```

---

**Last Updated**: May 7, 2026  
**System Version**: Caguioa Bank v2.0  
**Status**: ✅ Production Ready
