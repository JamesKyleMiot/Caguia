-- User Bank Accounts Table
-- Stores bank account information for each user (account number, bank name, etc.)

CREATE TABLE IF NOT EXISTS user_bank_accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    account_holder_name VARCHAR(255) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50),
    branch_code VARCHAR(20),
    is_primary BOOLEAN DEFAULT TRUE,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_account_number (account_number)
);

-- Add foreign key constraint if not exists
ALTER TABLE user_bank_accounts 
ADD CONSTRAINT IF NOT EXISTS fk_user_bank_accounts_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
