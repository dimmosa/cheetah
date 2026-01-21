package view;

import model.User;
import model.UserService;
import view.EmailService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Random;

public class ForgotPasswordDialog {

    private JDialog dialog;
    private JTextField emailField;
    private JTextField codeField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton sendCodeBtn;
    private JButton verifyBtn;
    private JButton resetBtn;
    private JPanel step1Panel;
    private JPanel step2Panel;
    private JPanel step3Panel;
    private String generatedCode;
    private String userEmail;
    private String username;

    public ForgotPasswordDialog(JFrame parent) {
        dialog = new JDialog(parent, "Reset Password", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());

        // Main container with dark background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(15, 25, 40),
                        getWidth(), getHeight(), new Color(25, 35, 50)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("🔒 Reset Password");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(title);
        mainPanel.add(Box.createVerticalStrut(10));

        step1Panel = createStep1Panel();
        step2Panel = createStep2Panel();
        step2Panel.setVisible(false);
        step3Panel = createStep3Panel();
        step3Panel.setVisible(false);

        mainPanel.add(step1Panel);
        mainPanel.add(step2Panel);
        mainPanel.add(step3Panel);

        dialog.add(mainPanel);
    }

    private JPanel createStep1Panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(400, 200));

        JLabel instruction = new JLabel("Enter your email address to receive a verification code");
        instruction.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instruction.setForeground(new Color(180, 190, 200));
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        emailField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 70, 90), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        emailField.setBackground(new Color(30, 40, 55));
        emailField.setForeground(Color.WHITE);
        emailField.setCaretColor(Color.WHITE);

        sendCodeBtn = createGradientButton("Send Verification Code");
        sendCodeBtn.addActionListener(e -> sendVerificationCode());

        panel.add(instruction);
        panel.add(Box.createVerticalStrut(20));
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(sendCodeBtn);

        return panel;
    }

    private JPanel createStep2Panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(400, 200));

        JLabel instruction = new JLabel("Enter the 6-digit code sent to your email");
        instruction.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instruction.setForeground(new Color(180, 190, 200));
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);

        codeField = new JTextField();
        codeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        codeField.setFont(new Font("SansSerif", Font.BOLD, 18));
        codeField.setHorizontalAlignment(JTextField.CENTER);
        codeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 70, 90), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        codeField.setBackground(new Color(30, 40, 55));
        codeField.setForeground(Color.WHITE);
        codeField.setCaretColor(Color.WHITE);

        verifyBtn = createGradientButton("Verify Code");
        verifyBtn.addActionListener(e -> verifyCode());

        panel.add(instruction);
        panel.add(Box.createVerticalStrut(20));
        panel.add(codeField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(verifyBtn);

        return panel;
    }

    private JPanel createStep3Panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(400, 250));

        JLabel instruction = new JLabel("Enter your new password");
        instruction.setFont(new Font("SansSerif", Font.PLAIN, 13));
        instruction.setForeground(new Color(180, 190, 200));
        instruction.setAlignmentX(Component.CENTER_ALIGNMENT);

        newPasswordField = createPasswordField();
        confirmPasswordField = createPasswordField();

        resetBtn = createGradientButton("Reset Password");
        resetBtn.addActionListener(e -> resetPassword());

        panel.add(instruction);
        panel.add(Box.createVerticalStrut(20));
        panel.add(newPasswordField);
        panel.add(Box.createVerticalStrut(15));
        panel.add(confirmPasswordField);
        panel.add(Box.createVerticalStrut(20));
        panel.add(resetBtn);

        return panel;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 70, 90), 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(new Color(30, 40, 55));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        return field;
    }

    private JButton createGradientButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0, 180, 255),
                        getWidth(), 0, new Color(0, 220, 100));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void sendVerificationCode() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "Please enter your email address.",
                    "Email Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate email format (logic unchanged) - UI improved
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showThemedAlert("Invalid Email",
                    "Please enter a valid email address.",
                    "❗",
                    new Color(255, 90, 90));
            return;
        }

        UserService userService = new UserService();
        User user = userService.getUserByEmail(email);

        if (user == null) {
            JOptionPane.showMessageDialog(dialog,
                    "No account found with this email address.",
                    "Email Not Found",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        username = user.getUsername();
        userEmail = email;

        generatedCode = String.format("%06d", new Random().nextInt(999999));

        sendCodeBtn.setEnabled(false);
        sendCodeBtn.setText("Sending...");

        new Thread(() -> {
            String status = EmailService.sendPasswordResetCode(email, generatedCode);

            SwingUtilities.invokeLater(() -> {
                sendCodeBtn.setEnabled(true);
                sendCodeBtn.setText("Send Verification Code");

                if (status.equals("EMAIL_SENT")) {
                    showEmailSentPopup(email);
                } else if (status.equals("DEMO_MODE")) {
                    showDemoModePopup(generatedCode, email);
                }

                step1Panel.setVisible(false);
                step2Panel.setVisible(true);
                dialog.revalidate();
                dialog.repaint();
            });
        }).start();
    }

    private void verifyCode() {
        String enteredCode = codeField.getText().trim();

        if (enteredCode.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "Please enter the verification code.",
                    "Code Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (enteredCode.equals(generatedCode)) {
            step2Panel.setVisible(false);
            step3Panel.setVisible(true);
            dialog.revalidate();
            dialog.repaint();
        } else {
            JOptionPane.showMessageDialog(dialog,
                    "Invalid verification code. Please try again.",
                    "Verification Failed",
                    JOptionPane.ERROR_MESSAGE);
            codeField.setText("");
        }
    }

    private void resetPassword() {
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "Please fill in both password fields.",
                    "Password Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(dialog,
                    "Password must be at least 6 characters long.",
                    "Password Too Short",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(dialog,
                    "Passwords do not match. Please try again.",
                    "Password Mismatch",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserService userService = new UserService();
        boolean success = userService.updatePassword(username, newPassword);

        if (success) {
            JOptionPane.showMessageDialog(dialog,
                    "Password reset successful!\nYou can now log in with your new password.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        } else {
            JOptionPane.showMessageDialog(dialog,
                    "Failed to reset password. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /* =========================
       UI FIXES (POPUPS ONLY)
       ========================= */

    // Small themed alert dialog (used for invalid email to match your modern popups)
    private void showThemedAlert(String title, String message, String emojiIcon, Color accent) {
        final int W = 420;
        final int H = 220;

        JDialog alert = new JDialog(dialog, title, true);
        alert.setUndecorated(true);
        alert.setSize(W, H);
        alert.setLocationRelativeTo(dialog);
        alert.setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, W, H, 24, 24));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 25, 40),
                        getWidth(), getHeight(), new Color(25, 35, 50));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);

                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 140));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 24, 24);
                g2.dispose();
            }
        };
        root.setBorder(new EmptyBorder(18, 22, 18, 22));
        root.setOpaque(false);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel(emojiIcon);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 18));
        t.setForeground(Color.WHITE);
        t.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("<html><div style='text-align:center; width:320px;'>" + message + "</div></html>");
        msg.setFont(new Font("SansSerif", Font.PLAIN, 13));
        msg.setForeground(new Color(180, 190, 200));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        msg.setHorizontalAlignment(SwingConstants.CENTER);

        center.add(Box.createVerticalGlue());
        center.add(icon);
        center.add(Box.createVerticalStrut(10));
        center.add(t);
        center.add(Box.createVerticalStrut(10));
        center.add(msg);
        center.add(Box.createVerticalGlue());

        JButton ok = createGradientButton("OK");
        ok.setMaximumSize(new Dimension(160, 42));
        ok.setPreferredSize(new Dimension(160, 42));
        ok.addActionListener(e -> alert.dispose());

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(ok);
        bottom.add(Box.createVerticalStrut(6));

        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        alert.setContentPane(root);
        alert.setVisible(true);
    }

    // Modern email sent popup (button will always be visible + true centered text)
    private void showEmailSentPopup(String email) {
        final int W = 480;
        final int H = 340;

        JDialog emailDialog = new JDialog(dialog, "Verification Sent", true);
        emailDialog.setUndecorated(true);
        emailDialog.setSize(W, H);
        emailDialog.setLocationRelativeTo(dialog);
        emailDialog.setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, W, H, 25, 25));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 25, 40),
                        getWidth(), getHeight(), new Color(25, 35, 50));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(new Color(0, 220, 100, 120));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 25, 25);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(24, 32, 18, 32));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel("✉️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Verification Code Sent");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 220, 100));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailLabel = new JLabel("<html><div style='text-align:center; width:360px;'>"
                + "A 6-digit code has been sent to:<br><b>" + email + "</b></div></html>");
        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        emailLabel.setForeground(new Color(180, 190, 200));
        emailLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel instructionLabel = new JLabel("<html><div style='text-align:center; width:360px;'>"
                + "Please check your inbox and enter the code<br>in the next step</div></html>");
        instructionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        instructionLabel.setForeground(new Color(150, 160, 170));
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(Box.createVerticalGlue());
        center.add(iconLabel);
        center.add(Box.createVerticalStrut(14));
        center.add(titleLabel);
        center.add(Box.createVerticalStrut(18));
        center.add(emailLabel);
        center.add(Box.createVerticalStrut(10));
        center.add(instructionLabel);
        center.add(Box.createVerticalGlue());

        JButton okBtn = createGradientButton("Continue");
        okBtn.setMaximumSize(new Dimension(200, 45));
        okBtn.setPreferredSize(new Dimension(200, 45));
        okBtn.addActionListener(e -> emailDialog.dispose());

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(okBtn);
        bottom.add(Box.createVerticalStrut(6));

        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        emailDialog.setContentPane(root);
        emailDialog.setVisible(true);
    }

    // Demo mode popup (button will always be visible + centered text)
    private void showDemoModePopup(String code, String email) {
        final int W = 520;
        final int H = 420;

        JDialog demoDialog = new JDialog(dialog, "Demo Mode", true);
        demoDialog.setUndecorated(true);
        demoDialog.setSize(W, H);
        demoDialog.setLocationRelativeTo(dialog);
        demoDialog.setShape(new java.awt.geom.RoundRectangle2D.Double(0, 0, W, H, 25, 25));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(0, 0, new Color(15, 25, 40),
                        getWidth(), getHeight(), new Color(25, 35, 50));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                g2.setColor(new Color(255, 180, 0, 140));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 25, 25);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(22, 34, 18, 34));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Offline Demo Mode");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(255, 180, 0));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel infoLabel = new JLabel("<html><div style='text-align:center; width:400px;'>"
                + "Unable to send email<br>Email service unavailable</div></html>");
        infoLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        infoLabel.setForeground(new Color(180, 190, 200));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(420, 1));
        separator.setForeground(new Color(60, 70, 90));
        separator.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel codeSectionLabel = new JLabel("Your Verification Code");
        codeSectionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        codeSectionLabel.setForeground(new Color(150, 160, 170));
        codeSectionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel codeLabel = new JLabel(code);
        codeLabel.setFont(new Font("Monospaced", Font.BOLD, 52));
        codeLabel.setForeground(new Color(0, 255, 200));
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hintLabel = new JLabel("<html><div style='text-align:center; width:400px;'>"
                + "Use this code to complete password reset</div></html>");
        hintLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hintLabel.setForeground(new Color(150, 160, 170));
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        center.add(Box.createVerticalGlue());
        center.add(iconLabel);
        center.add(Box.createVerticalStrut(12));
        center.add(titleLabel);
        center.add(Box.createVerticalStrut(10));
        center.add(infoLabel);
        center.add(Box.createVerticalStrut(18));
        center.add(separator);
        center.add(Box.createVerticalStrut(18));
        center.add(codeSectionLabel);
        center.add(Box.createVerticalStrut(8));
        center.add(codeLabel);
        center.add(Box.createVerticalStrut(8));
        center.add(hintLabel);
        center.add(Box.createVerticalGlue());

        JButton okBtn = createGradientButton("Got It");
        okBtn.setMaximumSize(new Dimension(180, 45));
        okBtn.setPreferredSize(new Dimension(180, 45));
        okBtn.addActionListener(e -> demoDialog.dispose());

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(okBtn);
        bottom.add(Box.createVerticalStrut(6));

        root.add(center, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        demoDialog.setContentPane(root);
        demoDialog.setVisible(true);
    }

    public void show() {
        dialog.setVisible(true);
    }
}
