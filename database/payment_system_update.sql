-- =====================================================
-- Payment System Update for VietQR Integration
-- FifaFieldDB - QR Banking with Auto Check & Countdown
-- Consolidated from update_payment and update_payment_2
-- =====================================================

USE FifaFieldDB;
GO

-- 1. Add payment_deadline to Booking table
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Booking') AND name = 'payment_deadline')
BEGIN
    ALTER TABLE Booking ADD payment_deadline DATETIME2 NULL;
    PRINT '✓ Added payment_deadline to Booking table';
END
ELSE
BEGIN
    PRINT '✓ payment_deadline already exists in Booking table';
END
GO

-- 2. Add VietQR tracking fields to Payment table
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Payment') AND name = 'transaction_code')
BEGIN
    ALTER TABLE Payment ADD transaction_code NVARCHAR(100) NULL;
    PRINT '✓ Added transaction_code to Payment table';
END
ELSE
BEGIN
    PRINT '✓ transaction_code already exists in Payment table';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Payment') AND name = 'qr_content')
BEGIN
    ALTER TABLE Payment ADD qr_content NVARCHAR(500) NULL;
    PRINT '✓ Added qr_content to Payment table';
END
ELSE
BEGIN
    PRINT '✓ qr_content already exists in Payment table';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Payment') AND name = 'bank_code')
BEGIN
    ALTER TABLE Payment ADD bank_code NVARCHAR(10) DEFAULT 'MB';
    PRINT '✓ Added bank_code to Payment table';
END
ELSE
BEGIN
    PRINT '✓ bank_code already exists in Payment table';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Payment') AND name = 'account_number')
BEGIN
    ALTER TABLE Payment ADD account_number NVARCHAR(50) DEFAULT '0974288256';
    PRINT '✓ Added account_number to Payment table';
END
ELSE
BEGIN
    PRINT '✓ account_number already exists in Payment table';
END
GO

-- 3. Create Payment_Log table if not exists
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Payment_Log')
BEGIN
    CREATE TABLE Payment_Log (
        log_id INT IDENTITY(1,1) PRIMARY KEY,
        payment_id VARCHAR(36) NOT NULL,
        check_time DATETIME2 DEFAULT SYSDATETIME(),
        status NVARCHAR(30),
        response_message NVARCHAR(255),
        CONSTRAINT FK_PaymentLog_Payment FOREIGN KEY (payment_id) REFERENCES Payment(payment_id)
    );
    PRINT '✓ Created Payment_Log table';
END
ELSE
BEGIN
    PRINT '✓ Payment_Log table already exists';
END
GO

-- 4. Create indexes for performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Payment_BookingId' AND object_id = OBJECT_ID('Payment'))
BEGIN
    CREATE UNIQUE INDEX IDX_Payment_BookingId ON Payment(booking_id);
    PRINT '✓ Created index IDX_Payment_BookingId';
END
ELSE
BEGIN
    PRINT '✓ Index IDX_Payment_BookingId already exists';
END
GO

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IDX_Booking_PaymentDeadline' AND object_id = OBJECT_ID('Booking'))
BEGIN
    CREATE INDEX IDX_Booking_PaymentDeadline ON Booking(payment_deadline);
    PRINT '✓ Created index IDX_Booking_PaymentDeadline';
END
ELSE
BEGIN
    PRINT '✓ Index IDX_Booking_PaymentDeadline already exists';
END
GO

-- 5. Verify the schema
PRINT '';
PRINT '=== Payment System Schema Verification ===';
SELECT 
    'Booking.payment_deadline' as Field,
    CASE WHEN EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Booking') AND name = 'payment_deadline') 
        THEN 'EXISTS' ELSE 'MISSING' END as Status;
SELECT 
    'Payment.transaction_code' as Field,
    CASE WHEN EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Payment') AND name = 'transaction_code') 
        THEN 'EXISTS' ELSE 'MISSING' END as Status;
SELECT 
    'Payment.qr_content' as Field,
    CASE WHEN EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Payment') AND name = 'qr_content') 
        THEN 'EXISTS' ELSE 'MISSING' END as Status;
SELECT 
    'Payment.bank_code' as Field,
    CASE WHEN EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Payment') AND name = 'bank_code') 
        THEN 'EXISTS' ELSE 'MISSING' END as Status;
SELECT 
    'Payment_Log table' as Field,
    CASE WHEN EXISTS (SELECT * FROM sys.tables WHERE name = 'Payment_Log') 
        THEN 'EXISTS' ELSE 'MISSING' END as Status;
GO

PRINT '';
PRINT '✓✓✓ Database schema update completed successfully! ✓✓✓';
GO
