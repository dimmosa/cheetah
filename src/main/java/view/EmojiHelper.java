package view;

import java.awt.*;

/**
 * Helper class to ensure emojis display correctly across platforms
 * 
 * Windows, Mac, and Linux all use different emoji fonts.
 * This helper provides a universal solution.
 */
public class EmojiHelper {
    
    private static Font emojiFont = null;
    
    /**
     * Get the best emoji font for the current platform
     */
    public static Font getEmojiFont(int size) {
        if (emojiFont == null || emojiFont.getSize() != size) {
            emojiFont = createEmojiFont(size);
        }
        return emojiFont;
    }
    
    /**
     * Create emoji font with fallback support
     */
    private static Font createEmojiFont(int size) {
        // Try Windows font
        Font font = new Font("Segoe UI Emoji", Font.PLAIN, size);
        if (canDisplayEmojis(font)) {
            return font;
        }
        
        // Try Mac font
        font = new Font("Apple Color Emoji", Font.PLAIN, size);
        if (canDisplayEmojis(font)) {
            return font;
        }
        
        // Try Linux font
        font = new Font("Noto Color Emoji", Font.PLAIN, size);
        if (canDisplayEmojis(font)) {
            return font;
        }
        
        // Fallback to Segoe UI (still better than default)
        return new Font("Segoe UI", Font.PLAIN, size);
    }
    
    /**
     * Test if font can display emojis
     */
    private static boolean canDisplayEmojis(Font font) {
        // Test with common emoji characters using Unicode escape sequences
        // Gift: U+1F381, Question: U+2753, Bomb: U+1F4A3
        String testEmoji = "\uD83C\uDF81"; // Gift emoji
        return font.canDisplayUpTo(testEmoji) == -1;
    }
    
    /**
     * Test if system supports emoji rendering
     */
    public static boolean canDisplayEmojis() {
        Font testFont = getEmojiFont(12);
        return canDisplayEmojis(testFont);
    }
    
    /**
     * Get system emoji font name
     */
    public static String getSystemEmojiFontName() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            return "Segoe UI Emoji";
        } else if (os.contains("mac")) {
            return "Apple Color Emoji";
        } else {
            return "Noto Color Emoji";
        }
    }
}