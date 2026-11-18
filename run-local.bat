@echo off
setlocal
cd /d "%~dp0"

where mvn >nul 2>nul
if errorlevel 1 (
    echo Khong tim thay Maven. Hay cai Maven va them vao PATH truoc.
    pause
    exit /b 1
)

set "LIBRARY_DB_URL=jdbc:sqlserver://localhost:14331;databaseName=QuanLyThuVien;encrypt=true;trustServerCertificate=true"
set "LIBRARY_DB_USER=qltv_app"
set /p "LIBRARY_DB_PASSWORD=Nhap mat khau SQL cua qltv_app (tu tao mat khau trong file sql): "
if "%LIBRARY_DB_PASSWORD%"=="" (
    echo Mat khau khong duoc de trong.
    pause
    exit /b 1
)

echo Dang bien dich va khoi dong Quan Ly Thu Vien...
mvn clean compile exec:java
pause
