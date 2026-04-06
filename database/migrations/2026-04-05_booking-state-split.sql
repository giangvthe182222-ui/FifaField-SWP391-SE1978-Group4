SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.Booking', 'play_status') IS NULL
    BEGIN
        ALTER TABLE dbo.Booking
            ADD play_status NVARCHAR(30) NOT NULL CONSTRAINT DF_Booking_play_status DEFAULT N'booked';
    END;

    IF COL_LENGTH('dbo.Booking', 'payment_status') IS NULL
    BEGIN
        ALTER TABLE dbo.Booking
            ADD payment_status NVARCHAR(30) NOT NULL CONSTRAINT DF_Booking_payment_status DEFAULT N'pending';
    END;

    IF COL_LENGTH('dbo.Booking', 'extra_payment_status') IS NULL
    BEGIN
        ALTER TABLE dbo.Booking
            ADD extra_payment_status NVARCHAR(30) NOT NULL CONSTRAINT DF_Booking_extra_payment_status DEFAULT N'none';
    END;

    UPDATE b
    SET
        play_status = CASE
            WHEN LOWER(ISNULL(b.status, '')) IN ('checked in', 'pending extra') THEN 'checked in'
            WHEN LOWER(ISNULL(b.status, '')) IN ('finished', 'checked out') THEN 'checked out'
            WHEN LOWER(ISNULL(b.status, '')) = 'completed' THEN 'completed'
            WHEN LOWER(ISNULL(b.status, '')) IN ('cancelled', 'pending refund', 'refunded') THEN 'cancelled'
            ELSE 'booked'
        END,
        payment_status = CASE
            WHEN LOWER(ISNULL(b.status, '')) = 'pending' THEN 'pending'
            WHEN LOWER(ISNULL(b.status, '')) = 'deposited' THEN 'deposited'
            WHEN LOWER(ISNULL(b.status, '')) IN ('paid', 'checked in', 'pending extra', 'finished', 'checked out', 'completed') THEN 'paid'
            WHEN LOWER(ISNULL(b.status, '')) = 'pending refund' THEN 'pending refund'
            WHEN LOWER(ISNULL(b.status, '')) = 'refunded' THEN 'refunded'
            WHEN LOWER(ISNULL(b.status, '')) = 'cancelled' THEN 'failed'
            ELSE 'pending'
        END,
        extra_payment_status = CASE
            WHEN LOWER(ISNULL(b.status, '')) = 'pending extra' THEN 'pending extra'
            ELSE 'none'
        END
    FROM dbo.Booking b;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = 'CK_Booking_PlayStatus_Values'
          AND parent_object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        ALTER TABLE dbo.Booking
            ADD CONSTRAINT CK_Booking_PlayStatus_Values
            CHECK (play_status IN (N'booked', N'checked in', N'checked out', N'completed', N'cancelled'));
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = 'CK_Booking_PaymentStatus_Values'
          AND parent_object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        ALTER TABLE dbo.Booking
            ADD CONSTRAINT CK_Booking_PaymentStatus_Values
            CHECK (payment_status IN (N'pending', N'deposited', N'paid', N'pending refund', N'refunded', N'failed'));
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = 'CK_Booking_ExtraPaymentStatus_Values'
          AND parent_object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        ALTER TABLE dbo.Booking
            ADD CONSTRAINT CK_Booking_ExtraPaymentStatus_Values
            CHECK (extra_payment_status IN (N'none', N'pending extra', N'paid extra'));
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IDX_Booking_PlayStatus'
          AND object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        CREATE INDEX IDX_Booking_PlayStatus ON dbo.Booking(play_status);
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IDX_Booking_PaymentStatus'
          AND object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        CREATE INDEX IDX_Booking_PaymentStatus ON dbo.Booking(payment_status);
    END;

    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IDX_Booking_ExtraPaymentStatus'
          AND object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        CREATE INDEX IDX_Booking_ExtraPaymentStatus ON dbo.Booking(extra_payment_status);
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
