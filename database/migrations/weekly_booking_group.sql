-- Weekly booking grouping support (Option B)
-- Run this migration on SQL Server before deploying updated weekly booking flow.

IF OBJECT_ID('dbo.Weekly_Booking_Group', 'U') IS NULL
BEGIN
    CREATE TABLE Weekly_Booking_Group (
        weekly_group_id VARCHAR(36) PRIMARY KEY,
        booker_id VARCHAR(36) NOT NULL,
        total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
        status NVARCHAR(30) NOT NULL DEFAULT N'pending',
        payment_deadline DATETIME2 NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_WeeklyGroup_User FOREIGN KEY (booker_id) REFERENCES Users(user_id)
    );
END;
GO

IF COL_LENGTH('dbo.Booking', 'weekly_group_id') IS NULL
BEGIN
    ALTER TABLE Booking ADD weekly_group_id VARCHAR(36) NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_Booking_WeeklyGroup'
)
BEGIN
    ALTER TABLE Booking
        ADD CONSTRAINT FK_Booking_WeeklyGroup
        FOREIGN KEY (weekly_group_id)
        REFERENCES Weekly_Booking_Group(weekly_group_id);
END;
GO

IF COL_LENGTH('dbo.Payment', 'weekly_group_id') IS NULL
BEGIN
    ALTER TABLE Payment ADD weekly_group_id VARCHAR(36) NULL;
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_Payment_WeeklyGroup'
)
BEGIN
    ALTER TABLE Payment
        ADD CONSTRAINT FK_Payment_WeeklyGroup
        FOREIGN KEY (weekly_group_id)
        REFERENCES Weekly_Booking_Group(weekly_group_id);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_Booking_WeeklyGroup'
      AND object_id = OBJECT_ID('dbo.Booking')
)
BEGIN
    CREATE INDEX IX_Booking_WeeklyGroup ON Booking(weekly_group_id);
END;
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'IX_Payment_WeeklyGroup'
      AND object_id = OBJECT_ID('dbo.Payment')
)
BEGIN
    CREATE INDEX IX_Payment_WeeklyGroup ON Payment(weekly_group_id);
END;
GO
