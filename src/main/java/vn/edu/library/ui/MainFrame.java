package vn.edu.library.ui;

import vn.edu.library.dao.LibraryDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private final LibraryDAO dao=new LibraryDAO();
    private final JLabel[] values=new JLabel[5];
    private static final String[] TITLES={"Đầu sách","Sách còn trong kho","Độc giả","Phiếu đang mượn","Phiếu quá hạn"};

    public MainFrame() {
        Ui.prepareFrame(this,"Hệ thống quản lý thư viện",920,620);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel root=new JPanel(new BorderLayout()); root.setBackground(Ui.BG); setContentPane(root);
        JPanel header=new JPanel(new BorderLayout());header.setBackground(Ui.NAVY);header.setBorder(new EmptyBorder(18,24,18,24));
        JLabel title=new JLabel("QUẢN LÝ THƯ VIỆN");title.setForeground(Color.WHITE);title.setFont(new Font("Segoe UI",Font.BOLD,25));
        JLabel sub=new JLabel("Library Management System  |  Java Swing + SQL Server");sub.setForeground(new Color(215,225,238));
        JPanel headText=new JPanel(new GridLayout(2,1));headText.setOpaque(false);headText.add(title);headText.add(sub);header.add(headText,BorderLayout.WEST);root.add(header,BorderLayout.NORTH);

        JPanel center=new JPanel(new BorderLayout(14,14));center.setBackground(Ui.BG);center.setBorder(new EmptyBorder(20,24,24,24));root.add(center,BorderLayout.CENTER);
        JLabel overview=new JLabel("TỔNG QUAN HỆ THỐNG");overview.setFont(new Font("Segoe UI",Font.BOLD,17));overview.setForeground(Ui.NAVY);center.add(overview,BorderLayout.NORTH);
        JPanel cards=new JPanel(new GridLayout(1,5,10,10));cards.setBackground(Ui.BG);
        for(int i=0;i<5;i++){
            JPanel card=new JPanel(new BorderLayout(4,12));card.setBackground(Color.WHITE);card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(225,230,237)),new EmptyBorder(14,12,14,12)));
            JLabel name=new JLabel(TITLES[i]);name.setForeground(new Color(90,100,112));name.setFont(new Font("Segoe UI",Font.PLAIN,12));
            values[i]=new JLabel("—");values[i].setForeground(Ui.BLUE);values[i].setFont(new Font("Segoe UI",Font.BOLD,27));
            card.add(name,BorderLayout.NORTH);card.add(values[i],BorderLayout.CENTER);cards.add(card);
        }
        center.add(cards,BorderLayout.CENTER);
        JPanel actions=new JPanel(new GridLayout(2,2,12,12));actions.setBackground(Ui.BG);
        JButton books=Ui.button("Quản lý sách");books.addActionListener(e->new BookForm().setVisible(true));
        JButton readers=Ui.button("Quản lý độc giả");readers.addActionListener(e->new ReaderForm().setVisible(true));
        JButton loans=Ui.button("Mượn / trả sách");loans.addActionListener(e->new LoanForm().setVisible(true));
        JButton refresh=Ui.button("Làm mới thống kê");refresh.addActionListener(e->refreshStats());
        actions.add(books);actions.add(readers);actions.add(loans);actions.add(refresh);
        JPanel bottom=new JPanel(new BorderLayout(8,8));bottom.setBackground(Ui.BG);bottom.add(actions,BorderLayout.CENTER);
        JLabel note=new JLabel("Lưu ý: cần chạy file SQL trong thư mục sql trước khi sử dụng phần mềm.");note.setForeground(new Color(100,105,112));bottom.add(note,BorderLayout.SOUTH);center.add(bottom,BorderLayout.SOUTH);
        refreshStats();
    }

    private void refreshStats(){
        try{int[] n=dao.getDashboardStats();for(int i=0;i<n.length;i++)values[i].setText(String.valueOf(n[i]));}
        catch(Exception ex){for(JLabel v:values)v.setText("—");}
    }
}
