package model;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class IconLoader {

    public static ImageIcon load(String path, int width, int height) {
        try {
            URL imgURL = IconLoader.class.getResource(path);
            
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image originalImage = originalIcon.getImage();
                
                Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                
                return new ImageIcon(scaledImage);
            } else {
                System.err.println("Icon not found: " + path);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}