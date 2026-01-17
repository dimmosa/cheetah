package view;

import com.formdev.flatlaf.FlatClientProperties;
import control.LoginControl;
import model.User;
import view.CustomPasswordField;
import view.CustomTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPrivateScreen extends JPanel {

    JFrame frame;

    public LoginPrivateScreen(JFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        togglePanel.setOpaque(false);
        JButton privateBtn = createToggleButton("👤 Private Account", true);
        JButton twoPlayerBtn = createToggleButton("👥 Two Players", false);
        togglePanel.add(privateBtn);
        togglePanel.add(twoPlayerBtn);

        twoPlayerBtn.addActionListener(e -> {
            setActiveToggle(twoPlayerBtn, privateBtn);

            frame.setContentPane(new LoginTwoPlayerScreen(frame));
            frame.revalidate();
            frame.repaint();
        });


        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(15, 25, 40, 230)); 
        card.setBorder(new EmptyBorder(40, 50, 50, 50));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 30");
        card.setPreferredSize(new Dimension(450, 500));

        JLabel iconLabel = new JLabel("💣");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 55));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel iconContainer = new JPanel();
        iconContainer.setOpaque(false);
        iconContainer.add(iconLabel);
        
        JLabel title = new JLabel("Minesweeper");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Private Account Login");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(0, 200, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComponent userField = createDarkInput("Username", "👤");
        JComponent passField = createDarkPasswordInput("Password", "🔒");

        JLabel forgotPass = new JLabel("Forgot Password?");
        forgotPass.setFont(new Font("SansSerif", Font.PLAIN, 12));
        forgotPass.setForeground(new Color(0, 150, 255));
        forgotPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPass.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JPanel fpPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        fpPanel.setOpaque(false);
        fpPanel.add(forgotPass);
        fpPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fpPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JButton signInBtn = new JButton("SIGN IN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 180, 255), getWidth(), 0, new Color(0, 220, 100));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                g2.setPaint(new Color(0, 255, 200, 50));
                g2.fillRoundRect(5, getHeight()-10, getWidth()-10, 15, 30, 30);
                
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

        LoginControl controller = new LoginControl();

        signInBtn.addActionListener(e -> {
            String username = ((JTextField) userField).getText();
            String password = ((JTextField) passField).getText();

            User user = controller.login(username, password);

            if (user != null) {
                JOptionPane.showMessageDialog(this, "Welcome back, " + user.getUsername() + "!");
                frame.setContentPane(new MainMenuPrivateScreen(frame));
                frame.revalidate();
                frame.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }
        });

        JLabel footer = new JLabel("<html>Don't have account? <font color='#00C6FF'><u>Sign Up</u></font></html>");
        footer.setForeground(Color.GRAY);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFrame f = (JFrame) SwingUtilities.getWindowAncestor(LoginPrivateScreen.this);
                f.setContentPane(new SignUpScreen(f));
                f.revalidate();
                f.repaint();
            }
        });


        card.add(iconContainer);
        card.add(Box.createVerticalStrut(10));
        card.add(title);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(40));
        card.add(userField);
        card.add(Box.createVerticalStrut(15));
        card.add(passField);
        card.add(fpPanel);
        card.add(Box.createVerticalStrut(30));
        card.add(signInBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(footer);

        mainContainer.add(togglePanel);
        mainContainer.add(Box.createVerticalStrut(20));
        mainContainer.add(card);

        add(mainContainer);
    }

    private void setActiveToggle(JButton active, JButton inactive) {
        active.setForeground(new Color(0, 220, 255));
        active.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 220, 255)),
                new EmptyBorder(5, 15, 5, 15)
        ));

        inactive.setForeground(Color.GRAY);
        inactive.setBorder(new EmptyBorder(7, 15, 7, 15));
    }


    private JButton createToggleButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        if (isActive) {
            btn.setForeground(new Color(0, 220, 255));
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 220, 255)),
                    new EmptyBorder(5, 15, 5, 15)
            ));
        } else {
            btn.setForeground(Color.GRAY);
            btn.setBorder(new EmptyBorder(7, 15, 7, 15));
        }
        return btn;
    }

    private JComponent createDarkInput(String placeholder, String icon) {
        return CustomTextField.createDarkInput(placeholder, icon);
    }

    private JComponent createDarkPasswordInput(String placeholder, String icon) {
        return CustomPasswordField.createDarkPasswordInput(placeholder, icon);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(0, 0, new Color(2, 5, 15), getWidth(), getHeight(), new Color(10, 20, 40));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        g2.setColor(new Color(255, 255, 255, 5));
        g2.drawOval(100, 100, 200, 200);
        g2.drawOval(getWidth()-200, getHeight()-200, 150, 150);
    }
}