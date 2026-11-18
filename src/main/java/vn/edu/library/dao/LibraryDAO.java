package vn.edu.library.dao;

import vn.edu.library.db.Database;
import vn.edu.library.model.Book;
import vn.edu.library.model.Category;
import vn.edu.library.model.LoanView;
import vn.edu.library.model.Reader;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Data-access layer. All SQL is parameterized to avoid SQL injection. */
public class LibraryDAO {

    public List<Category> getCategories() throws SQLException {
        String sql = "SELECT MaTheLoai, TenTheLoai FROM TheLoai ORDER BY TenTheLoai";
        List<Category> result = new ArrayList<>();
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Category(rs.getInt(1), rs.getString(2)));
        }
        return result;
    }

    public List<Book> getBooks(String keyword) throws SQLException {
        String sql = "SELECT s.MaSach,s.MaTheLoai,t.TenTheLoai,s.TenSach,s.TacGia,s.NhaXuatBan," +
                "s.NamXuatBan,s.SoLuongTong,s.SoLuongCon,s.ViTriKe " +
                "FROM Sach s JOIN TheLoai t ON t.MaTheLoai=s.MaTheLoai " +
                "WHERE (? = '' OR s.TenSach LIKE ? OR s.TacGia LIKE ? OR CAST(s.MaSach AS NVARCHAR(20)) LIKE ?) " +
                "ORDER BY s.TenSach";
        String k = keyword == null ? "" : keyword.trim();
        List<Book> result = new ArrayList<>();
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k); ps.setString(2, "%" + k + "%"); ps.setString(3, "%" + k + "%"); ps.setString(4, "%" + k + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new Book(rs.getInt("MaSach"), rs.getInt("MaTheLoai"),
                        rs.getString("TenTheLoai"), rs.getString("TenSach"), rs.getString("TacGia"),
                        rs.getString("NhaXuatBan"), (Integer) rs.getObject("NamXuatBan"),
                        rs.getInt("SoLuongTong"), rs.getInt("SoLuongCon"), rs.getString("ViTriKe")));
            }
        }
        return result;
    }

    public void saveBook(Book b) throws SQLException {
        if (b.title() == null || b.title().isBlank()) throw new SQLException("Ten sach khong duoc de trong.");
        if (b.totalQuantity() < 0) throw new SQLException("So luong khong the am.");
        if (b.id() == 0) {
            String sql = "INSERT INTO Sach(MaTheLoai,TenSach,TacGia,NhaXuatBan,NamXuatBan,SoLuongTong,SoLuongCon,ViTriKe) " +
                    "VALUES(?,?,?,?,?,?,?,?)";
            try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
                bindBook(ps, b, b.totalQuantity(), 1); ps.executeUpdate();
            }
        } else {
            String sql = "UPDATE Sach SET MaTheLoai=?,TenSach=?,TacGia=?,NhaXuatBan=?,NamXuatBan=?,SoLuongTong=?,SoLuongCon=?,ViTriKe=? " +
                    "WHERE MaSach=? AND SoLuongTong-SoLuongCon<=?";
            try (Connection c = Database.getConnection()) {
                c.setAutoCommit(false);
                try {
                    int borrowed;
                    try (PreparedStatement check = c.prepareStatement("SELECT SoLuongTong-SoLuongCon FROM Sach WITH (UPDLOCK,HOLDLOCK) WHERE MaSach=?")) {
                        check.setInt(1, b.id());
                        try (ResultSet rs = check.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Khong tim thay sach.");
                            borrowed = rs.getInt(1);
                        }
                    }
                    if (b.totalQuantity() < borrowed) throw new SQLException("So luong tong khong the nho hon so sach dang duoc muon: " + borrowed);
                    try (PreparedStatement ps = c.prepareStatement("UPDATE Sach SET MaTheLoai=?,TenSach=?,TacGia=?,NhaXuatBan=?,NamXuatBan=?,SoLuongTong=?,SoLuongCon=?,ViTriKe=? WHERE MaSach=?")) {
                        ps.setInt(1,b.categoryId()); ps.setString(2,b.title().trim()); ps.setString(3,blankToNull(b.author()));
                        ps.setString(4,blankToNull(b.publisher())); setNullableInt(ps,5,b.publishYear());
                        ps.setInt(6,b.totalQuantity()); ps.setInt(7,b.totalQuantity()-borrowed);
                        ps.setString(8,blankToNull(b.shelfLocation())); ps.setInt(9,b.id());
                        if (ps.executeUpdate()!=1) throw new SQLException("Cap nhat sach that bai.");
                    }
                    c.commit();
                } catch (SQLException ex) { c.rollback(); throw ex; }
                finally { c.setAutoCommit(true); }
            }
        }
    }

    private void bindBook(PreparedStatement ps, Book b, int available, int start) throws SQLException {
        ps.setInt(start, b.categoryId()); ps.setString(start+1, b.title().trim());
        ps.setString(start+2, blankToNull(b.author())); ps.setString(start+3, blankToNull(b.publisher()));
        setNullableInt(ps,start+4,b.publishYear()); ps.setInt(start+5,b.totalQuantity());
        ps.setInt(start+6,available); ps.setString(start+7,blankToNull(b.shelfLocation()));
    }

    public void deleteBook(int id) throws SQLException {
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM Sach WHERE MaSach=?")) {
            ps.setInt(1,id); if (ps.executeUpdate()==0) throw new SQLException("Khong tim thay sach.");
        } catch (SQLException ex) {
            if (ex.getMessage()!=null && ex.getMessage().contains("REFERENCE constraint"))
                throw new SQLException("Sach da co lich su muon, khong the xoa. Hay giu lai de bao toan lich su.");
            throw ex;
        }
    }

    public List<Reader> getReaders(String keyword) throws SQLException {
        String sql = "SELECT MaDocGia,MaThe,HoTen,NgaySinh,GioiTinh,SDT,Email,DiaChi FROM DocGia " +
                "WHERE (?='' OR MaThe LIKE ? OR HoTen LIKE ? OR SDT LIKE ?) ORDER BY HoTen";
        String k = keyword == null ? "" : keyword.trim();
        List<Reader> result = new ArrayList<>();
        try (Connection c=Database.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,k); ps.setString(2,"%"+k+"%"); ps.setString(3,"%"+k+"%"); ps.setString(4,"%"+k+"%");
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()) result.add(new Reader(rs.getInt("MaDocGia"),rs.getString("MaThe"),rs.getString("HoTen"),
                        rs.getDate("NgaySinh")==null?"":rs.getDate("NgaySinh").toString(),rs.getString("GioiTinh"),
                        rs.getString("SDT"),rs.getString("Email"),rs.getString("DiaChi")));
            }
        }
        return result;
    }

    public void saveReader(Reader r) throws SQLException {
        if (r.cardNumber()==null || r.cardNumber().isBlank() || r.fullName()==null || r.fullName().isBlank())
            throw new SQLException("Ma the va ho ten la bat buoc.");
        java.sql.Date birth = null;
        try { if (r.birthDate()!=null && !r.birthDate().isBlank()) birth=java.sql.Date.valueOf(LocalDate.parse(r.birthDate().trim())); }
        catch (RuntimeException ex) { throw new SQLException("Ngay sinh phai theo dinh dang yyyy-MM-dd."); }
        String sql = r.id()==0
                ? "INSERT INTO DocGia(MaThe,HoTen,NgaySinh,GioiTinh,SDT,Email,DiaChi) VALUES(?,?,?,?,?,?,?)"
                : "UPDATE DocGia SET MaThe=?,HoTen=?,NgaySinh=?,GioiTinh=?,SDT=?,Email=?,DiaChi=? WHERE MaDocGia=?";
        try (Connection c=Database.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,r.cardNumber().trim()); ps.setString(2,r.fullName().trim());
            if (birth==null) ps.setNull(3,Types.DATE); else ps.setDate(3,birth);
            ps.setString(4,blankToNull(r.gender())); ps.setString(5,blankToNull(r.phone()));
            ps.setString(6,blankToNull(r.email())); ps.setString(7,blankToNull(r.address()));
            if (r.id()!=0) ps.setInt(8,r.id());
            ps.executeUpdate();
        }
    }

    public void deleteReader(int id) throws SQLException {
        try (Connection c=Database.getConnection(); PreparedStatement ps=c.prepareStatement("DELETE FROM DocGia WHERE MaDocGia=?")) {
            ps.setInt(1,id); if(ps.executeUpdate()==0) throw new SQLException("Khong tim thay doc gia.");
        } catch(SQLException ex) {
            if(ex.getMessage()!=null && ex.getMessage().contains("REFERENCE constraint"))
                throw new SQLException("Doc gia da co lich su muon, khong the xoa.");
            throw ex;
        }
    }

    public void createLoan(int readerId, int bookId, LocalDate dueDate) throws SQLException {
        if (dueDate==null || dueDate.isBefore(LocalDate.now())) throw new SQLException("Han tra phai tu hom nay tro di.");
        String insertLoan="INSERT INTO PhieuMuon(MaDocGia,NgayMuon,HanTra,TrangThai) VALUES(?,CAST(GETDATE() AS DATE),?,N'DANG_MUON')";
        String insertDetail="INSERT INTO ChiTietPhieuMuon(MaPhieu,MaSach,SoLuong) VALUES(?,?,1)";
        String updateStock="UPDATE Sach SET SoLuongCon=SoLuongCon-1 WHERE MaSach=? AND SoLuongCon>0";
        try(Connection c=Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                int loanId;
                try(PreparedStatement ps=c.prepareStatement(insertLoan,Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1,readerId); ps.setDate(2,java.sql.Date.valueOf(dueDate)); ps.executeUpdate();
                    try(ResultSet keys=ps.getGeneratedKeys()) { if(!keys.next()) throw new SQLException("Khong tao duoc ma phieu muon."); loanId=keys.getInt(1); }
                }
                try(PreparedStatement ps=c.prepareStatement(updateStock)) {
                    ps.setInt(1,bookId); if(ps.executeUpdate()!=1) throw new SQLException("Sach khong ton tai hoac da het.");
                }
                try(PreparedStatement ps=c.prepareStatement(insertDetail)) { ps.setInt(1,loanId); ps.setInt(2,bookId); ps.executeUpdate(); }
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    public List<LoanView> getLoans(String keyword) throws SQLException {
        String sql="SELECT p.MaPhieu,d.HoTen,d.MaThe,s.TenSach,ct.SoLuong,p.NgayMuon,p.HanTra,p.NgayTra, " +
                "CASE WHEN p.NgayTra IS NOT NULL THEN N'DA_TRA' WHEN p.HanTra<CAST(GETDATE() AS DATE) THEN N'QUA_HAN' ELSE N'DANG_MUON' END AS TinhTrang " +
                "FROM PhieuMuon p JOIN DocGia d ON d.MaDocGia=p.MaDocGia " +
                "JOIN ChiTietPhieuMuon ct ON ct.MaPhieu=p.MaPhieu JOIN Sach s ON s.MaSach=ct.MaSach " +
                "WHERE (?='' OR d.HoTen LIKE ? OR d.MaThe LIKE ? OR s.TenSach LIKE ? OR CAST(p.MaPhieu AS NVARCHAR(20)) LIKE ?) " +
                "ORDER BY p.NgayMuon DESC,p.MaPhieu DESC";
        String k=keyword==null?"":keyword.trim(); List<LoanView> result=new ArrayList<>();
        try(Connection c=Database.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,k); for(int i=2;i<=5;i++) ps.setString(i,"%"+k+"%");
            try(ResultSet rs=ps.executeQuery()) { while(rs.next()) result.add(new LoanView(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getInt(5),
                    rs.getDate(6).toString(),rs.getDate(7).toString(),rs.getDate(8)==null?"":rs.getDate(8).toString(),rs.getString(9))); }
        }
        return result;
    }

    public void returnLoan(int loanId) throws SQLException {
        try(Connection c=Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                boolean alreadyReturned;
                try(PreparedStatement ps=c.prepareStatement("SELECT NgayTra FROM PhieuMuon WITH (UPDLOCK,HOLDLOCK) WHERE MaPhieu=?")) {
                    ps.setInt(1,loanId); try(ResultSet rs=ps.executeQuery()) {
                        if(!rs.next()) throw new SQLException("Khong tim thay phieu muon.");
                        alreadyReturned=rs.getDate(1)!=null;
                    }
                }
                if(alreadyReturned) throw new SQLException("Phieu nay da duoc tra truoc do.");
                try(PreparedStatement ps=c.prepareStatement("UPDATE s SET s.SoLuongCon=s.SoLuongCon+ct.SoLuong FROM Sach s JOIN ChiTietPhieuMuon ct ON ct.MaSach=s.MaSach WHERE ct.MaPhieu=?")) {
                    ps.setInt(1,loanId); ps.executeUpdate();
                }
                try(PreparedStatement ps=c.prepareStatement("UPDATE PhieuMuon SET NgayTra=CAST(GETDATE() AS DATE),TrangThai=N'DA_TRA' WHERE MaPhieu=? AND NgayTra IS NULL")) {
                    ps.setInt(1,loanId); if(ps.executeUpdate()!=1) throw new SQLException("Khong cap nhat duoc phieu tra.");
                }
                c.commit();
            } catch(SQLException ex) { c.rollback(); throw ex; }
            finally { c.setAutoCommit(true); }
        }
    }

    public int[] getDashboardStats() throws SQLException {
        String sql="SELECT (SELECT COUNT(*) FROM Sach),(SELECT COALESCE(SUM(SoLuongCon),0) FROM Sach),"+
                "(SELECT COUNT(*) FROM DocGia),(SELECT COUNT(*) FROM PhieuMuon WHERE NgayTra IS NULL),"+
                "(SELECT COUNT(*) FROM PhieuMuon WHERE NgayTra IS NULL AND HanTra<CAST(GETDATE() AS DATE))";
        try(Connection c=Database.getConnection(); Statement st=c.createStatement(); ResultSet rs=st.executeQuery(sql)) {
            if(rs.next()) return new int[]{rs.getInt(1),rs.getInt(2),rs.getInt(3),rs.getInt(4),rs.getInt(5)};
        }
        return new int[5];
    }

    private static String blankToNull(String value) { return value==null || value.isBlank()?null:value.trim(); }
    private static void setNullableInt(PreparedStatement ps,int index,Integer value) throws SQLException {
        if(value==null) ps.setNull(index,Types.INTEGER); else ps.setInt(index,value);
    }
}
