SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = 'CK_Booking_PaymentStatus_Values'
          AND parent_object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        ALTER TABLE dbo.Booking DROP CONSTRAINT CK_Booking_PaymentStatus_Values;
    END;

    ALTER TABLE dbo.Booking
        ADD CONSTRAINT CK_Booking_PaymentStatus_Values
        CHECK (payment_status IN (N'pending', N'deposited', N'paid', N'pending refund', N'pending refund confirm', N'refunded', N'failed'));

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
