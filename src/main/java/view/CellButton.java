package view;

import javax.swing.*;

import model.CellType;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CellButton extends JButton {

    public enum CellState {
        HIDDEN, REVEALED, FLAGGED, MINE, NUMBER, EMPTY,
        QUESTION, SURPRISE, USED_QUESTION, USED_SURPRISE
    }

    private CellState state = CellState.HIDDEN;
    private CellType cellType;
    private int number = 0;
    private boolean flagged = false;
    private boolean used = false;
    private boolean showingFeedback = false;
    private Color feedbackColor = null;
    private Color permanentBorderColor = null;


    private static final Font SYMBOL_FONT = new Font("Segoe UI Emoji", Font.BOLD, 16);
    private static final Font NUMBER_FONT = new Font("SansSerif", Font.BOLD, 20);

    public CellButton(int size) {
        setPreferredSize(new Dimension(size, size));
        setMinimumSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        setMargin(new Insets(0, 0, 0, 0));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (state == CellState.HIDDEN || state == CellState.FLAGGED) {
                    setBackground(new Color(100, 100, 150, 100));
                    setOpaque(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (state == CellState.HIDDEN || state == CellState.FLAGGED) {
                    setOpaque(false);
                }
            }
        });
    }


    public CellState getState() {
        return state;
    }

    public void setState(CellState newState) {
        this.state = newState;
        revalidate();
        repaint();
    }

    public CellType getCellType() {
        return cellType;
    }

    public void setCellType(CellType type) {
        this.cellType = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int num) {
        this.number = num;
        if (this.state == CellState.REVEALED) {
            this.state = CellState.NUMBER;
        }
        revalidate();
        repaint();
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
        if (flagged) {
            setState(CellState.FLAGGED);
        } else {
            setState(CellState.HIDDEN);
        }
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
        if (used) {
            if (cellType == CellType.QUESTION) {
                setState(CellState.USED_QUESTION);
            } else if (cellType == CellType.SURPRISE) {
                setState(CellState.USED_SURPRISE);
            }
        }
    }

    public void setPermanentBorderColor(Color c) {
        this.permanentBorderColor = c;
        repaint();
    }

    public void showMine() {
        setState(CellState.MINE);
    }

    public void showNumber(int num) {
        setNumber(num);
        setState(CellState.NUMBER);
    }

    public void showEmpty() {
        setState(CellState.EMPTY);
    }

    public void showQuestion() {
        setState(CellState.QUESTION);
    }

    public void showSurprise() {
        setState(CellState.SURPRISE);
    }


    public void showCorrectFlagFeedback() {
        showingFeedback = true;
        feedbackColor = new Color(0, 255, 0, 150);
        repaint();

        Timer timer = new Timer(500, e -> {
            showingFeedback = false;
            feedbackColor = null;
            repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }


    public void showIncorrectFlagFeedback() {
        showingFeedback = true;
        feedbackColor = new Color(255, 0, 0, 150);
        repaint();

        Timer timer = new Timer(500, e -> {
            showingFeedback = false;
            feedbackColor = null;
            repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int radius = 8;

        if (isOpaque()) {
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, radius, radius);
        }

        switch (state) {
            case HIDDEN:
            case FLAGGED:
                drawHiddenCell(g2, w, h, radius);
                if (state == CellState.FLAGGED) {
                    g2.setFont(SYMBOL_FONT);
                    g2.setColor(Color.RED);
                    drawCenteredString(g2, "🚩", w, h);
                }
                break;

            case MINE:
                g2.setColor(new Color(150, 0, 0));
                g2.fillRoundRect(0, 0, w, h, radius, radius);
                g2.setFont(SYMBOL_FONT);
                g2.setColor(Color.WHITE);
                drawCenteredString(g2, "💣", w, h);
                break;

            case NUMBER:
                g2.setColor(new Color(30, 30, 45));
                g2.fillRoundRect(0, 0, w, h, radius, radius);
                g2.setFont(NUMBER_FONT);
                g2.setColor(getNumberColor(number));
                drawCenteredString(g2, String.valueOf(number), w, h);
                break;

            case EMPTY:
                g2.setColor(new Color(25, 25, 35));
                g2.fillRoundRect(0, 0, w, h, radius, radius);
                break;

            case QUESTION:
                drawHiddenCell(g2, w, h, radius);
                g2.setFont(SYMBOL_FONT);
                g2.setColor(new Color(100, 200, 255));
                drawCenteredString(g2, "❓", w, h);
                break;

            case USED_QUESTION:
                g2.setColor(new Color(25, 25, 35));
                g2.fillRoundRect(0, 0, w, h, radius, radius);
                g2.setFont(SYMBOL_FONT);
                g2.setColor(new Color(0, 255, 127));
                drawCenteredString(g2, "✓", w, h);
                break;

            case SURPRISE:
                drawHiddenCell(g2, w, h, radius);
                g2.setFont(SYMBOL_FONT);
                g2.setColor(new Color(255, 215, 0));
                drawCenteredString(g2, "🎁", w, h);
                break;

            case USED_SURPRISE:
                g2.setColor(new Color(25, 25, 35));
                g2.fillRoundRect(0, 0, w, h, radius, radius);
                g2.setFont(SYMBOL_FONT);
                g2.setColor(new Color(255, 215, 0));
                drawCenteredString(g2, "⭐", w, h);
                break;

            case REVEALED:
            default:
                g2.setColor(new Color(30, 30, 45));
                g2.fillRoundRect(0, 0, w, h, radius, radius);
        }

        if (showingFeedback && feedbackColor != null) {
            g2.setColor(feedbackColor);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);
        }

        if (permanentBorderColor != null) {
            g2.setColor(permanentBorderColor);
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(2, 2, w - 4, h - 4, radius, radius);
        }

        g2.dispose();
    }

    private void drawHiddenCell(Graphics2D g2, int w, int h, int radius) {
        g2.setColor(new Color(50, 50, 70));
        g2.fillRoundRect(1, 1, w - 2, h - 2, radius, radius);

        g2.setColor(new Color(80, 80, 120));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(1, 1, w - 3, h - 3, radius, radius);
    }

    private void drawCenteredString(Graphics g, String text, int w, int h) {
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(text)) / 2;
        int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
        g.drawString(text, x, y);
    }

    private Color getNumberColor(int num) {
        return switch (num) {
            case 1 -> new Color(0, 191, 255);
            case 2 -> new Color(0, 255, 127);
            case 3 -> new Color(255, 69, 0);
            case 4 -> new Color(138, 43, 226);
            case 5 -> new Color(220, 20, 60);
            case 6 -> new Color(0, 206, 209);
            case 7 -> new Color(255, 215, 0);
            case 8 -> new Color(169, 169, 169);
            default -> Color.WHITE;
        };
    }


    public void reset() {
        state = CellState.HIDDEN;
        cellType = null;
        number = 0;
        flagged = false;
        used = false;
        showingFeedback = false;
        feedbackColor = null;
        setOpaque(false);
        repaint();
    }


    public boolean canReveal() {
        return state == CellState.HIDDEN && !flagged;
    }


    public boolean canFlag() {
        return state == CellState.HIDDEN || state == CellState.FLAGGED;
    }


    public boolean canActivate() {
        return (state == CellState.QUESTION || state == CellState.SURPRISE) && !used;
    }
}