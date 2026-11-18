package vn.edu.library.ui;

import vn.edu.library.dao.LibraryDAO;
import vn.edu.library.model.Book;
import vn.edu.library.model.LoanView;
import vn.edu.library.model.Reader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class LoanForm extends JFrame {
    private final LibraryDAO dao=new LibraryDAO();
    private final JComboBox<ReaderOption> readerBox=new JComboBox<>();
    private final JComboBox<BookOption> bookBox=new JComboBox<>();
    private final JTextField dueDate=new JTextField(LocalDate.now().plusDays(14).toString(),12);
    private final JTextField search=new JTextField(18);
    private final DefaultTableModel model=Ui.model("Mã phiếu","Độc giả","Mã thẻ","Tên sách","SL","Ngày mượn","Hạn trả","Ngày trả","Trạng thái");
    private final JTable table=new JTable(model);

    public LoanForm(){
        Ui.prepareFrame(this,"Quản lý mượn và trả sách",1120,680);
        JPanel root=new JPanel(new BorderLayout(10,10));root.setBackground(Ui.BG);setContentPane(root);
        JPanel heading=new JPanel();Ui.heading(heading,"QUẢN LÝ MƯỢN / TRẢ SÁCH","Lập phiếu mượn, theo dõi hạn trả và ghi nhận sách được trả");root.add(heading,BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(10,10));body.setBackground(Ui.BG);body.setBorder(new EmptyBorder(0,18,18,18));root.add(body,BorderLayout.CENTER);
        JPanel create=Ui.titled("Lập phiếu mượn mới");JPanel fields=new JPanel(new FlowLayout(FlowLayout.LEFT,12,12));fields.setBackground(Color.WHITE);
        fields.add(new JLabel("Độc giả:"));readerBox.setPreferredSize(new Dimension(190,30));fields.add(readerBox);
        fields.add(new JLabel("Sách:"));bookBox.setPreferredSize(new Dimension(260,30));fields.add(bookBox);
        fields.add(new JLabel("Hạn trả (yyyy-MM-dd):"));fields.add(dueDate);
        JButton borrow=Ui.button("Tạo phiếu mượn");borrow.addActionListener(e->createLoan());fields.add(borrow);create.add(fields,BorderLayout.CENTER);
        JPanel list=Ui.titled("Danh sách phiếu mượn / trả");JPanel searchBar=new JPanel(new FlowLayout(FlowLayout.LEFT));searchBar.setBackground(Color.WHITE);
        searchBar.add(new JLabel("Tìm theo mã phiếu / độc giả / sách:"));searchBar.add(search);
        JButton find=Ui.button("Tìm kiếm"),all=Ui.button("Hiển thị tất cả"),ret=Ui.button("Xác nhận trả sách");
        find.addActionListener(e->loadLoans());all.addActionListener(e->{search.setText("");loadLoans();});search.addActionListener(e->loadLoans());ret.addActionListener(e->returnSelected());
        searchBar.add(find);searchBar.add(all);searchBar.add(ret);list.add(searchBar,BorderLayout.NORTH);list.add(Ui.scroll(table),BorderLayout.CENTER);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,create,list);split.setResizeWeight(0.27);split.setBorder(null);body.add(split,BorderLayout.CENTER);
        reloadCombos();loadLoans();
    }

    private void reloadCombos(){
        try{
            readerBox.removeAllItems();for(Reader r:dao.getReaders(""))readerBox.addItem(new ReaderOption(r.id(),r.cardNumber()+" – "+r.fullName()));
            bookBox.removeAllItems();for(Book b:dao.getBooks(""))if(b.availableQuantity()>0)bookBox.addItem(new BookOption(b.id(),b.title()+" (còn "+b.availableQuantity()+")"));
        }catch(Exception ex){Ui.error(this,ex);}
    }
    private void createLoan(){
        try{
            ReaderOption r=(ReaderOption)readerBox.getSelectedItem();BookOption b=(BookOption)bookBox.getSelectedItem();
            if(r==null)throw new IllegalArgumentException("Chưa có độc giả. Hãy thêm độc giả trước.");
            if(b==null)throw new IllegalArgumentException("Không có sách khả dụng để cho mượn.");
            LocalDate due=LocalDate.parse(dueDate.getText().trim());
            dao.createLoan(r.id(),b.id(),due);
            Ui.info(this,"Tạo phiếu mượn thành công. Tồn kho sách đã được cập nhật.");reloadCombos();loadLoans();
        }catch(Exception ex){Ui.error(this,ex);}
    }
    private void loadLoans(){
        try{model.setRowCount(0);List<LoanView> loans=dao.getLoans(search.getText());for(LoanView l:loans)model.addRow(new Object[]{l.loanId(),l.readerName(),l.cardNumber(),l.bookTitle(),l.quantity(),l.loanDate(),l.dueDate(),l.returnDate(),translateStatus(l.status())});}
        catch(Exception ex){Ui.error(this,ex);}
    }
    private void returnSelected(){
        int row=table.getSelectedRow();if(row<0){Ui.info(this,"Hãy chọn phiếu mượn cần trả.");return;}
        int modelRow=table.convertRowIndexToModel(row);int loanId=(int)model.getValueAt(modelRow,0);String status=String.valueOf(model.getValueAt(modelRow,8));
        if(status.equals("Đã trả")){Ui.info(this,"Phiếu này đã được trả.");return;}
        if(Ui.confirm(this,"Xác nhận độc giả đã trả sách của phiếu #"+loanId+"?"))try{dao.returnLoan(loanId);Ui.info(this,"Đã ghi nhận trả sách và cập nhật tồn kho.");reloadCombos();loadLoans();}catch(Exception ex){Ui.error(this,ex);}
    }
    private String translateStatus(String status){return switch(status){case "DA_TRA"->"Đã trả";case "QUA_HAN"->"Quá hạn";default->"Đang mượn";};}
    private record ReaderOption(int id,String label){@Override public String toString(){return label;}}
    private record BookOption(int id,String label){@Override public String toString(){return label;}}
}
