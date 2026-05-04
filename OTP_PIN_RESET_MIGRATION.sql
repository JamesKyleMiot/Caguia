-- Database Migration: Add OTP functionality to PIN Reset System
-- Run this script to update your database with OTP support

USE lawbank;

-- Add OTP columns to pin_reset_requests table if they don't exist
ALTER TABLE pin_reset_requests ADD COLUMN IF NOT EXISTS otp VARCHAR(10);
ALTER TABLE pin_reset_requests ADD COLUMN IF NOT EXISTS otp_generated_at TIMESTAMP NULL;
ALTER TABLE pin_reset_requests ADD COLUMN IF NOT EXISTS otp_verified BOOLEAN DEFAULT FALSE;

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_pin_requests_user ON pin_reset_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_pin_requests_status ON pin_reset_requests(status);

-- Verify the table structure
DESC pin_reset_requests;

-- Test query to see all pending PIN reset requests
SELECT 
    p.id,
    p.user_id,
    u.username,
    u.fullname,
    p.email,
    p.status,
    p.otp_verified,
    p.created_at,
    p.reviewed_at,
    p.expires_at
FROM pin_reset_requests p
JOIN users u ON p.user_id = u.id
WHERE p.status IN ('pending', 'approved')
ORDER BY p.created_at DESC;
