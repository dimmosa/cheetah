package view;

import view.GameSetupScreen;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Minesweeper - Multiplayer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            GameSetupScreen setupScreen = new GameSetupScreen(frame);
            frame.setContentPane(setupScreen);
            
            frame.setSize(600, 500);
            frame.setLocationRelativeTo(null); // מרכז את החלון
            frame.setVisible(true);
        });
    }
}