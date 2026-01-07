package view;

import com.formdev.flatlaf.FlatClientProperties;
import control.GameHistoryController;
import model.DetailedGameHistoryEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainMenuTwoPlayerScreen extends JPanel {

    JFrame frame;

    public MainMenuTwoPlayerScreen(JFrame frame) {

        this.frame = frame;

        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(15, 25, 40, 240));
        card.setBorder(new EmptyBorder(40, 60, 50, 60));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 30");
        card.setPreferredSize(new Dimension(500, 600));

        JLabel iconLabel = new JLabel("💣");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel iconContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                RadialGradientPaint rgp = new RadialGradientPaint(
                        getWidth() / 2f, getHeight() / 2f, 40f,
                        new float[]{0f, 1f},
                        new Color[]{new Color(0, 200, 255, 100), new Color(0, 0, 0, 0)}
                );
                g2.setPaint(rgp);
                g2.fillOval(getWidth() / 2 - 40, getHeight() / 2 - 40, 80, 80);
                super.paintComponent(g);
            }
        };
        iconContainer.setOpaque(false);
        iconContainer.setMaximumSize(new Dimension(100, 100));
        iconContainer.add(iconLabel);

        JLabel title = new JLabel("MineSweeper");
        title.setFont(new Font("SansSerif", Font.PLAIN, 32));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Two-Player Edition");
        subtitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        subtitle.setForeground(new Color(0, 180, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnNewGame = createMenuButton("🎮   New Game");
        JButton btnQuestions = createMenuButton("❓   Question Management");
        JButton btnHistory = createMenuButton("🕒   History");

        // ✅ ONLY CHANGE: run screen creation in background (no freeze)

        btnNewGame.addActionListener(e -> {
            btnNewGame.setEnabled(false);
            btnQuestions.setEnabled(false);
            btnHistory.setEnabled(false);

            new SwingWorker<JPanel, Void>() {
                @Override
                protected JPanel doInBackground() {
                    // keep the same behavior: resize before showing setup screen
                    // (size changes will be applied in done() on EDT)
                    return new GameSetupScreen(frame);
                }

                @Override
                protected void done() {
                    try {
                        frame.setContentPane(get());
                        keepFrameBig();
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        btnNewGame.setEnabled(true);
                        btnQuestions.setEnabled(true);
                        btnHistory.setEnabled(true);
                    }
                }

            }.execute();
        });

        btnHistory.addActionListener(e -> {
            btnNewGame.setEnabled(false);
            btnQuestions.setEnabled(false);
            btnHistory.setEnabled(false);

            new SwingWorker<JPanel, Void>() {
                @Override
                protected JPanel doInBackground() {
                    GameHistoryController gameHistoryController = new GameHistoryController();
                    java.util.List<DetailedGameHistoryEntry> gameHistoryEntryList =
                            gameHistoryController.getDetailedHistoryForLoggedUser();
                    return new DetailedGameHistoryScreen(frame, gameHistoryEntryList);
                }

                @Override
                protected void done() {
                    try {
                        frame.setContentPane(get());
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        btnNewGame.setEnabled(true);
                        btnQuestions.setEnabled(true);
                        btnHistory.setEnabled(true);
                    }
                }
            }.execute();
        });

        btnQuestions.addActionListener(e -> {
            btnNewGame.setEnabled(false);
            btnQuestions.setEnabled(false);
            btnHistory.setEnabled(false);

            new SwingWorker<JPanel, Void>() {
                @Override
                protected JPanel doInBackground() {
                    return new QuestionManagementScreen(frame);
                }

                @Override
                protected void done() {
                    try {
                        frame.setContentPane(get());
                        frame.revalidate();
                        frame.repaint();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        btnNewGame.setEnabled(true);
                        btnQuestions.setEnabled(true);
                        btnHistory.setEnabled(true);
                    }
                }
            }.execute();
        });

        card.add(Box.createVerticalStrut(10));
        card.add(iconContainer);
        card.add(title);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(50));

        card.add(btnNewGame);
        card.add(Box.createVerticalStrut(15));
        card.add(btnQuestions);
        card.add(Box.createVerticalStrut(15));
        card.add(btnHistory);
        card.add(Box.createVerticalStrut(15));

        add(card);
    }
    
    private void keepFrameBig() {
        // If user had it maximized, keep it maximized
        if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            return;
        }

        // Otherwise: keep current size, but prevent it from being too small
        int w = Math.max(frame.getWidth(), 1200);
        int h = Math.max(frame.getHeight(), 760);
        frame.setSize(w, h);
        frame.setLocationRelativeTo(null);
    }


    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text) {
            private Color normalColor = new Color(30, 40, 60);
            private Color hoverColor = new Color(50, 70, 100);
            private boolean hovering = false;

            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovering = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hovering = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(hovering ? hoverColor : normalColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("SansSerif", Font.PLAIN, 16));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        btn.setPreferredSize(new Dimension(400, 55));
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(
                0, 0, new Color(5, 15, 30),
                getWidth(), getHeight(), new Color(10, 25, 50)
        );
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(255, 255, 255, 5));
        g2.drawOval(-100, 100, 600, 600);
        g2.drawOval(getWidth() - 300, -100, 500, 500);
    }
    
    
}
