package utils;

import javax.swing.*;

public class ShowUtils {
    public static void errorMessage(String s) {
        System.out.println(s);
        JOptionPane.showMessageDialog(null, s,"ERR", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warningMessage(String s) {
        JOptionPane.showMessageDialog(null, s,"WARN", JOptionPane.INFORMATION_MESSAGE);
    }
}
