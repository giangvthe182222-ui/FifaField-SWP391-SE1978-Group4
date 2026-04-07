SET NOCOUNT ON;
SET XACT_ABORT ON;

DECLARE @TargetFieldName NVARCHAR(255) = N'Sân 7:7 Từ Thiện';
DECLARE @Now DATETIME2 = SYSDATETIME();
DECLARE @Today DATE = CAST(@Now AS DATE);
DECLARE @NowTime TIME = CAST(@Now AS TIME);

DECLARE @LocationId VARCHAR(36);
DECLARE @FieldId VARCHAR(36);
DECLARE @BookerId VARCHAR(36);

DECLARE @ScheduleId VARCHAR(36) = CONVERT(VARCHAR(36), NEWID());
DECLARE @BookingId VARCHAR(36) = CONVERT(VARCHAR(36), NEWID());
DECLARE @PaymentId VARCHAR(36) = CONVERT(VARCHAR(36), NEWID());

DECLARE @PaymentDeadline DATETIME2 = DATEADD(MINUTE, 15, @Now);
DECLARE @StartTime TIME;
DECLARE @EndTime TIME;
DECLARE @Price DECIMAL(10,2);

IF @NowTime < '00:30:00'
BEGIN
    SET @StartTime = '00:00:00';
    SET @EndTime = '01:00:00';
END
ELSE IF @NowTime > '23:30:00'
BEGIN
    SET @StartTime = '23:00:00';
    SET @EndTime = '23:59:00';
END
ELSE
BEGIN
    SET @StartTime = CAST(DATEADD(MINUTE, -30, @Now) AS TIME);
    SET @EndTime = CAST(DATEADD(MINUTE, 30, @Now) AS TIME);
END;

SELECT TOP 1
    @FieldId = f.field_id,
    @LocationId = f.location_id
FROM dbo.Field f
WHERE LOWER(ISNULL(f.field_name, '')) = LOWER(@TargetFieldName)
   OR LOWER(ISNULL(f.field_name, '')) LIKE LOWER(N'%' + @TargetFieldName + N'%')
ORDER BY CASE WHEN LOWER(ISNULL(f.status, 'active')) IN ('active', 'available', 'open') THEN 0 ELSE 1 END;

IF @FieldId IS NULL
BEGIN
    RAISERROR(N'Khong tim thay san %s.', 16, 1, @TargetFieldName);
    RETURN;
END;

SELECT TOP 1 @BookerId = c.user_id
FROM dbo.Customer c
ORDER BY NEWID();

IF @BookerId IS NULL
BEGIN
    SELECT TOP 1 @BookerId = u.user_id
    FROM dbo.Users u
    ORDER BY NEWID();
END;

IF @BookerId IS NULL
BEGIN
    RAISERROR(N'Khong tim thay user de tao booking.', 16, 1);
    RETURN;
END;

SELECT TOP 1 @Price = TRY_CONVERT(DECIMAL(10,2), s.price)
FROM dbo.Schedule s
WHERE s.field_id = @FieldId
  AND s.price IS NOT NULL
  AND TRY_CONVERT(DECIMAL(10,2), s.price) > 0
ORDER BY NEWID();

IF @Price IS NULL OR @Price <= 0
BEGIN
    SET @Price = 300000;
END;

BEGIN TRY
    BEGIN TRANSACTION;

    INSERT INTO dbo.Schedule (
        schedule_id,
        field_id,
        booking_date,
        price,
        start_time,
        end_time,
        status
    )
    VALUES (
        @ScheduleId,
        @FieldId,
        @Today,
        @Price,
        @StartTime,
        @EndTime,
        N'unavailable'
    );

    IF COL_LENGTH('dbo.Booking', 'payment_deadline') IS NOT NULL
    BEGIN
        INSERT INTO dbo.Booking (
            booking_id,
            booker_id,
            field_id,
            schedule_id,
            booking_time,
            play_status,
            payment_status,
            extra_payment_status,
            total_price,
            payment_deadline
        )
        VALUES (
            @BookingId,
            @BookerId,
            @FieldId,
            @ScheduleId,
            @Now,
            N'checked in',
            N'paid',
            N'none',
            @Price,
            @PaymentDeadline
        );
    END
    ELSE
    BEGIN
        INSERT INTO dbo.Booking (
            booking_id,
            booker_id,
            field_id,
            schedule_id,
            booking_time,
            play_status,
            payment_status,
            extra_payment_status,
            total_price
        )
        VALUES (
            @BookingId,
            @BookerId,
            @FieldId,
            @ScheduleId,
            @Now,
            N'checked in',
            N'paid',
            N'none',
            @Price
        );
    END;

    INSERT INTO dbo.Payment (
        payment_id,
        booking_id,
        amount,
        payment_method,
        payment_status,
        payment_time
    )
    VALUES (
        @PaymentId,
        @BookingId,
        @Price,
        N'cash',
        N'SUCCESS',
        @Now
    );

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    DECLARE @Err NVARCHAR(4000) = ERROR_MESSAGE();
    RAISERROR(N'Seed failed: %s', 16, 1, @Err);
    RETURN;
END CATCH;

SELECT
    @BookingId AS booking_id,
    @ScheduleId AS schedule_id,
    @PaymentId AS payment_id,
    @LocationId AS location_id,
    @FieldId AS field_id,
    @BookerId AS booker_id,
    @Today AS booking_date,
    @StartTime AS start_time,
    @EndTime AS end_time,
    @Price AS total_price,
    N'checked in' AS play_status,
    N'paid' AS payment_status,
    N'none' AS extra_payment_status,
    N'checkedin-paid' AS seed_type;

SELECT
    b.booking_id,
    b.play_status,
    b.payment_status,
    b.extra_payment_status,
    b.total_price,
    p.amount,
    p.payment_status AS payment_row_status,
    s.start_time,
    s.end_time
FROM dbo.Booking b
JOIN dbo.Payment p ON p.booking_id = b.booking_id
JOIN dbo.Schedule s ON s.schedule_id = b.schedule_id
WHERE b.booking_id = @BookingId;