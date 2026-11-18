# ĐỒ ÁN JAVA: QUẢN LÝ THƯ VIỆN

Ứng dụng desktop quản lý thư viện sử dụng **Java 17, Java Swing, JDBC và Microsoft SQL Server**.

## 1. Chức năng đã xây dựng

- Trang chủ: thống kê đầu sách, số lượng sách còn trong kho, độc giả, phiếu đang mượn và phiếu quá hạn.
- Quản lý sách: thêm, sửa, xóa, tìm kiếm theo mã/tên/tác giả; quản lý thể loại và số lượng.
- Quản lý độc giả: thêm, sửa, xóa, tìm kiếm theo mã thẻ/họ tên/số điện thoại.
- Quản lý mượn/trả: tạo phiếu mượn, tìm kiếm phiếu, xác nhận trả sách, tự động cập nhật số lượng tồn.
- Các thao tác tạo phiếu và trả sách dùng transaction để đồng bộ phiếu mượn với tồn kho.

> Phiên bản giao diện hiện tại tạo một phiếu cho một đầu sách mỗi lần thao tác. Thiết kế CSDL vẫn có bảng ChiTietPhieuMuon để mở rộng một phiếu chứa nhiều đầu sách.

## 2. Cấu trúc thư mục

```text
QuanLyThuVien/
├── pom.xml
├── README.md
├── BAO_CAO_DO_AN.md
├── sql/
│   └── QuanLyThuVien.sql
└── src/main/java/vn/edu/library/
    ├── App.java
    ├── dao/LibraryDAO.java
    ├── db/Database.java
    ├── model/Book.java
    ├── model/Category.java
    ├── model/LoanView.java
    ├── model/Reader.java
    └── ui/
        ├── Ui.java
        ├── MainFrame.java
        ├── BookForm.java
        ├── ReaderForm.java
        └── LoanForm.java
```

## 3. Yêu cầu cài đặt

1. Windows 10/11.
2. JDK 17 trở lên.
3. Microsoft SQL Server (Express hoặc Developer) và SQL Server Management Studio (SSMS).
4. Apache Maven 3.8 trở lên.
5. SQL Server bật TCP/IP nếu kết nối qua cổng TCP tĩnh 14331 (theo cấu hình hướng dẫn bên dưới).

## 4. Tạo cơ sở dữ liệu

1. Mở SQL Server Management Studio và kết nối đến SQL Server của máy.
2. Mở file `sql/QuanLyThuVien.sql`.
3. Nhấn **Execute** (hoặc F5). Script tự tạo CSDL `QuanLyThuVien`, 5 bảng, khóa chính, khóa ngoại, ràng buộc và dữ liệu mẫu.
4. Kiểm tra thấy các bảng `TheLoai`, `Sach`, `DocGia`, `PhieuMuon`, `ChiTietPhieuMuon` trong CSDL `QuanLyThuVien`.

## 5. Cấu hình kết nối SQL Server

Ứng dụng dùng JDBC với SQL Server Authentication. Để phù hợp với SQL Server Express cài dưới dạng named instance, hướng dẫn này dùng cổng TCP tĩnh `14331` (có thể chọn cổng khác nếu máy đã sử dụng cổng này).

1. Trong **SQL Server Configuration Manager**, bật `TCP/IP` tại `SQL Server Network Configuration > Protocols for SQLEXPRESS01`.
2. Mở thuộc tính `TCP/IP > IP Addresses`, kéo xuống `IPAll`: xóa giá trị trong `TCP Dynamic Ports`, đặt `TCP Port` là `14331`.
3. Trong SSMS, mở Properties của server > Security, chọn **SQL Server and Windows Authentication mode**.
4. Khởi động lại dịch vụ `SQL Server (SQLEXPRESS01)`.
5. Mở `sql/02_tao_tai_khoan_java.sql`, thay `CHON_MAT_KHAU_MANH` bằng mật khẩu riêng, rồi Execute để tạo login `qltv_app` chỉ có quyền đọc/ghi dữ liệu trong database.

Không gửi mật khẩu cho người khác và không commit mật khẩu vào GitHub.

## 6. Chạy phần mềm

Cài JDK 17 trở lên và Apache Maven 3.8 trở lên. Mở CMD tại thư mục dự án, kiểm tra `java -version` và `mvn -version`, rồi chạy `run-local.bat`. Script sẽ hỏi mật khẩu của `qltv_app`, sau đó chạy `mvn clean compile exec:java`.

URL JDBC được sử dụng: `jdbc:sqlserver://localhost:14331;databaseName=QuanLyThuVien;encrypt=true;trustServerCertificate=true`.
Nếu bạn chọn cổng TCP khác, sửa số `14331` trong `run-local.bat` cho khớp.

Lần chạy đầu tiên Maven cần Internet để tải thư viện JDBC và plugin Maven. Có thể mở thư mục dự án bằng IntelliJ IDEA hoặc NetBeans dưới dạng Maven Project, sau đó chạy lớp `vn.edu.library.App`.

## 7. Kiểm thử nhanh

1. Mở **Quản lý sách**, thêm một cuốn sách, sửa tên/số lượng, tìm kiếm và thử xóa.
2. Mở **Quản lý độc giả**, thêm một độc giả, sửa và tìm theo mã thẻ.
3. Mở **Mượn / trả sách**, chọn độc giả và sách, đặt hạn trả, tạo phiếu. Kiểm tra số lượng còn của sách đã giảm.
4. Chọn phiếu vừa tạo và xác nhận trả. Kiểm tra ngày trả/trạng thái và số lượng sách đã tăng lại.
5. Thử tạo phiếu với sách hết hàng hoặc hạn trả ở quá khứ để kiểm tra validation.
