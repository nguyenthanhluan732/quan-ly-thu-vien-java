package vn.edu.library.ui;

import vn.edu.library.dao.LibraryDAO;
import vn.edu.library.model.Book;
import vn.edu.library.model.Category;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookForm extends JFrame {
    private final LibraryDAO dao=new LibraryDAO();
    private final JComboBox<Category> category=new JComboBox<>();
    private final JTextField title=new JTextField(22),author=new JTextField(22),publisher=new JTextField(22),year=new JTextField(8),quantity=new JTextField("1",8),shelf=new JTextField(12),search=new JTextField(18);
    private final DefaultTableModel tableModel=Ui.model("Mã sách","Tên sách","Tác giả","Thể loại","NXB","Năm","Tổng","Còn","Vị trí kệ");
    private final JTable table=new JTable(tableModel);
    private int selectedId=0;

    public BookForm(){
        Ui.prepareFrame(this,"Quản lý sách",1120,700);
        JPanel root=new JPanel(new BorderLayout(10,10));root.setBackground(Ui.BG);setContentPane(root);
        JPanel heading=new JPanel();Ui.heading(heading,"QUẢN LÝ SÁCH","Thêm, cập nhật, xóa và tìm kiếm đầu sách trong thư viện");root.add(heading,BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(10,10));body.setBackground(Ui.BG);body.setBorder(new EmptyBorder(0,18,18,18));root.add(body,BorderLayout.CENTER);
        JPanel form=Ui.titled("Thông tin sách");JPanel fields=Ui.formGrid();
        Ui.addField(fields,0,"Tên sách *",title);Ui.addField(fields,1,"Thể loại *",category);Ui.addField(fields,2,"Tác giả",author);
        Ui.addField(fields,3,"Nhà xuất bản",publisher);Ui.addField(fields,4,"Năm xuất bản",year);Ui.addField(fields,5,"Số lượng tổng *",quantity);Ui.addField(fields,6,"Vị trí kệ",shelf);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));actions.setBackground(Color.WHITE);
        JButton save=Ui.button("Lưu (thêm / sửa)"),delete=Ui.button("Xóa"),clear=Ui.button("Làm mới");
        save.addActionListener(e->saveBook());delete.addActionListener(e->deleteBook());clear.addActionListener(e->clearForm());
        actions.add(save);actions.add(delete);actions.add(clear);form.add(fields,BorderLayout.CENTER);form.add(actions,BorderLayout.SOUTH);
        JPanel list=Ui.titled("Danh sách sách");JPanel searchBar=new JPanel(new FlowLayout(FlowLayout.LEFT));searchBar.setBackground(Color.WHITE);searchBar.add(new JLabel("Tìm theo mã / tên / tác giả:"));searchBar.add(search);
        JButton find=Ui.button("Tìm kiếm"),all=Ui.button("Hiển thị tất cả");find.addActionListener(e->loadBooks());all.addActionListener(e->{search.setText("");loadBooks();});search.addActionListener(e->loadBooks());searchBar.add(find);searchBar.add(all);
        list.add(searchBar,BorderLayout.NORTH);list.add(Ui.scroll(table),BorderLayout.CENTER);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,form,list);split.setResizeWeight(0.43);split.setBorder(null);body.add(split,BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&table.getSelectedRow()>=0)selectBook();});
        loadCategories();loadBooks();
    }

    private void loadCategories(){try{category.removeAllItems();for(Category c:dao.getCategories())category.addItem(c);}catch(Exception ex){Ui.error(this,ex);}}
    private void loadBooks(){try{tableModel.setRowCount(0);List<Book> books=dao.getBooks(search.getText());for(Book b:books)tableModel.addRow(new Object[]{b.id(),b.title(),b.author(),b.categoryName(),b.publisher(),b.publishYear()==null?"":b.publishYear(),b.totalQuantity(),b.availableQuantity(),b.shelfLocation()});}
        catch(Exception ex){Ui.error(this,ex);}}
    private void selectBook(){
        try{int view=table.getSelectedRow();int id=(int)table.getValueAt(view,0);for(Book b:dao.getBooks(""))if(b.id()==id){selectedId=id;title.setText(b.title());author.setText(nvl(b.author()));publisher.setText(nvl(b.publisher()));year.setText(b.publishYear()==null?"":b.publishYear().toString());quantity.setText(String.valueOf(b.totalQuantity()));shelf.setText(nvl(b.shelfLocation()));for(int i=0;i<category.getItemCount();i++)if(category.getItemAt(i).id()==b.categoryId())category.setSelectedIndex(i);break;}}
        catch(Exception ex){Ui.error(this,ex);}
    }
    private void saveBook(){
        try{
            Category c=(Category)category.getSelectedItem();if(c==null)throw new IllegalArgumentException("Chưa có thể loại. Hãy chạy dữ liệu mẫu trong SQL.");
            Integer y=year.getText().isBlank()?null:Integer.valueOf(year.getText().trim());
            int q=Integer.parseInt(quantity.getText().trim());
            if(y!=null&&(y<1000||y>2100))throw new IllegalArgumentException("Năm xuất bản không hợp lệ.");
            if(q<0)throw new IllegalArgumentException("Số lượng không thể âm.");
            dao.saveBook(new Book(selectedId,c.id(),c.name(),title.getText(),author.getText(),publisher.getText(),y,q,0,shelf.getText()));
            Ui.info(this,"Đã lưu thông tin sách.");clearForm();loadBooks();
        }catch(Exception ex){Ui.error(this,ex);}
    }
    private void deleteBook(){if(selectedId==0){Ui.info(this,"Hãy chọn một sách trong danh sách.");return;}if(Ui.confirm(this,"Bạn chắc chắn muốn xóa sách này?"))try{dao.deleteBook(selectedId);Ui.info(this,"Đã xóa sách.");clearForm();loadBooks();}catch(Exception ex){Ui.error(this,ex);}}
    private void clearForm(){selectedId=0;title.setText("");author.setText("");publisher.setText("");year.setText("");quantity.setText("1");shelf.setText("");table.clearSelection();if(category.getItemCount()>0)category.setSelectedIndex(0);}
    private String nvl(String s){return s==null?"":s;}
}
