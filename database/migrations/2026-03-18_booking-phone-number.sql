SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.Booking', 'phone_number') IS NULL
    BEGIN
        ALTER TABLE dbo.Booking
            ADD phone_number NVARCHAR(20) NULL;
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    THROW;
END CATCH;
GO  -- 👈 QUAN TRỌNG

-- chạy batch mới
UPDATE b
SET b.phone_number = u.phone
FROM dbo.Booking b
INNER JOIN dbo.Users u ON u.user_id = b.booker_id
WHERE (b.phone_number IS NULL OR LTRIM(RTRIM(b.phone_number)) = '')
  AND u.phone IS NOT NULL
  AND LTRIM(RTRIM(u.phone)) <> '';