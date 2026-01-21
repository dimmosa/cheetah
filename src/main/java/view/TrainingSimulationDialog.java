package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

import static view.CustomIconButton.createNeonButton;

public class TrainingSimulationDialog extends JDialog {

    private enum Step { INTRO, FLAG, MINE, QUESTION, SURPRISE, HINT, FINISH }
    private Step step = Step.INTRO;

    private final JLabel title = new JLabel("", SwingConstants.CENTER);
    private final JLabel progress = new JLabel("", SwingConstants.CENTER);

    private final TypewriterTextArea typed = new TypewriterTextArea();
    private final NeonToast toast = new NeonToast();

    private MinesweeperBoardPanelTraining board;

    private final JButton startBtn = createNeonButton("BEGIN TRAINING", new Color(0,255,128), 220, 34);
    private final JButton nextBt   = createNeonButton("NEXT ▶", new Color(0,220,255), 120, 34);
    private final JButton hintBtn  = createNeonButton("💡 USE HINT", new Color(255,200,0), 210, 34);
    private final JButton closeBtn = createNeonButton("CLOSE", new Color(255,60,60), 120, 34);

    // ✅ prevents skipping the hint step
    private boolean hintUsed = false;

    public TrainingSimulationDialog(JFrame owner) {
        super(owner, "Training Simulation", true);

        setUndecorated(true);
        setSize(900, 600);
        setLocationRelativeTo(owner);
        setBackground(new Color(0, 0, 0, 0));
        getRootPane().setOpaque(true);

        JPanel root = new JPanel(new BorderLayout(12, 12)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(4, 4, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);

                g2.setColor(new Color(0, 255, 255, 150));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 26, 26);
            }
        };
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setOpaque(true);
        setContentPane(root);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(0, 255, 255));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        progress.setFont(new Font("SansSerif", Font.BOLD, 13));
        progress.setForeground(new Color(200, 200, 240));
        progress.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(progress);

        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 0));
        center.setOpaque(false);

        JPanel leftCard = glassCard();
        leftCard.setLayout(new BorderLayout(10, 10));
        leftCard.add(typed, BorderLayout.CENTER);
        leftCard.add(toast, BorderLayout.SOUTH);
        center.add(leftCard);

        Consumer<String> onAction = this::handleAction;
        board = new MinesweeperBoardPanelTraining(onAction);

        JPanel rightCard = glassCard();
        rightCard.setLayout(new BorderLayout());
        rightCard.add(board, BorderLayout.CENTER);
        center.add(rightCard);

        root.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        footer.setOpaque(false);

        // BEGIN TRAINING
        startBtn.addActionListener(e -> {
            step = Step.FLAG;
            hintUsed = false;
            if (board != null) {
                board.prepareForStep("FLAG");
                board.setTrainingEnabled(true);
            }
            updateStepUI(true);
        });

        // NEXT (block skipping HINT)
        nextBt.addActionListener(e -> {
            if (step == Step.HINT && !hintUsed) {
                toast.showToast("💡 You must use the hint first!", 1200);
                return;
            }
            advance();
        });

        // USE HINT (only activates in HINT step)
        hintBtn.addActionListener(e -> {
            if (step != Step.HINT) {
                toast.showToast("⏳ Hint is available only in the HINT task.", 1100);
                return;
            }
            if (board == null) return;

            board.useHintFromDialog(); // זה גם מצייר וגם שולח onAction("HINT")
            hintUsed = true;
            handleAction("HINT");       // toast + advance to FINISH
        });

        closeBtn.addActionListener(e -> dispose());

        footer.add(startBtn);
        footer.add(hintBtn);
        footer.add(nextBt);
        footer.add(closeBtn);

        root.add(footer, BorderLayout.SOUTH);

        updateStepUI(false);
    }

    private JPanel glassCard() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(18, 18, 32, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                g2.setColor(new Color(100, 100, 180, 180));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14, 14, 14, 14));
        return p;
    }

    private void handleAction(String action) {
        switch (step) {
            case FLAG -> {
                if ("FLAG".equals(action)) {
                    Toolkit.getDefaultToolkit().beep();
                    toast.showToast("✅ Perfect! Correct flags save lives in CO-OP!", 1400);
                    advance();
                } else {
                    toast.showToast("💡 Hint: RIGHT-CLICK on the glowing mine tile 🚩", 1200);
                }
            }
            case MINE -> {
                if ("MINE".equals(action)) {
                    Toolkit.getDefaultToolkit().beep();
                    toast.showToast("💣 BOOM! In real game: -1 shared life ❤️ + turn ends!", 1600);
                    advance();
                } else {
                    toast.showToast("💡 Hint: LEFT-CLICK the glowing mine tile 💣", 1200);
                }
            }
            case QUESTION -> {
                if ("QUESTION".equals(action)) {
                    Toolkit.getDefaultToolkit().beep();
                    toast.showToast("🧠 Great! Questions unlock strategy options!", 1600);
                    advance();
                } else {
                    toast.showToast("💡 Hint: LEFT-CLICK the glowing ❓ tile", 1200);
                }
            }
            case SURPRISE -> {
                if ("SURPRISE".equals(action)) {
                    Toolkit.getDefaultToolkit().beep();
                    toast.showToast("🎁 Nice! 50/50 gamble: bonus or penalty!", 1800);
                    advance();
                } else {
                    toast.showToast("💡 Hint: LEFT-CLICK the glowing 🎁 tile", 1200);
                }
            }
            case HINT -> {
                if ("HINT".equals(action)) {
                    Toolkit.getDefaultToolkit().beep();
                    toast.showToast("🔥 Awesome! HOT/WARM/COLD shows mine distance!", 1800);
                    advance();
                } else {
                    toast.showToast("💡 Hint: Press the 💡 USE HINT button below!", 1200);
                }
            }
            default -> {}
        }
    }

    private void advance() {
        if (step == Step.INTRO || step == Step.FINISH) return;

        step = switch (step) {
            case FLAG -> Step.MINE;
            case MINE -> Step.QUESTION;
            case QUESTION -> Step.SURPRISE;
            case SURPRISE -> Step.HINT;
            case HINT -> Step.FINISH;
            default -> step;
        };

        if (step == Step.HINT) hintUsed = false;

        if (step != Step.FINISH && board != null) {
            String stepName = switch (step) {
                case FLAG -> "FLAG";
                case MINE -> "MINE";
                case QUESTION -> "QUESTION";
                case SURPRISE -> "SURPRISE";
                case HINT -> "HINT";
                default -> "";
            };

            // IMPORTANT: prepareForStep("HINT") must NOT call showHotColdHint() inside the board
            board.prepareForStep(stepName);
        }

        updateStepUI(true);
    }

    private void updateStepUI(boolean animate) {
        // Buttons visibility
        startBtn.setVisible(step == Step.INTRO);

        nextBt.setVisible(step != Step.INTRO && step != Step.FINISH);
        nextBt.setEnabled(!(step == Step.HINT && !hintUsed));

        // ✅ show hint button during training, but enable only in HINT step
        hintBtn.setVisible(step != Step.INTRO && step != Step.FINISH);
        hintBtn.setEnabled(step == Step.HINT && !hintUsed);

        closeBtn.setVisible(true);

        if (board != null) {
            board.setTrainingEnabled(step != Step.INTRO && step != Step.FINISH);
        }

        String header;
        String text;
        int stepNum = 0;
        int total = 5;

        switch (step) {
            case INTRO -> {
                header = "🧪 TRAINING SIMULATION";
                progress.setText("Safe field • No score • No lives • Learn by doing");
                text =
                        "AGENTS, WELCOME TO TRAINING!\n\n" +
                        "This is a SAFE training field where you can learn without risk.\n\n" +
                        "You will learn CO-OP mechanics by completing 5 simple tasks.\n\n" +
                        "CONTROLS:\n" +
                        "• Right-Click = Place Flag 🚩\n" +
                        "• Left-Click = Reveal Tile\n\n" +
                        "Ready? Press BEGIN TRAINING to start!";
            }
            case FLAG -> {
                stepNum = 1;
                header = "TASK 1 — FLAG A MINE";
                progress.setText(stepNum + "/" + total + " • Right-click the glowing mine tile");
                text =
                        "YOUR MISSION:\n" +
                        "Place a flag 🚩 on the mine tile (the one with cyan glow).\n\n" +
                        "RIGHT-CLICK on the glowing mine tile now!";
            }
            case MINE -> {
                stepNum = 2;
                header = "TASK 2 — REVEAL A MINE";
                progress.setText(stepNum + "/" + total + " • Learn what happens when you hit a mine");
                text =
                        "YOUR MISSION:\n" +
                        "Click the mine tile 💣 (the glowing one).\n\n" +
                        "LEFT-CLICK on the glowing mine tile now!";
            }
            case QUESTION -> {
                stepNum = 3;
                header = "TASK 3 — ACTIVATE A QUESTION";
                progress.setText(stepNum + "/" + total + " • Questions give you advantages!");
                text =
                        "YOUR MISSION:\n" +
                        "Click the ❓ question tile (the glowing one).\n\n" +
                        "LEFT-CLICK on the glowing ❓ tile now!";
            }
            case SURPRISE -> {
                stepNum = 4;
                header = "TASK 4 — ACTIVATE A SURPRISE";
                progress.setText(stepNum + "/" + total + " • High risk = High reward!");
                text =
                        "YOUR MISSION:\n" +
                        "Click the 🎁 surprise tile (the glowing one).\n\n" +
                        "LEFT-CLICK on the glowing 🎁 tile now!";
            }
            case HINT -> {
                stepNum = 5;
                header = "TASK 5 — USE HOT/COLD HINT SYSTEM";
                progress.setText(stepNum + "/" + total + " • Press 💡 USE HINT to activate");
                text =
                        "YOUR MISSION:\n" +
                        "Press the 💡 USE HINT button below.\n\n" +
                        "HOW HINTS WORK:\n" +
                        "• 🔥 HOT  = Very close to a mine\n" +
                        "• 🌡️ WARM = Near a mine\n" +
                        "• ❄️ COLD = Far from mines\n\n" +
                        "Press the 💡 USE HINT button now!";
            }
            case FINISH -> {
                header = "✅ TRAINING COMPLETE - YOU'RE READY!";
                progress.setText("Congratulations! You know the basics now!");
                text =
                        "EXCELLENT WORK, AGENT!\n\n" +
                        "You've completed all training tasks.\n\n" +
                        "Close this window and start playing!";
            }
            default -> {
                header = "";
                text = "";
            }
        }

        title.setText(header);

        if (animate) typed.typeText(text, 12);
        else typed.setInstantText(text);

        revalidate();
        repaint();
    }

    private static class TypewriterTextArea extends JPanel {
        private final JTextArea area = new JTextArea();
        private Timer timer;

        TypewriterTextArea() {
            setOpaque(false);
            setLayout(new BorderLayout());
            area.setOpaque(false);
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(new Font("SansSerif", Font.PLAIN, 15));
            area.setForeground(new Color(245, 245, 255));
            area.setBorder(new EmptyBorder(6, 6, 6, 6));
            add(area, BorderLayout.CENTER);
        }

        void setInstantText(String s) {
            stop();
            area.setText(s);
        }

        void typeText(String s, int msPerChar) {
            stop();
            area.setText("");
            final int[] i = {0};

            timer = new Timer(msPerChar, e -> {
                if (i[0] >= s.length()) { stop(); return; }
                area.append(String.valueOf(s.charAt(i[0]++)));
            });
            timer.start();
        }

        private void stop() {
            if (timer != null) timer.stop();
            timer = null;
        }
    }

    private static class NeonToast extends JPanel {
        private final JLabel msg = new JLabel("", SwingConstants.CENTER);
        private Timer hideTimer;

        NeonToast() {
            setOpaque(false);
            setLayout(new BorderLayout());
            msg.setFont(new Font("SansSerif", Font.BOLD, 13));
            msg.setForeground(new Color(255, 255, 255));
            msg.setBorder(new EmptyBorder(10, 10, 10, 10));
            add(msg, BorderLayout.CENTER);
            setVisible(false);
        }

        void showToast(String text, int ms) {
            msg.setText(text);
            setVisible(true);
            repaint();

            if (hideTimer != null) hideTimer.stop();
            hideTimer = new Timer(ms, e -> setVisible(false));
            hideTimer.setRepeats(false);
            hideTimer.start();
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(0, 220, 255, 130));
            g2.fillRoundRect(6, 6, w - 12, h - 12, 14, 14);

            g2.setColor(new Color(0, 255, 255, 200));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(6, 6, w - 12, h - 12, 14, 14);
        }

        @Override public Dimension getPreferredSize() {
            return new Dimension(super.getPreferredSize().width, 56);
        }
    }
}
