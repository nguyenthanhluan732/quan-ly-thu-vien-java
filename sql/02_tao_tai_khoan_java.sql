
USE master;
GO
IF NOT EXISTS (SELECT 1 FROM sys.sql_logins WHERE name = N'qltv_app')
BEGIN
    CREATE LOGIN qltv_app WITH PASSWORD = N'tu tao mat khau';
END;
GO
USE QuanLyThuVien;
GO
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'qltv_app')
BEGIN
    CREATE USER qltv_app FOR LOGIN qltv_app;
END;
GO
ALTER ROLE db_datareader ADD MEMBER qltv_app;
ALTER ROLE db_datawriter ADD MEMBER qltv_app;
GO
SELECT name, type_desc FROM sys.database_principals WHERE name = N'qltv_app';
GO
