package view;

import javax.swing.*;
import model.CellType;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

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
 // ===== DEMO ICON (Training) =====
    private boolean demoIconOn = false;
    private String demoIcon = "";
    private Color demoIconColor = Color.WHITE;


    // Feedback (flag correctness)
    private boolean showingFeedback = false;
    private Color feedbackColor = null;

    // Permanent highlight (training pulse / step focus)
    private Color permanentBorderColor = null;

    // Training hint (text) system
    private boolean showingHint = false;
    private String hintText = "";
    private Color hintColor = null;
    private float hintPulse = 0f;
    private Timer hintPulseTimer = null;

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

    // =========================
    // Getters / Setters
    // =========================
    public CellState getState() { return state; }

    public void setState(CellState newState) {
        this.state = newState;
        revalidate();
        repaint();
    }

    public CellType getCellType() { return cellType; }

    public void setCellType(CellType type) { this.cellType = type; }

    public int getNumber() { return number; }

    public void setNumber(int num) {
        this.number = num;
        if (this.state == CellState.REVEALED) this.state = CellState.NUMBER;
        revalidate();
        repaint();
    }

    public boolean isFlagged() { return flagged; }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
        if (flagged) setState(CellState.FLAGGED);
        else setState(CellState.HIDDEN);
    }

    public boolean isUsed() { return used; }

    public void setUsed(boolean used) {
        this.used = used;
        if (used) {
            if (cellType == CellType.QUESTION) setState(CellState.USED_QUESTION);
            else if (cellType == CellType.SURPRISE) setState(CellState.USED_SURPRISE);
        }
    }

    public void setPermanentBorderColor(Color c) {
        this.permanentBorderColor = c;
        repaint();
    }

    // =========================
    // Reveal helpers
    // =========================
    public void showMine() { setState(CellState.MINE); }

    public void showNumber(int num) {
        setNumber(num);
        setState(CellState.NUMBER);
    }

    public void showEmpty() { setState(CellState.EMPTY); }

    public void showQuestion() { setState(CellState.QUESTION); }

    public void showSurprise() { setState(CellState.SURPRISE); }

    // =========================
    // Flag feedback
    // =========================
    public void showCorrectFlagFeedback() {
        showingFeedback = true;
        feedbackColor = new Color(0, 255, 0, 170);
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
        feedbackColor = new Color(255, 0, 0, 170);
        repaint();

        Timer timer = new Timer(500, e -> {
            showingFeedback = false;
            feedbackColor = null;
            repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }

    // =========================
    // Painting
    // =========================
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

        // 1) Base cell content
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
                drawMine(g2, w, h, radius);
                break;

            case NUMBER:
                drawNumberCell(g2, w, h, radius);
                break;

            case EMPTY:
                drawEmptyCell(g2, w, h, radius);
                break;

            case QUESTION:
                drawQuestionCell(g2, w, h, radius);
                break;

            case USED_QUESTION:
                drawUsedQuestionCell(g2, w, h, radius);
                break;

            case SURPRISE:
                drawSurpriseCell(g2, w, h, radius);
                break;

            case USED_SURPRISE:
                drawUsedSurpriseCell(g2, w, h, radius);
                break;

            default:
                g2.setColor(new Color(30, 30, 45));
                g2.fillRoundRect(0, 0, w, h, radius, radius);
        }

        // 2) GAME HOT/COLD OVERLAY (from board.getHintOverlayFor) - ALWAYS ON TOP
        drawGameOverlayHintIfAny(g2, w, h);

        // 3) TRAINING hint text overlay (showHint) - ALWAYS ON TOP
        if (showingHint && hintColor != null) {
            drawSuperVisibleHintTop(g2, w, h, radius);
        }

        // 4) feedback + permanent highlight borders
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

    // =========================
    // GAME overlay hint (boardRef)
    // =========================
    private void drawGameOverlayHintIfAny(Graphics2D g2, int w, int h) {
        Object boardObj = getClientProperty("boardRef");
        if (!(boardObj instanceof MinesweeperBoardPanelTwoPlayer)
        	    && !(boardObj instanceof MinesweeperBoardPanelCompetitive)) return;

        	Color overlay =
        	        (boardObj instanceof MinesweeperBoardPanelTwoPlayer b1)
        	                ? b1.getHintOverlayFor(this)
        	                : ((MinesweeperBoardPanelCompetitive) boardObj).getHintOverlayFor(this);

        if (overlay == null) return;

        Graphics2D gHint = (Graphics2D) g2.create();
        gHint.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Force strong alpha so it’s ALWAYS visible
        Color strong = new Color(overlay.getRed(), overlay.getGreen(), overlay.getBlue(), 210);

        // fill
        gHint.setColor(strong);
        gHint.fillRoundRect(0, 0, w, h, 10, 10);

        // wide glow
        gHint.setStroke(new BasicStroke(8f));
        gHint.setColor(new Color(255, 255, 255, 90));
        gHint.drawRoundRect(2, 2, w - 4, h - 4, 14, 14);

        // crisp outline
        gHint.setStroke(new BasicStroke(3.2f));
        gHint.setColor(new Color(strong.getRed(), strong.getGreen(), strong.getBlue(), 255));
        gHint.drawRoundRect(3, 3, w - 6, h - 6, 12, 12);

        gHint.dispose();
    }

    // =========================
    // TRAINING hint: text wrap + auto-fit
    // =========================
    private void drawSuperVisibleHintTop(Graphics2D g2, int w, int h, int radius) {
        float s = (float) ((Math.sin(hintPulse) + 1) / 2.0); // 0..1
        int fillA = 190 + (int) (55 * s);   // 190..245
        int glowA = 80 + (int) (110 * s);   // 80..190

        g2.setColor(new Color(hintColor.getRed(), hintColor.getGreen(), hintColor.getBlue(), fillA));
        g2.fillRoundRect(0, 0, w, h, radius, radius);

        // glow wide
        g2.setStroke(new BasicStroke(8f));
        g2.setColor(new Color(255, 255, 255, Math.min(180, glowA)));
        g2.drawRoundRect(2, 2, w - 4, h - 4, radius + 4, radius + 4);

        // crisp inner line
        g2.setStroke(new BasicStroke(3.2f));
        g2.setColor(new Color(hintColor.getRed(), hintColor.getGreen(), hintColor.getBlue(), 240));
        g2.drawRoundRect(3, 3, w - 6, h - 6, radius + 2, radius + 2);

        int pad = 7;
        Rectangle box = new Rectangle(pad, pad, w - pad * 2, h - pad * 2);

        Font fitted = fitWrappedFont(g2, hintText, box, 12, 8);
        g2.setFont(fitted);

        // shadow double
        g2.setColor(new Color(0, 0, 0, 230));
        drawWrappedCenteredString(g2, hintText, new Rectangle(box.x + 2, box.y + 2, box.width, box.height));
        g2.setColor(new Color(0, 0, 0, 170));
        drawWrappedCenteredString(g2, hintText, new Rectangle(box.x + 1, box.y + 1, box.width, box.height));

        // main text
        g2.setColor(Color.WHITE);
        drawWrappedCenteredString(g2, hintText, box);
    }

    private Font fitWrappedFont(Graphics2D g2, String text, Rectangle box, int startPx, int minPx) {
        if (text == null) text = "";
        for (int px = startPx; px >= minPx; px--) {
            Font f = new Font("SansSerif", Font.BOLD, px);
            int height = wrappedTextHeight(g2, text, box.width, f);
            if (height <= box.height) return f;
        }
        return new Font("SansSerif", Font.BOLD, minPx);
    }

    private int wrappedTextHeight(Graphics2D g2, String text, int maxWidth, Font font) {
        FontMetrics fm = g2.getFontMetrics(font);
        List<String> lines = wrapLines(text, fm, maxWidth);
        int lineH = fm.getAscent() + fm.getDescent();
        return lines.size() * lineH;
    }

    private void drawWrappedCenteredString(Graphics2D g2, String text, Rectangle box) {
        FontMetrics fm = g2.getFontMetrics();
        List<String> lines = wrapLines(text, fm, box.width);

        int lineH = fm.getAscent() + fm.getDescent();
        int totalH = lines.size() * lineH;

        int y = box.y + (box.height - totalH) / 2 + fm.getAscent();
        for (String line : lines) {
            int x = box.x + (box.width - fm.stringWidth(line)) / 2;
            g2.drawString(line, x, y);
            y += lineH;
        }
    }

    private List<String> wrapLines(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null) return lines;

        String[] paragraphs = text.split("\n");
        for (String p : paragraphs) {
            String[] words = p.trim().split("\\s+");
            if (words.length == 1 && words[0].isEmpty()) {
                lines.add("");
                continue;
            }

            StringBuilder line = new StringBuilder();
            for (String w : words) {
                String test = line.isEmpty() ? w : line + " " + w;
                if (fm.stringWidth(test) <= maxWidth) {
                    line.setLength(0);
                    line.append(test);
                } else {
                    if (!line.isEmpty()) lines.add(line.toString());
                    line.setLength(0);

                    if (fm.stringWidth(w) <= maxWidth) {
                        line.append(w);
                    } else {
                        // hard cut very long word
                        String cut = "";
                        for (int i = 0; i < w.length(); i++) {
                            String t = cut + w.charAt(i);
                            if (fm.stringWidth(t) > maxWidth) {
                                if (!cut.isEmpty()) lines.add(cut);
                                cut = "" + w.charAt(i);
                            } else cut = t;
                        }
                        line.append(cut);
                    }
                }
            }
            if (!line.isEmpty()) lines.add(line.toString());
        }
        return lines;
    }

    // =========================
    // Base cell drawing helpers
    // =========================
    private void drawMine(Graphics2D g2, int w, int h, int radius) {
        g2.setColor(new Color(150, 0, 0, 220));
        g2.fillRoundRect(0, 0, w, h, radius + 2, radius + 2);

        RadialGradientPaint explosionGradient = new RadialGradientPaint(
                w / 2f, h / 2f, w / 2f,
                new float[]{0.0f, 0.6f, 1.0f},
                new Color[]{
                        new Color(255, 80, 80),
                        new Color(200, 30, 30),
                        new Color(130, 0, 0)
                }
        );
        g2.setPaint(explosionGradient);
        g2.fillRoundRect(2, 2, w - 4, h - 4, radius, radius);

        g2.setFont(SYMBOL_FONT);
        g2.setColor(new Color(0, 0, 0, 150));
        drawCenteredString(g2, "💣", w + 1, h + 1);
        g2.setColor(Color.WHITE);
        drawCenteredString(g2, "💣", w, h);
    }

    private void drawNumberCell(Graphics2D g2, int w, int h, int radius) {
        g2.setColor(new Color(5, 5, 15, 230));
        g2.fillRoundRect(2, 2, w - 2, h - 2, radius, radius);

        GradientPaint insetGradient = new GradientPaint(
                0, 0, new Color(22, 22, 38),
                w, h, new Color(16, 16, 28)
        );
        g2.setPaint(insetGradient);
        g2.fillRoundRect(3, 3, w - 6, h - 6, radius, radius);

        g2.setFont(NUMBER_FONT);
        g2.setColor(getNumberColor(number));
        drawCenteredString(g2, String.valueOf(number), w, h);
    }

    private void drawEmptyCell(Graphics2D g2, int w, int h, int radius) {
        g2.setColor(new Color(5, 5, 15, 230));
        g2.fillRoundRect(2, 2, w - 2, h - 2, radius, radius);

        GradientPaint emptyGradient = new GradientPaint(
                0, 0, new Color(18, 18, 30),
                w, h, new Color(12, 12, 22)
        );
        g2.setPaint(emptyGradient);
        g2.fillRoundRect(3, 3, w - 6, h - 6, radius, radius);
    }

    private void drawQuestionCell(Graphics2D g2, int w, int h, int radius) {
        g2.setColor(new Color(0, 100, 180, 230));
        g2.fillRoundRect(2, 2, w - 2, h - 2, radius, radius);

        GradientPaint questionGradient = new GradientPaint(
                0, 0, new Color(15, 70, 140),
                w, h, new Color(10, 50, 100)
        );
        g2.setPaint(questionGradient);
        g2.fillRoundRect(3, 3, w - 6, h - 6, radius, radius);

        g2.setFont(SYMBOL_FONT);
        g2.setColor(new Color(120, 240, 255));
        drawCenteredString(g2, "❓", w, h);
    }

    private void drawUsedQuestionCell(Graphics2D g2, int w, int h, int radius) {
        drawEmptyCell(g2, w, h, radius);
        g2.setFont(SYMBOL_FONT);
        g2.setColor(new Color(50, 255, 150));
        drawCenteredString(g2, "✓", w, h);
    }

    private void drawSurpriseCell(Graphics2D g2, int w, int h, int radius) {
        g2.setColor(new Color(180, 120, 0, 230));
        g2.fillRoundRect(2, 2, w - 2, h - 2, radius, radius);

        GradientPaint surpriseGradient = new GradientPaint(
                0, 0, new Color(140, 90, 10),
                w, h, new Color(100, 65, 5)
        );
        g2.setPaint(surpriseGradient);
        g2.fillRoundRect(3, 3, w - 6, h - 6, radius, radius);

        g2.setFont(SYMBOL_FONT);
        g2.setColor(new Color(255, 230, 50));
        drawCenteredString(g2, "🎁", w, h);
    }

    private void drawUsedSurpriseCell(Graphics2D g2, int w, int h, int radius) {
        drawEmptyCell(g2, w, h, radius);
        g2.setFont(SYMBOL_FONT);
        g2.setColor(new Color(255, 230, 50));
        drawCenteredString(g2, "⭐", w, h);
    }

    private void drawHiddenCell(Graphics2D g2, int w, int h, int radius) {
        g2.setColor(new Color(20, 20, 30, 150));
        g2.fillRoundRect(3, 3, w - 3, h - 3, radius, radius);

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(65, 70, 100),
                w, h, new Color(50, 55, 80)
        );
        g2.setPaint(gradient);
        g2.fillRoundRect(1, 1, w - 4, h - 4, radius, radius);

        g2.setColor(new Color(85, 90, 130, 200));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(1, 1, w - 4, h - 4, radius, radius);
    }

    private void drawCenteredString(Graphics g, String text, int w, int h) {
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(text)) / 2;
        int y = (fm.getAscent() + (h - (fm.getAscent() + fm.getDescent())) / 2);
        g.drawString(text, x, y);
    }

    private Color getNumberColor(int num) {
        return switch (num) {
            case 1 -> new Color(0, 220, 255);
            case 2 -> new Color(50, 255, 150);
            case 3 -> new Color(255, 100, 30);
            case 4 -> new Color(170, 70, 255);
            case 5 -> new Color(255, 50, 90);
            case 6 -> new Color(0, 240, 220);
            case 7 -> new Color(255, 230, 0);
            case 8 -> new Color(200, 200, 200);
            default -> Color.WHITE;
        };
    }

    // =========================
    // Utility
    // =========================
    public void reset() {
        state = CellState.HIDDEN;
        cellType = null;
        number = 0;
        flagged = false;
        used = false;
        showingFeedback = false;
        feedbackColor = null;
        permanentBorderColor = null;

        clearHint();

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

    // =========================
    // Training hint API
    // =========================
    public void showHint(String hint, Color hintColor) {
        this.showingHint = true;
        this.hintText = hint != null ? hint : "";
        this.hintColor = hintColor != null ? hintColor : new Color(0, 255, 255);
        this.hintPulse = 0f;

        if (hintPulseTimer != null) hintPulseTimer.stop();

        hintPulseTimer = new Timer(30, e -> {
            hintPulse += 0.22f;
            repaint();
        });
        hintPulseTimer.start();

        Timer clearTimer = new Timer(5000, e -> clearHint());
        clearTimer.setRepeats(false);
        clearTimer.start();

        repaint();
    }

    private void clearHint() {
        if (hintPulseTimer != null) {
            hintPulseTimer.stop();
            hintPulseTimer = null;
        }
        showingHint = false;
        hintText = "";
        hintColor = null;
        hintPulse = 0f;
        repaint();
    }
    public void showFlagIcon() {
        demoIconOn = true;
        demoIcon = "🚩";
        demoIconColor = new Color(255, 80, 110);
        repaint();
    }

    public void forceShowMineIcon() {
        demoIconOn = true;
        demoIcon = "💣";
        demoIconColor = Color.WHITE;
        repaint();
    }

    public void clearDemoIcon() {
        demoIconOn = false;
        demoIcon = "";
        repaint();
    }

}
