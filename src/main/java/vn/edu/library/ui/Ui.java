package vn.edu.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public final class Ui {
    private Ui() {}
    public static final Color NAVY = new Color(25, 52, 84);
    public static final Color BLUE = new Color(35, 112, 180);
    public static final Color BG = new Color(245, 247, 250);

    public static void prepareFrame(JFrame frame, String title, int width, int height) {
        frame.setTitle(title); frame.setSize(width,height); frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); frame.getContentPane().setBackground(BG);
    }
    public static JButton button(String text) {
        JButton b=new JButton(text); b.setFocusPainted(false); b.setBackground(BLUE); b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(8,14,8,14)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    public static JPanel titled(String title) {
        JPanel p=new JPanel(new BorderLayout(8,8)); p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),new EmptyBorder(8,8,8,8)));
        return p;
    }
    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns,0) { @Override public boolean isCellEditable(int row,int col){return false;} };
    }
    public static JScrollPane scroll(JTable table) {
        table.setRowHeight(26); table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setAutoCreateRowSorter(true);
        return new JScrollPane(table);
    }
    public static void error(Component parent, Exception ex) {
        String message=ex.getMessage()==null?ex.toString():ex.getMessage();
        JOptionPane.showMessageDialog(parent,message,"Có lỗi xảy ra",JOptionPane.ERROR_MESSAGE);
    }
    public static boolean confirm(Component parent,String message) {
        return JOptionPane.showConfirmDialog(parent,message,"Xác nhận",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
    }
    public static void info(Component parent,String message) { JOptionPane.showMessageDialog(parent,message,"Thông báo",JOptionPane.INFORMATION_MESSAGE); }
    public static JPanel formGrid() {
        JPanel p=new JPanel(new GridBagLayout()); p.setBackground(Color.WHITE); return p;
    }
    public static void addField(JPanel p,int row,String label,JComponent field) {
        GridBagConstraints a=new GridBagConstraints(); a.gridx=0;a.gridy=row;a.insets=new Insets(4,4,4,10);a.anchor=GridBagConstraints.LINE_END;
        p.add(new JLabel(label),a);
        GridBagConstraints b=new GridBagConstraints();b.gridx=1;b.gridy=row;b.weightx=1;b.fill=GridBagConstraints.HORIZONTAL;b.insets=new Insets(4,4,4,4);
        p.add(field,b);
    }
    public static void heading(JPanel p,String title,String subtitle) {
        p.setLayout(new BorderLayout(4,4)); p.setBackground(BG); p.setBorder(new EmptyBorder(14,18,8,18));
        JLabel h=new JLabel(title); h.setFont(new Font("Segoe UI",Font.BOLD,22)); h.setForeground(NAVY);
        JLabel s=new JLabel(subtitle); s.setFont(new Font("Segoe UI",Font.PLAIN,12)); s.setForeground(Color.GRAY);
        JPanel labels=new JPanel(new GridLayout(2,1,2,2));labels.setOpaque(false);labels.add(h);labels.add(s);p.add(labels,BorderLayout.CENTER);
    }
}
