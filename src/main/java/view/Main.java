package view;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.awt.*;

public class Main {

    private static final String MENU_MUSIC = "/music/menu_theme.mp3";
    private static final String GAME_MUSIC = "/music/game_theme.wav";

    public static void main(String[] args) {
        // ✅ Install FlatLaf BEFORE creating Swing components
        FlatDarkLaf.setup();

        // ✅ init SFX once for the whole app
        AudioManager.init();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Minesweeper - Multiplayer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // ✅ Start screen
            JPanel startScreen = new LoginTwoPlayerScreen(frame);
            frame.setContentPane(startScreen);

            frame.setResizable(true);

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int initialWidth = (int) (screenSize.width * 0.8);
            int initialHeight = (int) (screenSize.height * 0.8);
            frame.setSize(initialWidth, initialHeight);
            frame.setLocationRelativeTo(null);

            frame.setVisible(true);

            // ✅ start menu music ONLY if audio is not muted
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
        // אם המשתמש עשה Mute -> לא מנגנים כלום
        if (AudioManager.isMuted()) {
            MusicManager.stop();
            return;
        }

        if (screen instanceof LoginTwoPlayerScreen) {
            MusicManager.playLoop(MENU_MUSIC);
        } else if (screen instanceof GameScreenMultiPlayer) {
            MusicManager.playLoop(GAME_MUSIC);
        } else {
            // מסכים אחרים - אפשר לבחור או להשאיר מוזיקה קיימת או לעצור
            // MusicManager.stop();
        }
    }
}
