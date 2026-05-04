-- Password Reset Request Table
-- Add this to your lawbank database

CREATE TABLE IF NOT EXISTS password_reset_requests (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  email VARCHAR(255),
  phone VARCHAR(20),
  status VARCHAR(20) DEFAULT 'pending',  -- pending, approved, denied
  request_reason VARCHAR(255),
  admin_id INT,
  admin_response VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  reviewed_at TIMESTAMP NULL,
  expires_at TIMESTAMP NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (admin_id) REFERENCES admin(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- Index for quick lookups
CREATE INDEX IF NOT EXISTS idx_reset_userid ON password_reset_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_reset_status ON password_reset_requests(status);
