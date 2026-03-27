USE FifaFieldDB2;
GO

SET NOCOUNT ON;
GO

DECLARE @Email NVARCHAR(255) = N'admin@gmail.com';
DECLARE @Password NVARCHAR(20) = N'123';
DECLARE @FullName NVARCHAR(255) = N'System Admin';
DECLARE @Status NVARCHAR(20) = N'active';

DECLARE @RoleId VARCHAR(36);
DECLARE @GmailId VARCHAR(36);
DECLARE @UserId VARCHAR(36);

BEGIN TRY
    BEGIN TRAN;

    -- Ensure Admin role exists.
    SELECT TOP 1 @RoleId = role_id
    FROM Role
    WHERE LOWER(role_name) = N'admin';

    IF @RoleId IS NULL
    BEGIN
        SET @RoleId = CONVERT(VARCHAR(36), NEWID());

        INSERT INTO Role(role_id, role_name, description)
        VALUES (@RoleId, N'Admin', N'Quản trị hệ thống');
    END

    -- Ensure Gmail account exists for target email.
    SELECT TOP 1 @GmailId = gmail_id
    FROM Gmail_Account
    WHERE LOWER(email) = LOWER(@Email);

    IF @GmailId IS NULL
    BEGIN
        SET @GmailId = CONVERT(VARCHAR(36), NEWID());

        INSERT INTO Gmail_Account(gmail_id, google_sub, email)
        VALUES (
            @GmailId,
            N'local_admin_' + REPLACE(CONVERT(NVARCHAR(36), NEWID()), N'-', N''),
            @Email
        );
    END

    -- Create user if not exists; otherwise force role/password/status to expected values.
    SELECT TOP 1 @UserId = user_id
    FROM Users
    WHERE gmail_id = @GmailId;

    IF @UserId IS NULL
    BEGIN
        SET @UserId = CONVERT(VARCHAR(36), NEWID());

        INSERT INTO Users(user_id, gmail_id, password, full_name, role_id, status)
        VALUES (@UserId, @GmailId, @Password, @FullName, @RoleId, @Status);
    END
    ELSE
    BEGIN
        UPDATE Users
        SET password = @Password,
            full_name = ISNULL(NULLIF(full_name, N''), @FullName),
            role_id = @RoleId,
            status = @Status
        WHERE user_id = @UserId;
    END

    COMMIT TRAN;

    SELECT N'Admin account is ready: admin@gmail.com / 123' AS message;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRAN;

    THROW;
END CATCH;
GO
