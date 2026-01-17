package view;

import com.formdev.flatlaf.FlatClientProperties;
import control.SignUpControl;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SignUpScreen extends JPanel {

    JFrame frame;

    public SignUpScreen(JFrame frame) {
        this.frame = frame;

        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(20, 30, 50, 220));
        card.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        card.setPreferredSize(new Dimension(400, 550));
        
        JLabel title = new JLabel("Sign Up");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(0, 180, 255));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Create your account to start playing");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(150, 160, 180));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComponent nameField = createLabeledInput("Full Name", "👤", "Enter your full name");
        JComponent userField = createLabeledInput("Username", "👤", "Choose a username");
        JComponent passField = createLabeledInput("Password", "🔒", "Create a password");
        JComponent confirmField = createLabeledInput("Confirm Password", "🔒", "Re-enter your password");

        JButton signUpBtn = new JButton("Sign Up") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 123, 255), getWidth(), 0, new Color(0, 198, 255));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        signUpBtn.setForeground(Color.WHITE);
        signUpBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        signUpBtn.setContentAreaFilled(false);
        signUpBtn.setBorderPainted(false);
        signUpBtn.setFocusPainted(false);
        signUpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signUpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        SignUpControl controller = new SignUpControl();

        signUpBtn.addActionListener(e -> {
            String username = ((JTextField) userField.getComponent(2)).getText().trim();
            String password = ((JTextField) passField.getComponent(2)).getText();
            String confirm = ((JTextField) confirmField.getComponent(2)).getText();

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (username.length() < 3) {
                JOptionPane.showMessageDialog(this, "Username must be at least 3 characters", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (password.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User newUser = controller.signup(username, password);
            if (newUser != null) {
                JOptionPane.showMessageDialog(this, "Account created successfully!\nWelcome, " + username + "!", "Success", JOptionPane.INFORMATION_MESSAGE);

                frame.setContentPane(new MainMenuPrivateScreen(frame));
                frame.revalidate();
                frame.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose a different username.", "Signup Failed", JOptionPane.ERROR_MESSAGE);
            }
        });



        card.add(title);
        card.add(Box.createVerticalStrut(5));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(30));
        card.add(nameField);
        card.add(Box.createVerticalStrut(15));
        card.add(userField);
        card.add(Box.createVerticalStrut(15));
        card.add(passField);
        card.add(Box.createVerticalStrut(15));
        card.add(confirmField);
        card.add(Box.createVerticalStrut(30));
        card.add(signUpBtn);

        JLabel footer = new JLabel("<html>Already have an account? <font color='#00C6FF'><u>Login</u></font></html>");
        footer.setForeground(Color.GRAY);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(30));
        card.add(footer);

        footer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JFrame f = (JFrame) SwingUtilities.getWindowAncestor(SignUpScreen.this);
                f.setContentPane(new LoginPrivateScreen(f));
                f.revalidate();
                f.repaint();
            }
        });

        JPanel glowingContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 100, 255, 50));
                g2.fillRoundRect(5, 5, getWidth()-10, getHeight()-10, 40, 40);
                super.paintComponent(g);
            }
        };
        glowingContainer.setOpaque(false);
        glowingContainer.add(card);
        
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 30");

        add(glowingContainer);
    }

    private JComponent createLabeledInput(String labelText, String iconSymbol, String placeholder) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(200, 210, 220));
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField field = new JTextField();
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);

        JLabel iconLabel = new JLabel(iconSymbol);
        iconLabel.setForeground(Color.GRAY);
        iconLabel.setBorder(new EmptyBorder(0, 10, 0, 5));
        field.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_COMPONENT, iconLabel);
        
        field.putClientProperty(FlatClientProperties.STYLE,
            "arc: 15; " +
            "background: #1A263E; " +
            "foreground: #FFFFFF; " +
            "caretColor: #FFFFFF; " +
            "borderWidth: 1; " +
            "borderColor: #2D3B55; "
        );
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(field);
        return panel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        GradientPaint gp = new GradientPaint(0, 0, new Color(5, 10, 30), getWidth(), getHeight(), new Color(20, 40, 80));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
}