/*
Rollback: remove booking phone number snapshot column
Target: SQL Server
Date: 2026-03-18

Warning:
- This rollback drops Booking.phone_number and will lose stored snapshot data.
*/

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.Booking', 'phone_number') IS NOT NULL
    BEGIN
        ALTER TABLE dbo.Booking
            DROP COLUMN phone_number;
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    DECLARE @ErrMsg NVARCHAR(4000) = ERROR_MESSAGE();
    DECLARE @ErrNum INT = ERROR_NUMBER();

    RAISERROR('Booking phone rollback failed (%d): %s', 16, 1, @ErrNum, @ErrMsg) WITH NOWAIT;
END CATCH;
