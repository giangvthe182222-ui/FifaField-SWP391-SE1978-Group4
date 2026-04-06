SET NOCOUNT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID(N'dbo.Booking', N'U') IS NULL
    BEGIN
        THROW 51000, 'Table dbo.Booking does not exist.', 1;
    END;

    IF COL_LENGTH('dbo.Booking', 'status') IS NOT NULL
    BEGIN
        IF OBJECT_ID(N'dbo.Booking_Status_Backup_20260406', N'U') IS NULL
        BEGIN
            CREATE TABLE dbo.Booking_Status_Backup_20260406 (
                booking_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
                status NVARCHAR(30) NULL,
                backed_up_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
            );
        END;

        DELETE FROM dbo.Booking_Status_Backup_20260406;

        INSERT INTO dbo.Booking_Status_Backup_20260406 (booking_id, status)
        SELECT booking_id, status
        FROM dbo.Booking;

        DECLARE @dropSql NVARCHAR(MAX);

        -- Drop default constraints bound to Booking.status.
        SELECT @dropSql = STRING_AGG(
            N'ALTER TABLE dbo.Booking DROP CONSTRAINT ' + QUOTENAME(dc.name),
            N';'
        )
        FROM sys.default_constraints dc
        INNER JOIN sys.columns c
            ON c.object_id = dc.parent_object_id
           AND c.column_id = dc.parent_column_id
        WHERE dc.parent_object_id = OBJECT_ID(N'dbo.Booking')
          AND c.name = N'status';

        IF @dropSql IS NOT NULL AND LEN(@dropSql) > 0
        BEGIN
            EXEC sp_executesql @dropSql;
        END;

        -- Drop check constraints that still reference the old status column.
        SELECT @dropSql = STRING_AGG(
            N'ALTER TABLE dbo.Booking DROP CONSTRAINT ' + QUOTENAME(cc.name),
            N';'
        )
        FROM sys.check_constraints cc
        WHERE cc.parent_object_id = OBJECT_ID(N'dbo.Booking')
          AND cc.definition LIKE N'%[[]status[]]%';

        IF @dropSql IS NOT NULL AND LEN(@dropSql) > 0
        BEGIN
            EXEC sp_executesql @dropSql;
        END;

        ALTER TABLE dbo.Booking DROP COLUMN status;
    END;

    COMMIT TRANSACTION;
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0
    BEGIN
        ROLLBACK TRANSACTION;
    END;

    THROW;
END CATCH;
