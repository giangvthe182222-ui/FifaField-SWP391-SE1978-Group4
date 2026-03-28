USE FifaFieldDB;
GO

/*
    Seed script for testing supplementary equipment flow
    FIXED VERSION:
    - Set start_time, end_time properly
    - Avoid NULL bug
    - More stable when re-run
*/

SET NOCOUNT ON; 
SET XACT_ABORT ON;

DECLARE @LocationName NVARCHAR(255) = N'Fifafield Ocean Park';
DECLARE @Now DATETIME2 = SYSDATETIME();

DECLARE @LocationId VARCHAR(36);
DECLARE @FieldId VARCHAR(36);
DECLARE @BookerId VARCHAR(36);

DECLARE @ScheduleId VARCHAR(36) = CONVERT(VARCHAR(36), NEWID());
DECLARE @BookingId VARCHAR(36) = CONVERT(VARCHAR(36), NEWID());
DECLARE @PaymentId VARCHAR(36) = CONVERT(VARCHAR(36), NEWID());

DECLARE @PaymentDeadline DATETIME2 = DATEADD(MINUTE, 15, @Now);

DECLARE @BookingDate DATE = CAST(@Now AS DATE);
DECLARE @StartCandidate DATETIME2 = DATEADD(MINUTE, -30, @Now);
DECLARE @EndCandidate DATETIME2 = DATEADD(MINUTE, 90, @Now);

DECLARE @StartTime TIME;
DECLARE @EndTime TIME;

DECLARE @Price DECIMAL(10,2) = 300000;

-- ✅ FIX: set time properly
SET @StartTime = CAST(@StartCandidate AS TIME);
SET @EndTime = CAST(@EndCandidate AS TIME);

-- Safety: ensure valid range
IF @EndTime <= @StartTime
BEGIN
    SET @EndTime = DATEADD(MINUTE, 60, @StartTime);
END;

--------------------------------------------------
-- 1) Find location
--------------------------------------------------
SELECT TOP 1 @LocationId = l.location_id
FROM Location l
WHERE LOWER(ISNULL(l.location_name, '')) LIKE LOWER(N'%' + @LocationName + N'%');

IF @LocationId IS NULL
BEGIN
    RAISERROR(N'Cannot find location.', 16, 1);
    RETURN;
END;

--------------------------------------------------
-- 2) Pick field
--------------------------------------------------
SELECT TOP 1 @FieldId = f.field_id
FROM Field f
WHERE f.location_id = @LocationId
ORDER BY CASE WHEN LOWER(ISNULL(f.status, '')) = 'active' THEN 0 ELSE 1 END;

IF @FieldId IS NULL
BEGIN
    RAISERROR(N'No field found.', 16, 1);
    RETURN;
END;

--------------------------------------------------
-- 3) Pick user
--------------------------------------------------
SELECT TOP 1 @BookerId = c.user_id
FROM Customer c
ORDER BY NEWID();

IF @BookerId IS NULL
BEGIN
    SELECT TOP 1 @BookerId = user_id FROM Users ORDER BY NEWID();
END;

IF @BookerId IS NULL
BEGIN
    RAISERROR(N'No user found.', 16, 1);
    RETURN;
END;

--------------------------------------------------
-- TRANSACTION
--------------------------------------------------
BEGIN TRY
    BEGIN TRAN;

    --------------------------------------------------
    -- 4) Create schedule
    --------------------------------------------------
    INSERT INTO Schedule (
        schedule_id, field_id, booking_date,
        price, start_time, end_time, status
    )
    VALUES (
        @ScheduleId, @FieldId, @BookingDate,
        @Price, @StartTime, @EndTime, N'booked'
    );

    --------------------------------------------------
    -- 5) Create booking
    --------------------------------------------------
    IF COL_LENGTH('Booking', 'payment_deadline') IS NULL
    BEGIN
        INSERT INTO Booking (
            booking_id, booker_id, field_id,
            schedule_id, voucher_id, status, total_price
        )
        VALUES (
            @BookingId, @BookerId, @FieldId,
            @ScheduleId, NULL, N'checked in', @Price
        );
    END
    ELSE
    BEGIN
        INSERT INTO Booking (
            booking_id, booker_id, field_id,
            schedule_id, voucher_id, status,
            total_price, payment_deadline
        )
        VALUES (
            @BookingId, @BookerId, @FieldId,
            @ScheduleId, NULL, N'checked in',
            @Price, @PaymentDeadline
        );
    END;

    --------------------------------------------------
    -- 6) Create payment
    --------------------------------------------------
    INSERT INTO Payment (
        payment_id, booking_id, amount,
        payment_method, payment_status, payment_time
    )
    VALUES (
        @PaymentId, @BookingId, @Price,
        N'cash', N'SUCCESS', @Now
    );

    --------------------------------------------------
    -- 7) Ensure equipment exists
    --------------------------------------------------
    IF EXISTS (
        SELECT 1 FROM Location_Equipment WHERE location_id = @LocationId
    )
    BEGIN
        UPDATE TOP (1) le
        SET
            le.quantity = CASE WHEN ISNULL(le.quantity, 0) <= 0 THEN 10 ELSE le.quantity END,
            le.status = 'available'
        FROM Location_Equipment le
        WHERE le.location_id = @LocationId;
    END
    ELSE
    BEGIN
        PRINT '⚠ No equipment found for this location';
    END;

    COMMIT TRAN;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRAN;

    DECLARE @Err NVARCHAR(4000) = ERROR_MESSAGE();
    RAISERROR(N'Seed failed: %s', 16, 1, @Err);
    RETURN;
END CATCH;

--------------------------------------------------
-- VERIFY
--------------------------------------------------
SELECT
    @LocationId AS location_id,
    @FieldId AS field_id,
    @BookerId AS booker_id,
    @ScheduleId AS schedule_id,
    @BookingId AS booking_id,
    @PaymentId AS payment_id,
    @BookingDate AS booking_date,
    @StartTime AS start_time,
    @EndTime AS end_time,
    @Price AS base_price;

SELECT
    b.booking_id,
    b.status,
    b.total_price,
    p.amount,
    p.payment_status,
    s.start_time,
    s.end_time
FROM Booking b
JOIN Payment p ON p.booking_id = b.booking_id
JOIN Schedule s ON s.schedule_id = b.schedule_id
WHERE b.booking_id = @BookingId;

SELECT TOP 10
    le.location_id,
    le.equipment_id,
    le.quantity,
    le.status
FROM Location_Equipment le
WHERE le.location_id = @LocationId;
GO