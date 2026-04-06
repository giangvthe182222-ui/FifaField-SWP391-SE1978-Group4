-- Reset all booking-related data after split-status migration.
-- This script removes booking records, dependent payment/feedback data,
-- and drops the legacy backup table created during status-column removal.

SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    -- Delete child records first to satisfy foreign key constraints.
    IF OBJECT_ID(N'dbo.Feedback', N'U') IS NOT NULL
        DELETE FROM dbo.Feedback;

    IF OBJECT_ID(N'dbo.Invoice', N'U') IS NOT NULL
        DELETE FROM dbo.Invoice;

    IF OBJECT_ID(N'dbo.Payment_Log', N'U') IS NOT NULL
        DELETE FROM dbo.Payment_Log;

    IF OBJECT_ID(N'dbo.Booking_Equipment', N'U') IS NOT NULL
        DELETE FROM dbo.Booking_Equipment;

    IF OBJECT_ID(N'dbo.Payment', N'U') IS NOT NULL
        DELETE FROM dbo.Payment;

    IF OBJECT_ID(N'dbo.Booking', N'U') IS NOT NULL
        DELETE FROM dbo.Booking;

    IF OBJECT_ID(N'dbo.Weekly_Booking_Group', N'U') IS NOT NULL
        DELETE FROM dbo.Weekly_Booking_Group;

    -- With bookings cleared, all schedules can be reopened.
    IF OBJECT_ID(N'dbo.Schedule', N'U') IS NOT NULL
    BEGIN
        UPDATE dbo.Schedule
        SET status = 'available'
        WHERE LOWER(ISNULL(status, '')) <> 'available';
    END;

    -- Remove legacy backup table from drop-status migration.
    IF OBJECT_ID(N'dbo.Booking_Status_Backup_20260406', N'U') IS NOT NULL
        DROP TABLE dbo.Booking_Status_Backup_20260406;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
