
IF DB_ID(N'QuanLyThuVien') IS NULL
    CREATE DATABASE QuanLyThuVien;
GO

USE QuanLyThuVien;
GO

IF OBJECT_ID(N'dbo.TheLoai', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TheLoai (
        MaTheLoai INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_TheLoai PRIMARY KEY,
        TenTheLoai NVARCHAR(100) NOT NULL,
        MoTa NVARCHAR(300) NULL,
        CONSTRAINT UQ_TheLoai_Ten UNIQUE (TenTheLoai)
    );
END;
GO

/* 2. SACH */
IF OBJECT_ID(N'dbo.Sach', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Sach (
        MaSach INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Sach PRIMARY KEY,
        MaTheLoai INT NOT NULL,
        ISBN VARCHAR(20) NULL,
        TenSach NVARCHAR(200) NOT NULL,
        TacGia NVARCHAR(150) NULL,
        NhaXuatBan NVARCHAR(150) NULL,
        NamXuatBan INT NULL,
        SoLuongTong INT NOT NULL CONSTRAINT DF_Sach_SoLuongTong DEFAULT(0),
        SoLuongCon INT NOT NULL CONSTRAINT DF_Sach_SoLuongCon DEFAULT(0),
        ViTriKe NVARCHAR(50) NULL,
        CONSTRAINT FK_Sach_TheLoai FOREIGN KEY (MaTheLoai) REFERENCES dbo.TheLoai(MaTheLoai),
        CONSTRAINT CK_Sach_SoLuong CHECK (SoLuongTong >= 0 AND SoLuongCon >= 0 AND SoLuongCon <= SoLuongTong),
        CONSTRAINT CK_Sach_Nam CHECK (NamXuatBan IS NULL OR NamXuatBan BETWEEN 1000 AND 2100)
    );
    CREATE INDEX IX_Sach_TenSach ON dbo.Sach(TenSach);
END;
GO

/* 3. DOC GIA */
IF OBJECT_ID(N'dbo.DocGia', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.DocGia (
        MaDocGia INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_DocGia PRIMARY KEY,
        MaThe VARCHAR(30) NOT NULL,
        HoTen NVARCHAR(150) NOT NULL,
        NgaySinh DATE NULL,
        GioiTinh NVARCHAR(10) NULL,
        SDT VARCHAR(20) NULL,
        Email VARCHAR(120) NULL,
        DiaChi NVARCHAR(250) NULL,
        NgayDangKy DATE NOT NULL CONSTRAINT DF_DocGia_NgayDangKy DEFAULT(CONVERT(DATE, GETDATE())),
        CONSTRAINT UQ_DocGia_MaThe UNIQUE(MaThe),
        CONSTRAINT CK_DocGia_GioiTinh CHECK (GioiTinh IS NULL OR GioiTinh IN (N'Nam',N'Nữ',N'Khác'))
    );
    CREATE INDEX IX_DocGia_HoTen ON dbo.DocGia(HoTen);
END;
GO

/* 4. PHIEU MUON - moi phieu co the chua nhieu dau sach */
IF OBJECT_ID(N'dbo.PhieuMuon', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.PhieuMuon (
        MaPhieu INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_PhieuMuon PRIMARY KEY,
        MaDocGia INT NOT NULL,
        NgayMuon DATE NOT NULL CONSTRAINT DF_PhieuMuon_NgayMuon DEFAULT(CONVERT(DATE, GETDATE())),
        HanTra DATE NOT NULL,
        NgayTra DATE NULL,
        TrangThai NVARCHAR(20) NOT NULL CONSTRAINT DF_PhieuMuon_TrangThai DEFAULT(N'DANG_MUON'),
        GhiChu NVARCHAR(300) NULL,
        CONSTRAINT FK_PhieuMuon_DocGia FOREIGN KEY (MaDocGia) REFERENCES dbo.DocGia(MaDocGia),
        CONSTRAINT CK_PhieuMuon_Ngay CHECK (HanTra >= NgayMuon AND (NgayTra IS NULL OR NgayTra >= NgayMuon)),
        CONSTRAINT CK_PhieuMuon_TrangThai CHECK (TrangThai IN (N'DANG_MUON',N'DA_TRA'))
    );
END;
GO

/* 5. CHI TIET PHIEU MUON */
IF OBJECT_ID(N'dbo.ChiTietPhieuMuon', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.ChiTietPhieuMuon (
        MaChiTiet INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_ChiTietPhieuMuon PRIMARY KEY,
        MaPhieu INT NOT NULL,
        MaSach INT NOT NULL,
        SoLuong INT NOT NULL CONSTRAINT DF_ChiTiet_SoLuong DEFAULT(1),
        CONSTRAINT FK_ChiTiet_Phieu FOREIGN KEY (MaPhieu) REFERENCES dbo.PhieuMuon(MaPhieu),
        CONSTRAINT FK_ChiTiet_Sach FOREIGN KEY (MaSach) REFERENCES dbo.Sach(MaSach),
        CONSTRAINT UQ_ChiTiet_Phieu_Sach UNIQUE(MaPhieu,MaSach),
        CONSTRAINT CK_ChiTiet_SoLuong CHECK (SoLuong > 0)
    );
END;
GO

/* DU LIEU MAU - chi chen neu bang dang rong */
IF NOT EXISTS (SELECT 1 FROM dbo.TheLoai)
BEGIN
    INSERT INTO dbo.TheLoai(TenTheLoai,MoTa) VALUES
    (N'Văn học',N'Tiểu thuyết, truyện ngắn, thơ'),
    (N'Khoa học - Công nghệ',N'Sách khoa học và công nghệ thông tin'),
    (N'Kinh tế',N'Quản trị, kinh doanh, tài chính'),
    (N'Kỹ năng sống',N'Phát triển bản thân và kỹ năng');
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Sach)
BEGIN
    INSERT INTO dbo.Sach(MaTheLoai,TenSach,TacGia,NhaXuatBan,NamXuatBan,SoLuongTong,SoLuongCon,ViTriKe)
    SELECT MaTheLoai,N'Lập trình Java cơ bản',N'Nguyễn Văn A',N'Giáo dục',2023,10,10,N'A01' FROM dbo.TheLoai WHERE TenTheLoai=N'Khoa học - Công nghệ';
    INSERT INTO dbo.Sach(MaTheLoai,TenSach,TacGia,NhaXuatBan,NamXuatBan,SoLuongTong,SoLuongCon,ViTriKe)
    SELECT MaTheLoai,N'Cấu trúc dữ liệu và giải thuật',N'Trần Văn B',N'Khoa học và Kỹ thuật',2022,7,7,N'A02' FROM dbo.TheLoai WHERE TenTheLoai=N'Khoa học - Công nghệ';
    INSERT INTO dbo.Sach(MaTheLoai,TenSach,TacGia,NhaXuatBan,NamXuatBan,SoLuongTong,SoLuongCon,ViTriKe)
    SELECT MaTheLoai,N'Dế Mèn phiêu lưu ký',N'Tô Hoài',N'Kim Đồng',2020,5,5,N'B01' FROM dbo.TheLoai WHERE TenTheLoai=N'Văn học';
END;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.DocGia)
BEGIN
    INSERT INTO dbo.DocGia(MaThe,HoTen,NgaySinh,GioiTinh,SDT,Email,DiaChi) VALUES
    ('DG001',N'Nguyễn Minh Anh','2005-04-12',N'Nữ','0900000001','minhanh@example.com',N'Hà Nội'),
    ('DG002',N'Trần Quốc Bảo','2004-09-20',N'Nam','0900000002','quocbao@example.com',N'Hà Nội'),
    ('DG003',N'Lê Thu Hà','2006-01-05',N'Nữ','0900000003','thuha@example.com',N'Bắc Ninh');
END;
GO

/* KIEM TRA SAU KHI TAO */
SELECT N'TheLoai' AS TenBang, COUNT(*) AS SoBanGhi FROM dbo.TheLoai
UNION ALL SELECT N'Sach', COUNT(*) FROM dbo.Sach
UNION ALL SELECT N'DocGia', COUNT(*) FROM dbo.DocGia
UNION ALL SELECT N'PhieuMuon', COUNT(*) FROM dbo.PhieuMuon
UNION ALL SELECT N'ChiTietPhieuMuon', COUNT(*) FROM dbo.ChiTietPhieuMuon;
GO
