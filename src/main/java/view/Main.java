package view;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class Main {

    // change paths if needed
    private static final String MENU_MUSIC = "/music/menu_theme.mp3";
    private static final String GAME_MUSIC = "/music/game_theme.wav";

    public static void main(String[] args) {
        // ✅ Install FlatLaf BEFORE creating Swing components
        FlatDarkLaf.setup();

        // ✅ Init SFX once for the whole app
        AudioManager.init();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Minesweeper - Multiplayer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(true);

            // ✅ Start screen
            JPanel startScreen = new LoginTwoPlayerScreen(frame);
            frame.setContentPane(startScreen);

            // window size
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int initialWidth = (int) (screenSize.width * 0.8);
            int initialHeight = (int) (screenSize.height * 0.8);
            frame.setSize(initialWidth, initialHeight);
            frame.setLocationRelativeTo(null);

            frame.setVisible(true);

            // ✅ start correct music for the first screen
            playMusicForScreen(startScreen);
        });
    }

    public static void changeScreen(JFrame frame, JPanel newScreen) {
        SwingUtilities.invokeLater(() -> {
            // ✅ Swap content
            frame.setContentPane(newScreen);
            frame.revalidate();
            frame.repaint();

            // ✅ Change music after screen swap (safe)
            playMusicForScreen(newScreen);
        });
    }

    // ---------------- helpers ----------------

    private static void playMusicForScreen(JPanel screen) {
        // If user muted -> stop everything
        if (AudioManager.isMuted()) {
            MusicManager.stop();
            return;
        }

        if (screen instanceof LoginTwoPlayerScreen) {
            MusicManager.playLoop(MENU_MUSIC);
        } else if (screen instanceof GameScreenMultiPlayer) {
            MusicManager.playLoop(GAME_MUSIC);
        } else {
            // other screens:
            // option A: keep current music (do nothing)
            // option B: stop music:
            // MusicManager.stop();
        }
    }
}