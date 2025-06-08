
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.*;
import java.util.List;

public class Typegame extends JFrame {
    // UI Components
    private JLabel promptLabel, timerLabel, wpmLabel, accuracyLabel, highScoreLabel, feedbackLabel;
    private JTextArea inputArea;
    private JButton startButton, restartButton, exitButton, themeButton;
    private JComboBox<String> difficultyCombo;
    private JPanel statsPanel, controlPanel;
    private JScrollPane promptScroll, historyScroll;
    private JProgressBar wpmProgressBar, accuracyProgressBar;
    
    // Game state
    private boolean gameRunning = false;
    private Instant gameStartTime;
    private int correctChars = 0;
    private int totalChars = 0;
    private List<Integer> wpmHistory = new ArrayList<>();
    private boolean darkMode = false;
    private HighScoreManager highScores = new HighScoreManager();
    private String currentPrompt = "";
    
    // Colors
    private Color bgColor, textColor, primaryColor, accentColor, correctColor, errorColor;
    
    // Difficulty levels
    enum Difficulty {
        EASY(15, "Easy", 1.0f), 
        MEDIUM(25, "Medium", 1.2f), 
        HARD(40, "Hard", 1.5f);
        
        final int length;
        final String name;
        final float multiplier;
        Difficulty(int length, String name, float multiplier) {
            this.length = length;
            this.name = name;
            this.multiplier = multiplier;
        }
    }
    
    // Word bank
    private static final List<String> WORDS = List.of(
        "the", "quick", "brown", "fox", "jumps", "over", "lazy", "dog",
        "Java", "programming", "language", "developer", "computer",
        "algorithm", "data", "structure", "typing", "speed", "test",
        "keyboard", "efficient", "productive", "coding", "debugging"
    );

    public Typegame() {
        initializeComponents();
        setTheme(false); // Start with light theme
        setupUI();
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeComponents() {
        promptLabel = new JLabel();
        inputArea = new JTextArea();
        timerLabel = new JLabel();
        wpmLabel = new JLabel();
        accuracyLabel = new JLabel();
        highScoreLabel = new JLabel();
        feedbackLabel = new JLabel();
        startButton = new JButton("Start");
        restartButton = new JButton("Restart");
        exitButton = new JButton("Exit");
        themeButton = new JButton("🌙 Dark");
        difficultyCombo = new JComboBox<>();
        wpmProgressBar = new JProgressBar(0, 150);
        accuracyProgressBar = new JProgressBar(0, 100);
    }

    private void setTheme(boolean dark) {
        darkMode = dark;
        if (darkMode) {
            bgColor = new Color(40, 44, 52);
            textColor = Color.WHITE;
            primaryColor = new Color(100, 149, 237);
            accentColor = new Color(255, 165, 0);
            correctColor = new Color(144, 238, 144);
            errorColor = new Color(255, 102, 102);
            themeButton.setText("☀️ Light");
        } else {
            bgColor = new Color(240, 240, 240);
            textColor = Color.BLACK;
            primaryColor = new Color(70, 130, 180);
            accentColor = new Color(255, 140, 0);
            correctColor = new Color(152, 251, 152);
            errorColor = new Color(255, 160, 122);
            themeButton.setText("🌙 Dark");
        }
        applyTheme();
    }

    private void applyTheme() {
        getContentPane().setBackground(bgColor);
        
        // Set component colors
        promptLabel.setForeground(textColor);
        inputArea.setBackground(darkMode ? new Color(60, 64, 72) : Color.WHITE);
        inputArea.setForeground(textColor);
        inputArea.setCaretColor(textColor);
        
        timerLabel.setForeground(textColor);
        wpmLabel.setForeground(textColor);
        accuracyLabel.setForeground(textColor);
        highScoreLabel.setForeground(textColor);
        feedbackLabel.setForeground(accentColor);
        
        // Progress bars
        styleProgressBar(wpmProgressBar, primaryColor);
        styleProgressBar(accuracyProgressBar, accentColor);
        
        // Buttons
        styleButton(startButton, primaryColor);
        styleButton(restartButton, accentColor);
        styleButton(exitButton, new Color(220, 53, 69));
        styleButton(themeButton, darkMode ? accentColor : primaryColor);
    }

    private void styleProgressBar(JProgressBar bar, Color color) {
        bar.setForeground(color);
        bar.setBackground(bgColor);
        bar.setBorder(BorderFactory.createLineBorder(color, 1));
        bar.setStringPainted(true);
    }

    private void styleButton(JButton button, Color bg) {
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 2),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
    }

    private void setupUI() {
        setLayout(new BorderLayout(15, 15));
        
        // Control Panel (Top)
        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(bgColor);
        
        difficultyCombo.setModel(new DefaultComboBoxModel<>(
            Arrays.stream(Difficulty.values())
                .map(d -> d.name)
                .toArray(String[]::new)
        ));
        difficultyCombo.setSelectedIndex(1); // Default to Medium
        
        themeButton.addActionListener(e -> setTheme(!darkMode));
        
        controlPanel.add(new JLabel("Difficulty:"));
        controlPanel.add(difficultyCombo);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(themeButton);
        add(controlPanel, BorderLayout.NORTH);
        
        // Prompt Area (Center Top)
        promptLabel.setFont(new Font("Consolas", Font.BOLD, 20));
        promptLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        promptScroll = new JScrollPane(promptLabel);
        promptScroll.setBorder(BorderFactory.createTitledBorder("Type This Text"));
        add(promptScroll, BorderLayout.CENTER);
        
        // Input Area (Center Bottom)
        inputArea.setFont(new Font("Consolas", Font.PLAIN, 18));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setMargin(new Insets(15, 15, 15, 15));
        inputArea.setEnabled(false);
        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { checkTyping(); }
            @Override
            public void removeUpdate(DocumentEvent e) { checkTyping(); }
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        add(new JScrollPane(inputArea), BorderLayout.SOUTH);
        
        // Stats Panel (Right)
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        statsPanel.setBackground(bgColor);
        
        // Timer
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statsPanel.add(createStatPanel("⏱️ Time Remaining", timerLabel));
        
        // WPM with progress bar
        wpmLabel.setFont(new Font("Arial", Font.BOLD, 18));
        wpmProgressBar.setString("WPM");
        statsPanel.add(createStatPanel("🚀 Speed (WPM)", wpmLabel, wpmProgressBar));
        
        // Accuracy with progress bar
        accuracyLabel.setFont(new Font("Arial", Font.BOLD, 18));
        accuracyProgressBar.setString("Accuracy");
        statsPanel.add(createStatPanel("🎯 Accuracy", accuracyLabel, accuracyProgressBar));
        
        // Feedback
        feedbackLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        statsPanel.add(createStatPanel("💡 Feedback", feedbackLabel));
        
        // High Scores
        highScoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statsPanel.add(createStatPanel("🏆 High Scores", highScoreLabel));
        
        add(statsPanel, BorderLayout.EAST);
        
        // Button Panel (Bottom)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBackground(bgColor);
        
        startButton.addActionListener(e -> startGame());
        restartButton.addActionListener(e -> restartGame());
        exitButton.addActionListener(e -> System.exit(0));
        
        buttonPanel.add(startButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize values
        timerLabel.setText("60 seconds");
        wpmLabel.setText("0 WPM");
        accuracyLabel.setText("0%");
        highScoreLabel.setText(highScores.toString());
        feedbackLabel.setText("Press Start to begin");
    }

    private JPanel createStatPanel(String title, JComponent... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(accentColor);
        panel.add(titleLabel);
        
        for (JComponent comp : components) {
            panel.add(comp);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        return panel;
    }

    private void startGame() {
        gameRunning = true;
        gameStartTime = Instant.now();
        correctChars = 0;
        totalChars = 0;
        wpmHistory.clear();
        
        inputArea.setText("");
        inputArea.setEnabled(true);
        inputArea.requestFocus();
        
        generatePrompt();
        updateFeedback("Type the text above! First character...");
        
        // Game timer
        new Thread(() -> {
            while (gameRunning) {
                long secondsLeft = 60 - Duration.between(gameStartTime, Instant.now()).getSeconds();
                
                if (secondsLeft <= 0) {
                    gameRunning = false;
                    endGame();
                    break;
                }
                
                SwingUtilities.invokeLater(() -> {
                    timerLabel.setText(secondsLeft + " seconds");
                    updateStats();
                });
                
                try { Thread.sleep(100); } 
                catch (InterruptedException e) { break; }
            }
        }).start();
    }

    private void generatePrompt() {
        Difficulty diff = Difficulty.values()[difficultyCombo.getSelectedIndex()];
        Random rand = new Random();
        StringBuilder sb = new StringBuilder("<html>");
        
        // Generate wrapped HTML text for better display
        int lineLength = 0;
        for (int i = 0; i < diff.length; i++) {
            String word = WORDS.get(rand.nextInt(WORDS.size()));
            sb.append(word).append(" ");
            lineLength += word.length();
            if (lineLength > 30) { // Wrap after ~30 chars
                sb.append("<br>");
                lineLength = 0;
            }
        }
        
        currentPrompt = sb.toString().replace("<br>", " ").replace("</html>", "").replace("<html>", "");
        promptLabel.setText(sb.append("</html>").toString());
        inputArea.setText("");
    }

    private void checkTyping() {
        if (!gameRunning) return;
        
        String typed = inputArea.getText();
        int correct = 0;
        StringBuilder feedback = new StringBuilder("<html>");
        
        for (int i = 0; i < typed.length(); i++) {
            char expected = i < currentPrompt.length() ? currentPrompt.charAt(i) : ' ';
            char actual = typed.charAt(i);
            
            if (actual == expected) {
                correct++;
                feedback.append("<span style='color:green'>").append(actual).append("</span>");
            } else {
                feedback.append("<span style='color:red'>").append(actual).append("</span>");
            }
        }
        
        // Show remaining characters
        if (typed.length() < currentPrompt.length()) {
            feedback.append("<span style='color:gray'>")
                  .append(currentPrompt.substring(typed.length()))
                  .append("</span>");
        }
        
        correctChars = correct;
        totalChars = typed.length();
        updateStats();
        updateFeedback(feedback.append("</html>").toString());
    }

    private void updateStats() {
        double minutes = Duration.between(gameStartTime, Instant.now()).toMillis() / 60000.0;
        Difficulty diff = Difficulty.values()[difficultyCombo.getSelectedIndex()];
        
        // Calculate base WPM
        int baseWpm = (int)((correctChars / 5.0) / (minutes > 0 ? minutes : 1));
        
        // Apply difficulty multiplier
        int adjustedWpm = (int)(baseWpm * diff.multiplier);
        
        double accuracy = totalChars > 0 ? (correctChars * 100.0 / totalChars) : 0;
        
        // Update UI
        wpmLabel.setText(adjustedWpm + " WPM");
        accuracyLabel.setText(String.format("%.1f%%", accuracy));
        wpmProgressBar.setValue(Math.min(adjustedWpm, 150));
        accuracyProgressBar.setValue((int)accuracy);
        
        // Record WPM every 5 seconds
        if (wpmHistory.isEmpty() || Duration.between(gameStartTime, Instant.now()).getSeconds() % 5 == 0) {
            wpmHistory.add(adjustedWpm);
        }
    }

    private void updateFeedback(String message) {
        if (message.startsWith("<html>")) {
            feedbackLabel.setText(message);
        } else {
            feedbackLabel.setText("<html><span style='color:" + 
                (darkMode ? "white" : "black") + "'>" + message + "</span></html>");
        }
    }

    private void endGame() {
        inputArea.setEnabled(false);
        
        double minutes = Duration.between(gameStartTime, Instant.now()).toMillis() / 60000.0;
        Difficulty diff = Difficulty.values()[difficultyCombo.getSelectedIndex()];
        int finalWpm = (int)((correctChars / 5.0 * diff.multiplier) / minutes);
        double finalAccuracy = totalChars > 0 ? (correctChars * 100.0 / totalChars) : 0;
        
        // Save high score
        highScores.update(diff, finalWpm);
        highScoreLabel.setText(highScores.toString());
        
        // Show results with performance comment
        String performanceComment;
        if (finalWpm >= 80) performanceComment = "Expert Typist! 🚀";
        else if (finalWpm >= 50) performanceComment = "Great Job! 👍";
        else if (finalWpm >= 30) performanceComment = "Good Start! 🙂";
        else performanceComment = "Keep Practicing! 💪";
        
        JOptionPane.showMessageDialog(this,
            "<html><div style='font-size:14pt;text-align:center'>" +
            "<b>Test Complete!</b><br><br>" +
            "Final WPM: <font color='blue'>" + finalWpm + "</font><br>" +
            "Accuracy: <font color='blue'>" + String.format("%.1f%%", finalAccuracy) + "</font><br><br>" +
            performanceComment +
            "</div></html>",
            "Results",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void restartGame() {
        gameRunning = false;
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        startGame();
    }

    class HighScoreManager {
        private Map<Difficulty, Integer> scores = new EnumMap<>(Difficulty.class);
        
        HighScoreManager() {
            for (Difficulty d : Difficulty.values()) {
                scores.put(d, 0);
            }
        }
        
        void update(Difficulty d, int score) {
            if (score > scores.get(d)) {
                scores.put(d, score);
            }
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("<html>");
            for (Difficulty d : Difficulty.values()) {
                sb.append(d.name).append(": <b>").append(scores.get(d)).append(" WPM</b><br>");
            }
            return sb.append("</html>").toString();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Typegame();
        });
    }
}
