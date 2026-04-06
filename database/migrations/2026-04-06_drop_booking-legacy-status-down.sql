SET NOCOUNT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.Booking', N'U') IS NULL
    BEGIN
        THROW 51000, 'Table dbo.Booking does not exist.', 1;
    END;

    IF COL_LENGTH('dbo.Booking', 'status') IS NULL
    BEGIN
        ALTER TABLE dbo.Booking ADD status NVARCHAR(30) NULL;
    END;

    UPDATE b
    SET b.status = CASE
        WHEN LOWER(ISNULL(b.payment_status, '')) = 'pending refund' THEN N'pending refund'
                WHEN LOWER(ISNULL(b.payment_status, '')) = 'pending refund confirm' THEN N'pending refund'
        WHEN LOWER(ISNULL(b.payment_status, '')) = 'refunded' THEN N'refunded'
        WHEN LOWER(ISNULL(b.payment_status, '')) = 'failed'
          OR LOWER(ISNULL(b.play_status, '')) = 'cancelled' THEN N'cancelled'
        WHEN LOWER(ISNULL(b.play_status, '')) = 'completed' THEN N'completed'
        WHEN LOWER(ISNULL(b.play_status, '')) = 'checked out' THEN N'checked out'
        WHEN LOWER(ISNULL(b.play_status, '')) = 'checked in'
             AND LOWER(ISNULL(b.extra_payment_status, 'none')) = 'pending extra' THEN N'pending extra'
        WHEN LOWER(ISNULL(b.play_status, '')) = 'checked in' THEN N'checked in'
        WHEN LOWER(ISNULL(b.payment_status, '')) = 'deposited' THEN N'deposited'
        WHEN LOWER(ISNULL(b.payment_status, '')) = 'paid' THEN N'paid'
        ELSE N'pending'
    END
    FROM dbo.Booking b;

    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE parent_object_id = OBJECT_ID(N'dbo.Booking')
          AND name = N'CK_Booking_Status_Values'
    )
    BEGIN
        ALTER TABLE dbo.Booking DROP CONSTRAINT CK_Booking_Status_Values;
    END;

    ALTER TABLE dbo.Booking
        ADD CONSTRAINT CK_Booking_Status_Values
        CHECK (status IN (
            N'pending',
            N'deposited',
            N'paid',
            N'checked in',
            N'pending extra',
            N'checked out',
            N'completed',
            N'cancelled',
            N'pending refund',
            N'refunded'
        ));

    ALTER TABLE dbo.Booking ALTER COLUMN status NVARCHAR(30) NOT NULL;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
    BEGIN
        ROLLBACK TRANSACTION;
    END;

    THROW;
END CATCH;
