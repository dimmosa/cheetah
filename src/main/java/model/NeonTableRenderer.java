package model;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;


public class NeonTableRenderer extends DefaultTableCellRenderer {
    
    private static final Color DARK_BACKGROUND = new Color(20, 20, 35);
    private static final Color ALTERNATE_BACKGROUND = new Color(25, 25, 40);
    private static final Color FOREGROUND_TEXT = new Color(220, 220, 255);
    private static final Color SELECTION_BACKGROUND = new Color(0, 150, 255, 150);
    private static final Color SELECTION_FOREGROUND = Color.WHITE;

    public NeonTableRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        
        if (isSelected) {
            setBackground(SELECTION_BACKGROUND);
        } else {
            if (row % 2 == 0) {
                setBackground(DARK_BACKGROUND);
            } else {
                setBackground(ALTERNATE_BACKGROUND);
            }
        }
        
        setForeground(isSelected ? SELECTION_FOREGROUND : FOREGROUND_TEXT);
        
        setFont(new Font("SansSerif", Font.PLAIN, 14));

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (column == 0 || column == 2) {
            setHorizontalAlignment(CENTER);
        } else {
            setHorizontalAlignment(LEFT);
        }
        
        if (column == 2) {
            String difficulty = value != null ? value.toString() : "";
            if ("Easy".equalsIgnoreCase(difficulty)) {
                setForeground(new Color(0, 255, 127));
            } else if ("Medium".equalsIgnoreCase(difficulty)) {
                setForeground(new Color(255, 215, 0));
            } else if ("Hard".equalsIgnoreCase(difficulty)) {
                setForeground(new Color(255, 100, 100));
            }
        }

        return this;
    }
}