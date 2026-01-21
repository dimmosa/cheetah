package view;

import com.formdev.flatlaf.FlatClientProperties;
import model.User;
import model.SessionManager;
import model.UserService;
import view.CustomPasswordField;
import view.CustomTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginTwoPlayerScreen extends JPanel {

    JFrame frame;

    private JTextField player1User;
    private JTextField player1Pass;

    private JTextField player2User;
    private JTextField player2Pass;


    public LoginTwoPlayerScreen(JFrame frame) {
        this.frame = frame;

        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        togglePanel.setOpaque(false);
        JButton privateBtn = createToggleButton("👤 Private Account", false);
        JButton twoPlayerBtn = createToggleButton("👥 Two Players", true);
        togglePanel.add(privateBtn);
        togglePanel.add(twoPlayerBtn);

        privateBtn.addActionListener(e -> {
            frame.setContentPane(new LoginPrivateScreen(frame));
            frame.revalidate();
            frame.repaint();
        });


        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(15, 25, 40, 230));
        card.setBorder(new EmptyBorder(30, 40, 40, 40));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 30");
        card.setPreferredSize(new Dimension(700, 550));

        JLabel iconLabel = new JLabel("💣");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Minesweeper");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Two Player Login");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(0, 200, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel playersGrid = new JPanel(new GridLayout(1, 2, 30, 0));
        playersGrid.setOpaque(false);
        playersGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JPanel p1Panel = createPlayerColumn("Player 1", new Color(0, 150, 255));
        JPanel p2Panel = createPlayerColumn("Player 2", new Color(0, 200, 100));
        
        playersGrid.add(p1Panel);
        playersGrid.add(p2Panel);

        JButton signInBtn = new JButton("SIGN IN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 180, 255), getWidth(), 0, new Color(0, 220, 100));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        signInBtn.setContentAreaFilled(false);
        signInBtn.setBorderPainted(false);
        signInBtn.setFocusPainted(false);
        signInBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        signInBtn.addActionListener(e -> {
            String p1 = player1User.getText();
            String p1Pass = player1Pass.getText();

            String p2 = player2User.getText();
            String p2Pass = player2Pass.getText();

            UserService service = new UserService();

            User player1 = service.getUser(p1);
            User player2 = service.getUser(p2);

            if (player1 != null && player1.getPassword().equals(p1Pass) &&
                    player2 != null && player2.getPassword().equals(p2Pass)) {

                SessionManager.getInstance().loginTwoPlayers(player1, player2);

                JOptionPane.showMessageDialog(this,
                        "Welcome " + player1.getUsername() + " and " + player2.getUsername() + "!");

                frame.setContentPane(new MainMenuTwoPlayerScreen(frame));
                frame.revalidate();
                frame.repaint();

            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid login for one or both players.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JLabel footer = new JLabel("<html>Don't have accounts? <font color='#00C6FF'><u>Sign Up</u></font></html>");
        footer.setForeground(Color.GRAY);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFrame f = (JFrame) SwingUtilities.getWindowAncestor(LoginTwoPlayerScreen.this);
                f.setContentPane(new SignUpScreen(f));
                f.revalidate();
                f.repaint();
            }
        });

        card.add(iconLabel);
        card.add(title);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(30));
        card.add(playersGrid);
        card.add(Box.createVerticalStrut(30));
        card.add(signInBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(footer);

        mainContainer.add(togglePanel);
        mainContainer.add(Box.createVerticalStrut(20));
        mainContainer.add(card);

        add(mainContainer);
    }

    private JPanel createPlayerColumn(String titleText, Color badgeColor) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel badge = new JLabel(titleText);
        badge.setOpaque(true);
        badge.setBackground(new Color(badgeColor.getRed(), badgeColor.getGreen(), badgeColor.getBlue(), 50));
        badge.setForeground(badgeColor);
        badge.setFont(new Font("SansSerif", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(5, 15, 5, 15));

        JPanel badgeWrapper = new JPanel();
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(badge);

        p.add(badgeWrapper);
        p.add(Box.createVerticalStrut(10));

        JTextField userField = createDarkInput("Username", "👤");
        JTextField passField = createDarkPasswordInput("Password", "🔒");

        if (titleText.equals("Player 1")) {
            player1User = userField;
            player1Pass = passField;
        } else {
            player2User = userField;
            player2Pass = passField;
        }

        p.add(userField);
        p.add(Box.createVerticalStrut(15));
        p.add(passField);

        return p;
    }


    private JButton createToggleButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        if (isActive) {
            btn.setForeground(new Color(0, 220, 100));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 220, 100)),
                    new EmptyBorder(5, 15, 5, 15)
            ));
        } else {
            btn.setForeground(Color.GRAY);
            btn.setBorder(new EmptyBorder(7, 15, 7, 15));
        }
        return btn;
    }

    private JTextField createDarkInput(String placeholder, String icon) {
        return (JTextField) CustomTextField.createDarkInput(placeholder, icon);
    }

    private JTextField createDarkPasswordInput(String placeholder, String icon) {
        return (JTextField) CustomPasswordField.createDarkPasswordInput(placeholder, icon);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(2, 5, 15), getWidth(), getHeight(), new Color(10, 20, 40));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}