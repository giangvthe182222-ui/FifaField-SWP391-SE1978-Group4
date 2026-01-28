CREATE DATABASE FifaFieldDB;
GO
USE FifaFieldDB;
GO

CREATE TABLE Role (
    role_id VARCHAR(36) PRIMARY KEY,
    role_name NVARCHAR(50) NOT NULL UNIQUE,
	description NVARCHAR(255),
);
GO

CREATE TABLE Gmail_Account (
    gmail_id VARCHAR(36) PRIMARY KEY,
    google_sub NVARCHAR(255) NOT NULL UNIQUE,
    email NVARCHAR(255) NOT NULL UNIQUE
);
GO

CREATE TABLE Users (
    user_id VARCHAR(36) PRIMARY KEY,
    gmail_id VARCHAR(36) NOT NULL,
    password NVARCHAR(20),
    full_name NVARCHAR(255),
    phone CHAR(20),
    address NVARCHAR(255),
    gender NVARCHAR(20),
    role_id VARCHAR(36) NOT NULL,
    status NVARCHAR(20) DEFAULT N'active',
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_User_Gmail FOREIGN KEY (gmail_id) REFERENCES Gmail_Account(gmail_id),
    CONSTRAINT FK_User_Role FOREIGN KEY (role_id) REFERENCES Role(role_id)
);
GO

CREATE TABLE Customer (
    user_id VARCHAR(36) PRIMARY KEY,
    loyalty_points INT DEFAULT 0,
    date_of_birth DATE,
    CONSTRAINT FK_Customer_User FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
GO

CREATE TABLE Manager (
    user_id VARCHAR(36) PRIMARY KEY,
    start_date DATE,
	location_id VARCHAR(36),
    CONSTRAINT FK_Manager_User FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
GO

CREATE TABLE Location (
    location_id VARCHAR(36) PRIMARY KEY,
    location_name NVARCHAR(255) NOT NULL,
    address NVARCHAR(255),
	latitude NVARCHAR(255),
	longtitude NVARCHAR(255),
	image_url NVARCHAR(255),
	status NVARCHAR(50),
    manager_id VARCHAR(36) NOT NULL,
    CONSTRAINT FK_Location_Manager FOREIGN KEY (manager_id) REFERENCES Manager(user_id)
);
GO

CREATE TABLE Staff (
    user_id VARCHAR(36) PRIMARY KEY,
    employee_code NVARCHAR(50) UNIQUE,
    hire_date DATE,
    status NVARCHAR(50),
    location_id VARCHAR(36) NOT NULL,
    CONSTRAINT FK_Staff_User FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT FK_Staff_Location FOREIGN KEY (location_id) REFERENCES Location(location_id)
);
GO

CREATE TABLE Field (
    field_id VARCHAR(36) PRIMARY KEY,
    field_name NVARCHAR(255) NOT NULL,
    field_type NVARCHAR(20) CHECK (field_type IN (N'7-a-side', N'11-a-side')),
    image_url NVARCHAR(255) NOT NULL,
	status NVARCHAR(50) NOT NULL,
	condition NVARCHAR(50) NOT NULL,
    location_id VARCHAR(36) NOT NULL,
    CONSTRAINT FK_Field_Location FOREIGN KEY (location_id) REFERENCES Location(location_id)
);
GO

CREATE TABLE Schedule (
    schedule_id VARCHAR(36) PRIMARY KEY,
    field_id VARCHAR(36) NOT NULL,
    booking_date DATE NOT NULL,
	price DECIMAL NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BIT DEFAULT 1,
    CONSTRAINT FK_Schedule_Field FOREIGN KEY (field_id) REFERENCES Field(field_id)
);
GO

CREATE TABLE Equipment (
    equipment_id VARCHAR(36) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    equipment_type NVARCHAR(50),
	image_url NVARCHAR(255) NOT NULL,
    rental_price DECIMAL(10,2) NOT NULL,
    damage_fee DECIMAL(10,2),
	description VARCHAR(255) NOT NULL,
    status NVARCHAR(20) DEFAULT N'active'
);
GO

CREATE TABLE Field_Equipment (
    field_id VARCHAR(36) NOT NULL,
    equipment_id VARCHAR(36) NOT NULL,
    quantity INT CHECK (quantity >= 0),
	status VARCHAR(255) NOT NULL,
	condition VARCHAR(255) NOT NULL,
    CONSTRAINT PK_Field_Equipment PRIMARY KEY (field_id, equipment_id),
    CONSTRAINT FK_FE_Field FOREIGN KEY (field_id) REFERENCES Field(field_id),
    CONSTRAINT FK_FE_Equipment FOREIGN KEY (equipment_id) REFERENCES Equipment(equipment_id)
);
GO

CREATE TABLE Shift (
    shift_id VARCHAR(36) PRIMARY KEY,
    shift_name NVARCHAR(50),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);
GO

CREATE TABLE Staff_Shift (
    staff_id VARCHAR(36) NOT NULL,
    field_id VARCHAR(36) NOT NULL,
    shift_id VARCHAR(36) NOT NULL,
    working_date DATE NOT NULL,
    assigned_by VARCHAR(36) NOT NULL,
    status NVARCHAR(20) DEFAULT N'assigned',
    CONSTRAINT PK_Staff_Shift PRIMARY KEY (staff_id, field_id, shift_id, working_date),
    CONSTRAINT FK_SS_Staff FOREIGN KEY (staff_id) REFERENCES Staff(user_id),
    CONSTRAINT FK_SS_Field FOREIGN KEY (field_id) REFERENCES Field(field_id),
    CONSTRAINT FK_SS_Shift FOREIGN KEY (shift_id) REFERENCES Shift(shift_id),
    CONSTRAINT FK_SS_Manager FOREIGN KEY (assigned_by) REFERENCES Manager(user_id)
);
GO

CREATE TABLE Voucher (
    voucher_id VARCHAR(36) PRIMARY KEY,
    code NVARCHAR(50) UNIQUE,
    discount_value DECIMAL(5,2),
	description NVARCHAR(255),
    start_date DATE,
	end_date DATE,
	used_count INT DEFAULT 0,
    status NVARCHAR(20)
);
GO

CREATE TABLE Customer_Voucher (
    customer_id VARCHAR(36) NOT NULL,
    voucher_id VARCHAR(36) NOT NULL,
    is_used BIT DEFAULT 0,
    CONSTRAINT PK_Customer_Voucher PRIMARY KEY (customer_id, voucher_id),
    CONSTRAINT FK_CV_Customer FOREIGN KEY (customer_id) REFERENCES Customer(user_id),
    CONSTRAINT FK_CV_Voucher FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id)
);
GO

CREATE TABLE Booking (
    booking_id VARCHAR(36) PRIMARY KEY,
    booker_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NULL,
    schedule_id VARCHAR(36) NOT NULL,
    field_id VARCHAR(36) NOT NULL,
    voucher_id VARCHAR(36) NULL,
    booking_time DATETIME2 DEFAULT SYSDATETIME(),
    status NVARCHAR(30),
    total_price DECIMAL(10,2),
    CONSTRAINT FK_Booking_Booker FOREIGN KEY (booker_id) REFERENCES Users(user_id),
    CONSTRAINT FK_Booking_Customer FOREIGN KEY (customer_id) REFERENCES Customer(user_id),
    CONSTRAINT FK_Booking_Field FOREIGN KEY (field_id) REFERENCES Field(field_id),
    CONSTRAINT FK_Booking_Schedule FOREIGN KEY (schedule_id) REFERENCES Schedule(schedule_id),
    CONSTRAINT FK_Booking_Voucher FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id)
);
GO

CREATE TABLE Booking_Equipment (
    booking_id VARCHAR(36) NOT NULL,
    equipment_id VARCHAR(36) NOT NULL,
    quantity INT CHECK (quantity > 0),
    CONSTRAINT PK_Booking_Equipment PRIMARY KEY (booking_id, equipment_id),
    CONSTRAINT FK_BE_Booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
    CONSTRAINT FK_BE_Equipment FOREIGN KEY (equipment_id) REFERENCES Equipment(equipment_id)
);
GO

CREATE TABLE Payment (
    payment_id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL,
    amount DECIMAL(10,2),
    payment_method NVARCHAR(50),
    payment_status NVARCHAR(30),
    payment_time DATETIME2,
    CONSTRAINT FK_Payment_Booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id)
);
GO

CREATE TABLE Invoice (
    invoice_id VARCHAR(36) PRIMARY KEY,
    payment_id VARCHAR(36) NOT NULL,
    created_by VARCHAR(36) NOT NULL,
    issued_date DATETIME2 DEFAULT SYSDATETIME(),
    total_amount DECIMAL(10,2),
    CONSTRAINT FK_Invoice_Payment FOREIGN KEY (payment_id) REFERENCES Payment(payment_id),
    CONSTRAINT FK_Invoice_Staff FOREIGN KEY (created_by) REFERENCES Staff(user_id)
);
GO

CREATE TABLE Feedback (
    feedback_id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment NVARCHAR(MAX),
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Feedback_Booking FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
    CONSTRAINT FK_Feedback_Customer FOREIGN KEY (customer_id) REFERENCES Customer(user_id)
);
GO
