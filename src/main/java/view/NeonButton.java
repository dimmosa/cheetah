package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NeonButton extends JButton {

    private Color neonColor;
    private Color darkBackground = new Color(30, 30, 50);
    private Color hoverBackground;


    public NeonButton(String text, Color neonColor) {
        super(text);
        this.neonColor = neonColor;
        this.hoverBackground = darkBackground.brighter();

        setFont(new Font("SansSerif", Font.BOLD, 14));
        setForeground(Color.WHITE);
        setBackground(darkBackground);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverBackground);
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(darkBackground);
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int arc = 15;
        
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, width - 1, height - 1, arc, arc);

        g2.setColor(neonColor);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(0, 0, width - 1, height - 1, arc, arc);

        if (getModel().isRollover()) {
             g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
             g2.setColor(new Color(neonColor.getRed(), neonColor.getGreen(), neonColor.getBlue(), 50));
             g2.drawRoundRect(2, 2, width - 5, height - 5, arc, arc);
        }

        g2.dispose();
        super.paintComponent(g);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
    }
}