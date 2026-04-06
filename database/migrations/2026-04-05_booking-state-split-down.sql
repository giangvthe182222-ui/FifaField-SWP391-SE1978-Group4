SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IDX_Booking_ExtraPaymentStatus'
          AND object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        DROP INDEX IDX_Booking_ExtraPaymentStatus ON dbo.Booking;
    END;

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IDX_Booking_PaymentStatus'
          AND object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        DROP INDEX IDX_Booking_PaymentStatus ON dbo.Booking;
    END;

    IF EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'IDX_Booking_PlayStatus'
          AND object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        DROP INDEX IDX_Booking_PlayStatus ON dbo.Booking;
    END;

    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = 'CK_Booking_ExtraPaymentStatus_Values'
          AND parent_object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        ALTER TABLE dbo.Booking DROP CONSTRAINT CK_Booking_ExtraPaymentStatus_Values;
    END;

    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = 'CK_Booking_PaymentStatus_Values'
          AND parent_object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        ALTER TABLE dbo.Booking DROP CONSTRAINT CK_Booking_PaymentStatus_Values;
    END;

    IF EXISTS (
        SELECT 1
        FROM sys.check_constraints
        WHERE name = 'CK_Booking_PlayStatus_Values'
          AND parent_object_id = OBJECT_ID('dbo.Booking')
    )
    BEGIN
        ALTER TABLE dbo.Booking DROP CONSTRAINT CK_Booking_PlayStatus_Values;
    END;

    IF COL_LENGTH('dbo.Booking', 'extra_payment_status') IS NOT NULL
    BEGIN
        DECLARE @df_extra_name NVARCHAR(128);
        SELECT @df_extra_name = dc.name
        FROM sys.default_constraints dc
        INNER JOIN sys.columns c
            ON c.default_object_id = dc.object_id
        WHERE dc.parent_object_id = OBJECT_ID('dbo.Booking')
          AND c.name = 'extra_payment_status';

        IF @df_extra_name IS NOT NULL
        BEGIN
            EXEC('ALTER TABLE dbo.Booking DROP CONSTRAINT ' + QUOTENAME(@df_extra_name));
        END;

        ALTER TABLE dbo.Booking DROP COLUMN extra_payment_status;
    END;

    IF COL_LENGTH('dbo.Booking', 'payment_status') IS NOT NULL
    BEGIN
        DECLARE @df_payment_name NVARCHAR(128);
        SELECT @df_payment_name = dc.name
        FROM sys.default_constraints dc
        INNER JOIN sys.columns c
            ON c.default_object_id = dc.object_id
        WHERE dc.parent_object_id = OBJECT_ID('dbo.Booking')
          AND c.name = 'payment_status';

        IF @df_payment_name IS NOT NULL
        BEGIN
            EXEC('ALTER TABLE dbo.Booking DROP CONSTRAINT ' + QUOTENAME(@df_payment_name));
        END;

        ALTER TABLE dbo.Booking DROP COLUMN payment_status;
    END;

    IF COL_LENGTH('dbo.Booking', 'play_status') IS NOT NULL
    BEGIN
        DECLARE @df_play_name NVARCHAR(128);
        SELECT @df_play_name = dc.name
        FROM sys.default_constraints dc
        INNER JOIN sys.columns c
            ON c.default_object_id = dc.object_id
        WHERE dc.parent_object_id = OBJECT_ID('dbo.Booking')
          AND c.name = 'play_status';

        IF @df_play_name IS NOT NULL
        BEGIN
            EXEC('ALTER TABLE dbo.Booking DROP CONSTRAINT ' + QUOTENAME(@df_play_name));
        END;

        ALTER TABLE dbo.Booking DROP COLUMN play_status;
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
