package model;

import javax.swing.*;
import java.awt.*;

public class CellButton extends JButton {

    public enum CellState {
        HIDDEN, NUMBER, MINE, FLAGGED, EMPTY, QUESTION, SURPRISE, USED_QUESTION, USED_SURPRISE
    }

    public CellState state;
    private int number;

    public CellButton(int size) {
        state = CellState.HIDDEN;
        setPreferredSize(new Dimension(size, size));
        setFont(new Font("SansSerif", Font.BOLD, 16));
        updateAppearance();
    }

    public void setState(CellState state) {
        this.state = state;
        updateAppearance();
    }

    public void setNumber(int number) {
        this.number = number;
        updateAppearance();
    }

    private void updateAppearance() {
        switch (state) {
            case HIDDEN -> {
                setText("");
                setBackground(Color.DARK_GRAY);
            }
            case NUMBER -> setText(String.valueOf(number));
            case MINE -> setText("💣");
            case FLAGGED -> setText("🚩");
            case EMPTY -> setText("");
            case QUESTION -> setText("?");
            case SURPRISE -> setText("🎁");
            case USED_QUESTION -> setText("❔");
            case USED_SURPRISE -> setText("❌");
        }
        setOpaque(true);
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
}
