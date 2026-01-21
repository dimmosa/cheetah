package view;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedPanel extends JPanel {
    private final int arc;

    public RoundedPanel(int arc) {
        this.arc = arc;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape rr = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc);
        g2.setClip(rr);

        // background
        g2.setColor(getBackground());
        g2.fill(rr);

        g2.dispose();
        super.paintComponent(g);
    }
}
