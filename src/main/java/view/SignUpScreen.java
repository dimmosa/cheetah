package view;

import model.UserService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SignUpScreen extends JPanel {

    JFrame frame;
    private JTextField usernameField;
    private JTextField emailField;  // ✅ NEW: Email field
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public SignUpScreen(JFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);

        // Card
        JPanel card = createRoundedCard(new Color(15, 25, 40, 230), 30);
        card.setBorder(new EmptyBorder(30, 40, 40, 40));
        card.setPreferredSize(new Dimension(450, 600));

        // Icon
        JLabel iconLabel = new JLabel(" 💣 ");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel iconContainer = new JPanel();
        iconContainer.setOpaque(false);
        iconContainer.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        iconContainer.add(iconLabel);
        iconContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        // Title
        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign up to play Minesweeper");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(0, 200, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input fields
        usernameField = createDarkInput("Username", "👤");
        emailField = createDarkInput("Email Address", "📧");  // ✅ NEW
        passwordField = createDarkPasswordInput("Password", "🔒");
        confirmPasswordField = createDarkPasswordInput("Confirm Password", "🔒");

        // Sign Up Button
        JButton signUpBtn = new JButton("CREATE ACCOUNT") {
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
        signUpBtn.setForeground(Color.WHITE);
        signUpBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        signUpBtn.setContentAreaFilled(false);
        signUpBtn.setBorderPainted(false);
        signUpBtn.setFocusPainted(false);
        signUpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        signUpBtn.addActionListener(e -> handleSignUp());

        // Footer
        JLabel footer = new JLabel("<html>Already have an account? <font color='#00C6FF'><u>Sign In</u></font></html>");
        footer.setForeground(Color.GRAY);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 13));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);

        footer.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                frame.setContentPane(new LoginPrivateScreen(frame));
                frame.revalidate();
                frame.repaint();
            }
        });

        // Layout
        card.add(iconContainer);
        card.add(Box.createVerticalStrut(5));
        card.add(title);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(25));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));
        card.add(emailField);  // ✅ NEW
        card.add(Box.createVerticalStrut(12));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));
        card.add(confirmPasswordField);
        card.add(Box.createVerticalStrut(25));
        card.add(signUpBtn);
        card.add(Box.createVerticalStrut(20));
        card.add(footer);

        mainContainer.add(card);
        add(mainContainer);
    }

    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();  // ✅ NEW
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ✅ Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Invalid Email",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 6 characters long.",
                    "Weak Password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match. Please try again.",
                    "Password Mismatch",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create account with email
        UserService userService = new UserService();
        boolean success = userService.signup(username, password, email);  // ✅ Pass email

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Account created successfully!\nYou can now log in.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            
            frame.setContentPane(new LoginPrivateScreen(frame));
            frame.revalidate();
            frame.repaint();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Username already exists. Please choose a different username.",
                    "Sign Up Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField createDarkInput(String placeholder, String icon) {
        return (JTextField) CustomTextField.createDarkInput(placeholder, icon);
    }

    private JPasswordField createDarkPasswordInput(String placeholder, String icon) {
        return (JPasswordField) CustomPasswordField.createDarkPasswordInput(placeholder, icon);
    }

    private JPanel createRoundedCard(Color bg, int arc) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                g2.setColor(bg);
                g2.fillRoundRect(1, 1, w - 2, h - 2, arc, arc);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
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