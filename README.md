# Caguioa Bank System

## System Overview
A Java Swing-based banking application with comprehensive transaction management, loans, and savings features.

---

## Features

### 1. **Deposit**
- Multiple deposit methods: GCash, PayMaya, Bank
- Bank options: BDO, LandBank, UnionBank, GoTymeBank, SeaBank
- Updates both balance and total deposit
- Transaction logging

### 2. **Withdraw**
- Check available balance before withdrawal
- Confirmation prompt required
- Insufficient balance protection
- Receipt generation with option to print
- Transaction logging

### 3. **Loan**
- Minimum loan: ₱50,000
- Maximum loan: ₱300,000
- Requires minimum balance/deposit of ₱50,000
- 10% interest charged
- Total payable = Loan Amount + (Loan Amount × 10%)
- Loan receipt with print option

### 4. **Transfer to Savings**
- Transfer from balance to savings
- Balance validation before transfer
- Shows current balance after transfer
- Transaction logging

### 5. **Withdraw from Savings**
- Withdraw funds from savings account
- Returns funds to main balance
- Transaction logging

### 6. **View Savings**
- Display current savings balance

---

## Savings Account Flow

✅ **Example:**

If you put ₱10,000 in a savings account with 1% annual interest:

- **Initial Deposit:** ₱10,000
- **Annual Interest Rate:** 1%
- **Interest Earned (After 1 Year):** ₱10,000 × 1% = ₱100
- **Total After 1 Year:** ₱10,100

### How It Works:
1. Transfer funds from your main balance to savings
2. Savings earns 1% annual interest automatically
3. Interest accrues over time
4. Withdraw anytime to return funds to main balance

---

## System Requirements

- Java Development Kit (JDK) 8 or higher
- MySQL Database
- NetBeans IDE (optional, for development)

---

## Database Setup

The system uses MySQL with the following main tables:
- `users` - User account information (balance, total_deposit, savings)
- `transactions` - Transaction history
- `loans` - Loan records
- `user_messages` - Admin notifications shown in the user dashboard
- `account_audit_log` - Audit trail for suspend/reactivate actions
- `admin` - Admin login accounts

### User Dashboard Flow
The user dashboard now shows three live views:
1. All user transactions
2. All user loans / loan history
3. Admin notifications

If a user already has an active loan, the Loan button prompts them to pay first before applying for another loan.

---

## Getting Started

1. Clone or download the project
2. Configure database connection in `DB.java`
3. Build the project using NetBeans or compile with javac
4. Run the application through the main entry point

---

## Transaction Flow

### Deposit Transaction
- Amount added to balance and total_deposit
- Method (GCash/PayMaya/Bank) recorded
- Immediate update to account

### Loan Transaction
- User must have ₱50,000+ in balance or total deposit
- User selects loan amount (₱50,000 - ₱300,000)
- 10% interest automatically calculated
- Loan disbursed and recorded
- Receipt generated
- Existing active loans must be paid first before a new loan can be requested

### Savings Transaction
- Transfer from balance to savings (with balance validation)
- Funds protected in savings account
- Grows with 1% annual interest
- Can withdraw back to main balance anytime

---

## Security Features

- User authentication required
- Session management
- Transaction logging for audit trail
- Balance verification before transactions
- Notification history stored in the database for in-app viewing

---

## Support

For issues or inquiries, please contact the development team.
