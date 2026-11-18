package vn.edu.library;

import vn.edu.library.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { }
        EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
