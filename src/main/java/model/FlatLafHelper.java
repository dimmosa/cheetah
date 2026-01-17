package model;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class FlatLafHelper {

    public static void setupLightTheme() {
        try { UIManager.setLookAndFeel(new FlatLightLaf()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    public static void setupDarkTheme() {
        try { UIManager.setLookAndFeel(new FlatDarkLaf()); }
        catch (Exception e) { e.printStackTrace(); }
    }
}