package view;

import javax.swing.*;
import java.awt.*;

public class Main {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Minesweeper - Multiplayer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            MainMenuTwoPlayerScreen mainMenuTwoPlayerScreen = new MainMenuTwoPlayerScreen(frame);
            frame.setContentPane(mainMenuTwoPlayerScreen);

            // Make it responsive
            frame.setResizable(true);

            // Get screen dimensions
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width;
            int screenHeight = screenSize.height;

            // Set initial size to 80% of screen
            int initialWidth = (int) (screenWidth * 0.8);
            int initialHeight = (int) (screenHeight * 0.8);
            frame.setSize(initialWidth, initialHeight);

            // Center on screen
            frame.setLocationRelativeTo(null);

            frame.setVisible(true);
        });
    }
    
    /**
     * Simple smooth transition between screens
     * Usage: Main.changeScreen(frame, new GameSetupScreen(frame));
     */
    public static void changeScreen(JFrame frame, JPanel newScreen) {
        SwingUtilities.invokeLater(() -> {
            frame.setContentPane(newScreen);
            frame.revalidate();
            frame.repaint();
        });
    }
}