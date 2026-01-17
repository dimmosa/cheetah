package view;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        // ✅ Install FlatLaf BEFORE creating Swing components
        FlatDarkLaf.setup(); // sets UIManager LookAndFeel

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Minesweeper - Multiplayer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // ✅ Start here
            frame.setContentPane(new LoginTwoPlayerScreen(frame));

            frame.setResizable(true);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int initialWidth = (int) (screenSize.width * 0.8);
            int initialHeight = (int) (screenSize.height * 0.8);
            frame.setSize(initialWidth, initialHeight);
            frame.setLocationRelativeTo(null);

            frame.setVisible(true);
        });
    }

    public static void changeScreen(JFrame frame, JPanel newScreen) {
        SwingUtilities.invokeLater(() -> {
            frame.setContentPane(newScreen);
            frame.revalidate();
            frame.repaint();
        });
    }
}
