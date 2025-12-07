package view;

import control.QuestionController;
import model.Question;
import model.SysData;
import model.NeonTableRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class QuestionManagementScreen extends JPanel {

    JFrame frame;

    private DefaultTableModel model;
    private JTable questionTable;

    private JTextArea questionText;
    private JTextField[] answerFields;
    private JComboBox<String> correctIndexDropdown;
    private JComboBox<String> difficultyDropdown;

    private final QuestionController controller = new QuestionController();

    public QuestionManagementScreen(JFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(15, 15, 25));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("<html><font color='#00BFFF'>QUESTION</font> <font color='#87CEFA'>MANAGER</font></html>");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);

        model = new DefaultTableModel(new Object[]{"ID", "Question Text", "Difficulty"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        questionTable = new JTable(model);
        styleTable(questionTable);

        JScrollPane scrollPane = new JScrollPane(questionTable);
        scrollPane.getViewport().setBackground(new Color(15, 15, 25));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 255), 1));

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = createActionButtonsPanel();
        centerPanel.add(actionPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        JPanel detailsPanel = createDetailsPanel();
        add(detailsPanel, BorderLayout.SOUTH);

        loadQuestions();

        questionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && questionTable.getSelectedRow() != -1) {
                displayQuestionDetails(questionTable.getSelectedRow());
            }
        });
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setGridColor(new Color(30, 30, 50));
        table.setBackground(new Color(20, 20, 35));
        table.setForeground(new Color(220, 220, 255));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));

        table.getTableHeader().setBackground(new Color(0, 100, 150));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 16));
        table.getTableHeader().setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new NeonTableRenderer());

        table.getColumnModel().getColumn(0).setMinWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);

        table.getColumnModel().getColumn(2).setMinWidth(120);
        table.getColumnModel().getColumn(2).setMaxWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);

        table.getColumnModel().getColumn(1).setPreferredWidth(600);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private JPanel createActionButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JButton addButton = createNeonButton("ADD NEW", new Color(0, 200, 0));
        JButton editButton = createNeonButton("SAVE EDITS", new Color(255, 215, 0));
        JButton deleteButton = createNeonButton("DELETE", new Color(200, 0, 0));
        JButton backButton = createNeonButton("BACK TO MENU", new Color(0, 150, 255));

        Dimension buttonSize = new Dimension(180, 45);
        addButton.setMaximumSize(buttonSize);
        editButton.setMaximumSize(buttonSize);
        deleteButton.setMaximumSize(buttonSize);
        backButton.setMaximumSize(buttonSize);

        backButton.addActionListener(
                e -> {

                    frame.setContentPane(new MainMenuTwoPlayerScreen(frame));
                    frame.revalidate();
                    frame.repaint();
                }
        );

        panel.add(addButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(editButton);
        panel.add(Box.createVerticalStrut(10));
        panel.add(deleteButton);
        panel.add(Box.createVerticalGlue());
        panel.add(backButton);

        addButton.addActionListener(e -> clearDetailsPanelForNew());
        editButton.addActionListener(e -> saveQuestion(questionTable.getSelectedRow() == -1));
        deleteButton.addActionListener(e -> deleteSelectedQuestion());
        backButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Returning to Menu", "Navigation", JOptionPane.INFORMATION_MESSAGE));

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0, 150, 255), 1),
                "Question Details",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 18),
                new Color(135, 206, 250)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(createLabel("Question Text:"), gbc);

        questionText = createTextArea();
        JScrollPane textScrollPane = new JScrollPane(questionText);
        textScrollPane.setPreferredSize(new Dimension(500, 80));
        textScrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 70)));
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3; gbc.weightx = 1.0;
        panel.add(textScrollPane, gbc);

        answerFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.gridwidth = 1; gbc.weightx = 0;
            panel.add(createLabel("Answer " + (i + 1) + ":"), gbc);

            answerFields[i] = createTextField();
            gbc.gridx = 1; gbc.gridy = i + 1; gbc.gridwidth = 1; gbc.weightx = 0.5;
            panel.add(answerFields[i], gbc);
        }

        gbc.gridx = 2; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(createLabel("Correct Answer Index:"), gbc);

        correctIndexDropdown = createComboBox(new String[]{"1", "2", "3", "4"});
        gbc.gridx = 3; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.5;
        panel.add(correctIndexDropdown, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0;
        panel.add(createLabel("Difficulty:"), gbc);

        difficultyDropdown = createComboBox(new String[]{"Easy", "Medium", "Hard"});
        gbc.gridx = 3; gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.5;
        panel.add(difficultyDropdown, gbc);

        clearDetailsPanelForNew();

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(180, 180, 200));
        label.setFont(new Font("SansSerif", Font.PLAIN, 16));
        return label;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea(3, 30);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(new Color(30, 30, 50));
        area.setForeground(Color.WHITE);
        area.setCaretColor(new Color(0, 200, 255));
        return area;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(30);
        field.setBackground(new Color(30, 30, 50));
        field.setForeground(Color.WHITE);
        field.setCaretColor(new Color(0, 200, 255));
        field.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return field;
    }

    private JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> box = new JComboBox<>(items);
        box.setBackground(new Color(30, 30, 50));
        box.setForeground(Color.WHITE);
        return box;
    }


    private void clearDetailsPanelForNew() {
        questionText.setText("");
        for (JTextField field : answerFields) {
            field.setText("");
        }
        correctIndexDropdown.setSelectedIndex(0);
        difficultyDropdown.setSelectedIndex(0);
        questionTable.clearSelection();
    }


    private String getDifficultyString(int diff) {
        return switch (diff) {
            case 1 -> "Easy";
            case 2 -> "Medium";
            case 3 -> "Hard";
            case 4 -> "Expert";
            default -> "Unknown";
        };
    }

    public void loadQuestions() {
        model.setRowCount(0);

        for (Question q : controller.getAllQuestions()) {
            model.addRow(new Object[]{
                    q.getId(),
                    q.getText(),
                    QuestionController.difficultyToString(q.getDifficulty())
            });
        }
    }

    private void displayQuestionDetails(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= model.getRowCount()) return;

        int questionId = (int) model.getValueAt(rowIndex, 0);

        Question selectedQ = SysData.getInstance().getQuestions()
                .stream()
                .filter(q -> q.getId() == questionId)
                .findFirst()
                .orElse(null);

        if (selectedQ != null) {
            questionText.setText(selectedQ.getText());

            String[] answers = selectedQ.getAnswers();
            for (int i = 0; i < 4; i++) {
                answerFields[i].setText(answers[i]);
            }

            correctIndexDropdown.setSelectedIndex(selectedQ.getCorrectIndex());

            String difficultyStr = getDifficultyString(selectedQ.getDifficulty());
            difficultyDropdown.setSelectedItem(difficultyStr);
        }
    }

    private void saveQuestion(boolean isNew) {

        Question q = getQuestionFromInputFields();
        if (q == null) return;

        if (q.getCorrectIndex() < 0 || q.getCorrectIndex() > 3) {
            q.setCorrectIndex(Math.max(0, Math.min(3, q.getCorrectIndex() - 1)));
        }

        boolean success;
        String action;

        if (isNew) {
            success = controller.addQuestion(q);
            action = "Added";

        } else {

            int row = questionTable.getSelectedRow();
            if (row == -1) return;

            int id = (int) model.getValueAt(row, 0);

            q.setId(id);

            success = controller.updateQuestion(q);
            action = "Updated";
        }

        JOptionPane.showMessageDialog(
                this,
                "Question " + action + (success ? " successfully." : " failed."),
                success ? "Success" : "Error",
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
        );

        loadQuestions();
        clearDetailsPanelForNew();
    }


    private void deleteSelectedQuestion() {
        int row = questionTable.getSelectedRow();
        if (row == -1) return;

        int id = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete question ID " + id + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = controller.deleteQuestion(id);

        JOptionPane.showMessageDialog(
                this,
                success ? "Deleted successfully." : "Failed to delete.",
                success ? "Success" : "Error",
                success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
        );

        loadQuestions();
        clearDetailsPanelForNew();
    }


    private Question getQuestionFromInputFields() {
        String questionTex = questionText.getText().trim();
        if (questionTex.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Question text cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        String[] answers = new String[4];
        for (int i = 0; i < 4; i++) {
            String answer = answerFields[i].getText().trim();
            if (answer.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Answer option " + (char)('A' + i) + " cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            answers[i] = answer;
        }

        int correctAnsIndex = Integer.parseInt(correctIndexDropdown.getSelectedItem().toString()) - 1;

        int difficulty = Question.getDifficultyFromString(difficultyDropdown.getSelectedItem().toString());

        return new Question(0, questionTex, answers, correctAnsIndex, difficulty);
    }


    private JButton createNeonButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color.darker());
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(color, 2));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        return button;
    }
}