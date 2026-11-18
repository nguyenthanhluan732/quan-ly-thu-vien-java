package vn.edu.library.ui;

import vn.edu.library.dao.LibraryDAO;
import vn.edu.library.model.Reader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReaderForm extends JFrame {
    private final LibraryDAO dao=new LibraryDAO();
    private final JTextField card=new JTextField(20),name=new JTextField(20),birth=new JTextField(12),phone=new JTextField(16),email=new JTextField(20),address=new JTextField(24),search=new JTextField(18);
    private final JComboBox<String> gender=new JComboBox<>(new String[]{"Nam","Nữ","Khác"});
    private final DefaultTableModel tableModel=Ui.model("Mã độc giả","Mã thẻ","Họ tên","Ngày sinh","Giới tính","Điện thoại","Email","Địa chỉ");
    private final JTable table=new JTable(tableModel);
    private int selectedId=0;

    public ReaderForm(){
        Ui.prepareFrame(this,"Quản lý độc giả",1080,680);
        JPanel root=new JPanel(new BorderLayout(10,10));root.setBackground(Ui.BG);setContentPane(root);
        JPanel heading=new JPanel();Ui.heading(heading,"QUẢN LÝ ĐỘC GIẢ","Quản lý thông tin thẻ thư viện và thông tin liên hệ");root.add(heading,BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(10,10));body.setBackground(Ui.BG);body.setBorder(new EmptyBorder(0,18,18,18));root.add(body,BorderLayout.CENTER);
        JPanel form=Ui.titled("Thông tin độc giả");JPanel fields=Ui.formGrid();
        Ui.addField(fields,0,"Mã thẻ *",card);Ui.addField(fields,1,"Họ và tên *",name);Ui.addField(fields,2,"Ngày sinh (yyyy-MM-dd)",birth);
        Ui.addField(fields,3,"Giới tính",gender);Ui.addField(fields,4,"Số điện thoại",phone);Ui.addField(fields,5,"Email",email);Ui.addField(fields,6,"Địa chỉ",address);
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT));actions.setBackground(Color.WHITE);
        JButton save=Ui.button("Lưu (thêm / sửa)"),delete=Ui.button("Xóa"),clear=Ui.button("Làm mới");save.addActionListener(e->saveReader());delete.addActionListener(e->deleteReader());clear.addActionListener(e->clearForm());
        actions.add(save);actions.add(delete);actions.add(clear);form.add(fields,BorderLayout.CENTER);form.add(actions,BorderLayout.SOUTH);
        JPanel list=Ui.titled("Danh sách độc giả");JPanel searchBar=new JPanel(new FlowLayout(FlowLayout.LEFT));searchBar.setBackground(Color.WHITE);searchBar.add(new JLabel("Tìm theo mã thẻ / họ tên / SĐT:"));searchBar.add(search);
        JButton find=Ui.button("Tìm kiếm"),all=Ui.button("Hiển thị tất cả");find.addActionListener(e->loadReaders());all.addActionListener(e->{search.setText("");loadReaders();});search.addActionListener(e->loadReaders());searchBar.add(find);searchBar.add(all);
        list.add(searchBar,BorderLayout.NORTH);list.add(Ui.scroll(table),BorderLayout.CENTER);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,form,list);split.setResizeWeight(0.43);split.setBorder(null);body.add(split,BorderLayout.CENTER);
        table.getSelectionModel().addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&table.getSelectedRow()>=0)selectReader();});loadReaders();
    }
    private void loadReaders(){try{tableModel.setRowCount(0);List<Reader> list=dao.getReaders(search.getText());for(Reader r:list)tableModel.addRow(new Object[]{r.id(),r.cardNumber(),r.fullName(),r.birthDate(),r.gender(),r.phone(),r.email(),r.address()});}catch(Exception ex){Ui.error(this,ex);}}
    private void selectReader(){try{int row=table.getSelectedRow();int id=(int)table.getValueAt(row,0);for(Reader r:dao.getReaders(""))if(r.id()==id){selectedId=id;card.setText(r.cardNumber());name.setText(r.fullName());birth.setText(r.birthDate());gender.setSelectedItem(r.gender()==null?"Khác":r.gender());phone.setText(nvl(r.phone()));email.setText(nvl(r.email()));address.setText(nvl(r.address()));break;}}catch(Exception ex){Ui.error(this,ex);}}
    private void saveReader(){try{dao.saveReader(new Reader(selectedId,card.getText(),name.getText(),birth.getText(),(String)gender.getSelectedItem(),phone.getText(),email.getText(),address.getText()));Ui.info(this,"Đã lưu thông tin độc giả.");clearForm();loadReaders();}catch(Exception ex){Ui.error(this,ex);}}
    private void deleteReader(){if(selectedId==0){Ui.info(this,"Hãy chọn độc giả cần xóa.");return;}if(Ui.confirm(this,"Bạn chắc chắn muốn xóa độc giả này?"))try{dao.deleteReader(selectedId);Ui.info(this,"Đã xóa độc giả.");clearForm();loadReaders();}catch(Exception ex){Ui.error(this,ex);}}
    private void clearForm(){selectedId=0;card.setText("");name.setText("");birth.setText("");gender.setSelectedIndex(0);phone.setText("");email.setText("");address.setText("");table.clearSelection();}
    private String nvl(String s){return s==null?"":s;}
}
