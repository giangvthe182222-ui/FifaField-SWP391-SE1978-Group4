-- N'Chạy từng cái 1 nhé các daika'

USE FifaFieldDB;
GO

IF NOT EXISTS (SELECT 1 FROM Role WHERE role_name = N'customer')
BEGIN
  INSERT INTO Role(role_id, role_name, description)
  VALUES (CONVERT(VARCHAR(36), NEWID()), N'customer', N'Khách hàng');
END
GO

-- Staff
IF NOT EXISTS (SELECT 1 FROM Role WHERE role_name = N'Staff')
BEGIN
  INSERT INTO Role(role_id, role_name, description)
  VALUES (CONVERT(VARCHAR(36), NEWID()), N'Staff', N'Nhân viên');
END

-- Manager
IF NOT EXISTS (SELECT 1 FROM Role WHERE role_name = N'Manager')
BEGIN
  INSERT INTO Role(role_id, role_name, description)
  VALUES (CONVERT(VARCHAR(36), NEWID()), N'Manager', N'Quản lý');
END

-- Admin
IF NOT EXISTS (SELECT 1 FROM Role WHERE role_name = N'Admin')
BEGIN
  INSERT INTO Role(role_id, role_name, description)
  VALUES (CONVERT(VARCHAR(36), NEWID()), N'Admin', N'Quản trị hệ thống');
END
GO

DECLARE @roleId VARCHAR(36) = (SELECT TOP 1 role_id FROM Role WHERE role_name = N'Admin');
DECLARE @gmailId VARCHAR(36) = CONVERT(VARCHAR(36), NEWID());
DECLARE @userId  VARCHAR(36) = CONVERT(VARCHAR(36), NEWID());

IF NOT EXISTS (SELECT 1 FROM Gmail_Account WHERE email = N'admin@fff.com')
BEGIN
  INSERT INTO Gmail_Account(gmail_id, google_sub, email)
  VALUES(@gmailId, N'local_admin', N'admin@fff.com');

  INSERT INTO Users(user_id, gmail_id, password, full_name, role_id, status)
  VALUES(@userId, @gmailId, N'123456', N'Admin FFF', @roleId, N'active');
END
GO
