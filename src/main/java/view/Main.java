package view;

import view.GameSetupScreen;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // הפעל את הממשק ב-Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Minesweeper - Multiplayer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // פתח את מסך ההגדרות
            GameSetupScreen setupScreen = new GameSetupScreen(frame);
            frame.setContentPane(setupScreen);
            
            frame.setSize(600, 500);
            frame.setLocationRelativeTo(null); // מרכז את החלון
            frame.setVisible(true);
        });
    }
}