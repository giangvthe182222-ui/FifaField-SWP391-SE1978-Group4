USE FifaFieldDB;
GO

/*
    Seed script for testing supplementary equipment flow at location FIFA Q7.
    What this script does each time it runs:
    1) Finds location by name containing 'FIFA Q7'.
    2) Picks an active field in that location.
    3) Creates a schedule covering current time (so booking is active now).
    4) Creates a booking in status 'checked in'.
    5) Creates payment in status 'SUCCESS'.
    6) Ensures at least one equipment row at that location is available for add-equipment testing.
*/

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
DECLARE @StartCandidate DATETIME2 = DATEADD(MINUTE,-0, @Now);
DECLARE @EndCandidate DATETIME2 = DATEADD(MINUTE, 100, @Now);
DECLARE @StartTime TIME;
DECLARE @EndTime TIME;
DECLARE @Price DECIMAL(10,2) = 300000;



-- 1) Find location FIFA Q7
SELECT TOP 1 @LocationId = l.location_id
FROM Location l
WHERE LOWER(ISNULL(l.location_name, '')) LIKE LOWER(N'%' + @LocationName + N'%');

IF @LocationId IS NULL
BEGIN
    RAISERROR(N'Cannot find location containing configured location name.', 16, 1);
    RETURN;
END;

-- 2) Pick field in this location (prefer active)
SELECT TOP 1 @FieldId = f.field_id
FROM Field f
WHERE f.location_id = @LocationId
ORDER BY CASE WHEN LOWER(ISNULL(f.status, '')) = 'active' THEN 0 ELSE 1 END, f.field_name;

IF @FieldId IS NULL
BEGIN
    RAISERROR(N'No field found under configured location.', 16, 1);
    RETURN;
END;

-- 3) Pick one customer (fallback to any user)
SELECT TOP 1 @BookerId = c.user_id
FROM Customer c
JOIN Users u ON u.user_id = c.user_id
ORDER BY u.created_at DESC;

IF @BookerId IS NULL
BEGIN
    SELECT TOP 1 @BookerId = u.user_id
    FROM Users u
    ORDER BY u.created_at DESC;
END;

IF @BookerId IS NULL
BEGIN
    RAISERROR(N'No user/customer found to create booking.', 16, 1);
    RETURN;
END;

BEGIN TRY
    BEGIN TRAN;

    -- 4) Create schedule active at current time
    INSERT INTO Schedule (schedule_id, field_id, booking_date, price, start_time, end_time, status)
    VALUES (@ScheduleId, @FieldId, @BookingDate, @Price, @StartTime, @EndTime, N'unavailable');

    -- 5) Create checked-in booking
    IF COL_LENGTH('Booking', 'payment_deadline') IS NULL
    BEGIN
        INSERT INTO Booking (booking_id, booker_id, field_id, schedule_id, voucher_id, status, total_price)
        VALUES (@BookingId, @BookerId, @FieldId, @ScheduleId, NULL, N'checked in', @Price);
    END
    ELSE
    BEGIN
        INSERT INTO Booking (booking_id, booker_id, field_id, schedule_id, voucher_id, status, total_price, payment_deadline)
        VALUES (@BookingId, @BookerId, @FieldId, @ScheduleId, NULL, N'checked in', @Price, @PaymentDeadline);
    END;

    -- 6) Create success payment record
    INSERT INTO Payment (payment_id, booking_id, amount, payment_method, payment_status, payment_time)
    VALUES (@PaymentId, @BookingId, @Price, N'cash', N'SUCCESS', @Now);

    -- 7) Ensure at least one equipment is available at this location for testing add equipment
    UPDATE TOP (1) le
    SET le.quantity = CASE WHEN ISNULL(le.quantity, 0) <= 0 THEN 10 ELSE le.quantity END,
        le.status = 'available'
    FROM Location_Equipment le
    WHERE le.location_id = @LocationId;

    COMMIT TRAN;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK TRAN;

    DECLARE @Err NVARCHAR(4000) = ERROR_MESSAGE();
    RAISERROR(N'Failed to seed checked-in booking for FIFA Q7: %s', 16, 1, @Err);
    RETURN;
END CATCH;

-- 8) Verification output
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
    p.amount AS payment_amount,
    p.payment_status,
    s.booking_date,
    s.start_time,
    s.end_time,
    l.location_name,
    f.field_name
FROM Booking b
JOIN Payment p ON p.booking_id = b.booking_id
JOIN Schedule s ON s.schedule_id = b.schedule_id
JOIN Field f ON f.field_id = b.field_id
JOIN Location l ON l.location_id = f.location_id
WHERE b.booking_id = @BookingId;

SELECT TOP 10
    le.location_id,
    l.location_name,
    le.equipment_id,
    e.name AS equipment_name,
    le.quantity,
    le.status
FROM Location_Equipment le
JOIN Equipment e ON e.equipment_id = le.equipment_id
JOIN Location l ON l.location_id = le.location_id
WHERE le.location_id = @LocationId
ORDER BY e.name;
GO
