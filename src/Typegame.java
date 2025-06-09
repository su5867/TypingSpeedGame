import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.time.LocalDateTime;
import javax.swing.JOptionPane;

public class Typegame extends JFrame {
    // UI Components
    private JLabel promptLabel, timerLabel, wpmLabel, accuracyLabel, highScoreLabel, feedbackLabel;
    private JTextArea inputArea;
    private JButton startButton, restartButton, exitButton, themeButton;
    private JComboBox<String> difficultyCombo;
    private JPanel statsPanel, controlPanel;
    private JScrollPane promptScroll;
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
    private Color buttonPressedColor = new Color(180, 180, 180);
    
    // Difficulty enum
    private enum Difficulty {
        EASY("Easy", 30, 1.0),
        MEDIUM("Medium", 50, 1.2),
        HARD("Hard", 80, 1.5);
        
        final String name;
        final int length;
        final double multiplier;
        
        Difficulty(String name, int length, double multiplier) {
            this.name = name;
            this.length = length;
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
        setTitle("Typing Speed Master");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeComponents() {
        promptLabel = new JLabel("", SwingConstants.CENTER);
        inputArea = new JTextArea();
        timerLabel = new JLabel("60 seconds", SwingConstants.CENTER);
        wpmLabel = new JLabel("0 WPM", SwingConstants.CENTER);
        accuracyLabel = new JLabel("0%", SwingConstants.CENTER);
        highScoreLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel = new JLabel("Press Start to begin", SwingConstants.CENTER);
        
        // Buttons with text only
        startButton = new JButton("START");
        restartButton = new JButton("RESTART");
        exitButton = new JButton("EXIT");
        themeButton = new JButton("DARK MODE");
        
        difficultyCombo = new JComboBox<>();
        wpmProgressBar = new JProgressBar(0, 150);
        accuracyProgressBar = new JProgressBar(0, 100);
        
        // Add key listener for Enter key
        inputArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && gameRunning) {
                    checkForCompletion();
                    e.consume(); // Prevent newline from being added
                }
            }
        });
    }

    private void checkForCompletion() {
        String typedText = inputArea.getText().replace("\n", ""); // Remove any newlines
        String promptText = currentPrompt.replace("\n", "").replace("\r", "");
        
        if (typedText.equals(promptText)) {
            // User has completed the text
            gameRunning = false;
            endGame();
        }
    }

    private void setTheme(boolean dark) {
        darkMode = dark;
        if (darkMode) {
            // Dark theme colors
            bgColor = new Color(36, 36, 36);
            textColor = new Color(240, 240, 240);
            primaryColor = new Color(70, 130, 180);   // Steel Blue
            accentColor = new Color(255, 165, 0);     // Orange
            correctColor = new Color(50, 205, 50);    // Lime Green
            errorColor = new Color(255, 69, 0);       // Red-Orange
            themeButton.setText("LIGHT MODE");
        } else {
            // Light theme colors
            bgColor = new Color(245, 245, 245);
            textColor = new Color(40, 40, 40);
            primaryColor = new Color(30, 144, 255);   // Dodger Blue
            accentColor = new Color(255, 140, 0);     // Dark Orange
            correctColor = new Color(34, 139, 34);    // Forest Green
            errorColor = new Color(220, 20, 60);      // Crimson
            themeButton.setText("DARK MODE");
        }
        applyTheme();
    }

    private void applyTheme() {
        getContentPane().setBackground(bgColor);
        
        // Set component colors
        promptLabel.setForeground(textColor);
        inputArea.setBackground(darkMode ? new Color(50, 50, 50) : Color.WHITE);
        inputArea.setForeground(textColor);
        inputArea.setCaretColor(accentColor);
        inputArea.setSelectionColor(primaryColor);
        inputArea.setSelectedTextColor(Color.WHITE);
        
        timerLabel.setForeground(textColor);
        wpmLabel.setForeground(textColor);
        accuracyLabel.setForeground(textColor);
        highScoreLabel.setForeground(textColor);
        feedbackLabel.setForeground(accentColor);
        
        // Progress bars
        styleProgressBar(wpmProgressBar, primaryColor);
        styleProgressBar(accuracyProgressBar, accentColor);
        
        // Buttons - updated with more vibrant colors
        styleButton(startButton, new Color(76, 175, 80)); // Green
        styleButton(restartButton, new Color(33, 150, 243)); // Blue
        styleButton(exitButton, new Color(244, 67, 54)); // Red
        styleButton(themeButton, darkMode ? new Color(66, 66, 66) : new Color(189, 189, 189));
    }

    private void styleProgressBar(JProgressBar bar, Color color) {
        bar.setForeground(color);
        bar.setBackground(darkMode ? new Color(70, 70, 70) : new Color(220, 220, 220));
        bar.setBorder(BorderFactory.createLineBorder(color.darker(), 1));
        bar.setStringPainted(true);
        bar.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    private void styleButton(JButton button, Color bg) {
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 2),
            BorderFactory.createEmptyBorder(12, 25, 12, 25)
        ));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bg.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });
        
        // Click effect
        button.getModel().addChangeListener(e -> {
            ButtonModel model = (ButtonModel) e.getSource();
            if (model.isPressed()) {
                button.setBackground(bg.darker());
            } else if (model.isRollover()) {
                button.setBackground(bg.brighter());
            } else {
                button.setBackground(bg);
            }
        });
    }

    private void setupUI() {
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(bgColor);
        
        // Control Panel (Top)
        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        controlPanel.setBackground(bgColor);
        
        difficultyCombo.setModel(new DefaultComboBoxModel<>(
            Arrays.stream(Difficulty.values())
                .map(d -> d.name)
                .toArray(String[]::new)
        ));
        difficultyCombo.setSelectedIndex(1); // Default to Medium
        difficultyCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        difficultyCombo.setBackground(darkMode ? new Color(70, 70, 70) : Color.WHITE);
        difficultyCombo.setForeground(textColor);
        
        themeButton.addActionListener(e -> setTheme(!darkMode));
        
        controlPanel.add(new JLabel("Difficulty:"));
        controlPanel.add(difficultyCombo);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(themeButton);
        add(controlPanel, BorderLayout.NORTH);
        
        // Prompt Area (Center Top)
        promptLabel.setFont(new Font("Consolas", Font.BOLD, 22));
        promptLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        promptScroll = new JScrollPane(promptLabel);
        promptScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(primaryColor, 2), 
            "TYPE THIS TEXT",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 12),
            primaryColor
        ));
        promptScroll.setBackground(bgColor);
        add(promptScroll, BorderLayout.CENTER);
        
        // Input Area (Center Bottom)
        inputArea.setFont(new Font("Consolas", Font.PLAIN, 20));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setMargin(new Insets(20, 20, 20, 20));
        inputArea.setEnabled(false);
        inputArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { checkTyping(); }
            @Override
            public void removeUpdate(DocumentEvent e) { checkTyping(); }
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(accentColor, 2), 
            "YOUR TYPING",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Segoe UI", Font.BOLD, 12),
            accentColor
        ));
        add(inputScroll, BorderLayout.SOUTH);
        
        // Stats Panel (Right)
        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        statsPanel.setBackground(bgColor);
        
        // Timer
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statsPanel.add(createStatPanel("⏱ TIME REMAINING", timerLabel));
        
        // WPM with progress bar
        wpmLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        wpmProgressBar.setString("WPM");
        statsPanel.add(createStatPanel("🚀 TYPING SPEED (WPM)", wpmLabel, wpmProgressBar));
        
        // Accuracy with progress bar
        accuracyLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        accuracyProgressBar.setString("ACCURACY");
        statsPanel.add(createStatPanel("🎯 ACCURACY", accuracyLabel, accuracyProgressBar));
        
        // Feedback
        feedbackLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statsPanel.add(createStatPanel("💡 LIVE FEEDBACK", feedbackLabel));
        
        // High Scores
        highScoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statsPanel.add(createStatPanel("🏆 HIGH SCORES", highScoreLabel));
        
        add(statsPanel, BorderLayout.EAST);
        
        // Button Panel (Bottom)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(bgColor);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        // Center buttons horizontally
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        centerPanel.setBackground(bgColor);
        
        startButton.addActionListener(e -> startGame());
        restartButton.addActionListener(e -> restartGame());
        exitButton.addActionListener(e -> System.exit(0));
        
        centerPanel.add(startButton);
        centerPanel.add(restartButton);
        centerPanel.add(exitButton);
        
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(centerPanel);
        buttonPanel.add(Box.createHorizontalGlue());
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize values
        highScoreLabel.setText(highScores.toString());
    }

    private JPanel createStatPanel(String title, JComponent... components) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(accentColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        for (JComponent comp : components) {
            comp.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(comp);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
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
        StringBuilder sb = new StringBuilder("<html><div style='text-align:center'>");
        
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
        
        // Store clean version without HTML tags for comparison
        currentPrompt = sb.toString()
            .replace("<br>", " ")
            .replaceAll("<[^>]+>", "") // Remove all HTML tags
            .trim();
        
        // Set the formatted version for display
        promptLabel.setText(sb.append("</div></html>").toString());
        inputArea.setText("");
    }

    private void checkTyping() {
        if (!gameRunning) return;
        
        String typed = inputArea.getText();
        int correct = 0;
        StringBuilder feedback = new StringBuilder("<html><div style='text-align:center'>");
        
        for (int i = 0; i < typed.length(); i++) {
            char expected = i < currentPrompt.length() ? currentPrompt.charAt(i) : ' ';
            char actual = typed.charAt(i);
            
            if (actual == expected) {
                correct++;
                feedback.append("<span style='color:").append(darkMode ? "#32CD32" : "#228B22").append("'>").append(actual).append("</span>");
            } else {
                feedback.append("<span style='color:").append(darkMode ? "#FF4500" : "#DC143C").append("'>").append(actual).append("</span>");
            }
        }
        
        // Show remaining characters
        if (typed.length() < currentPrompt.length()) {
            feedback.append("<span style='color:").append(darkMode ? "#757575" : "#ADB5BD").append("'>")
                  .append(currentPrompt.substring(typed.length()))
                  .append("</span>");
        }
        
        correctChars = correct;
        totalChars = typed.length();
        updateStats();
        updateFeedback(feedback.append("</div></html>").toString());
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
        wpmLabel.setText("<html><b>" + adjustedWpm + "</b> WPM</html>");
        accuracyLabel.setText(String.format("<html><b>%.1f%%</b></html>", accuracy));
        wpmProgressBar.setValue(Math.min(adjustedWpm, 150));
        accuracyProgressBar.setValue((int)accuracy);
        
        // Record WPM every 5 seconds
        if (wpmHistory.isEmpty() || Duration.between(gameStartTime, Instant.now()).getSeconds() % 5 == 0) {
            wpmHistory.add(adjustedWpm);
        }
    }

    private void updateFeedback(String message) {
        feedbackLabel.setText(message);
    }

    private void endGame() {
        inputArea.setEnabled(false);
        
        // Calculate time taken (either full 60s or actual time if completed early)
        long secondsTaken = Duration.between(gameStartTime, Instant.now()).getSeconds();
        boolean completedEarly = secondsTaken < 60;
        
        double minutes = secondsTaken / 60.0;
        Difficulty diff = Difficulty.values()[difficultyCombo.getSelectedIndex()];
        int finalWpm = (int)((correctChars / 5.0 * diff.multiplier) / (minutes > 0 ? minutes : 1));
        double finalAccuracy = totalChars > 0 ? (correctChars * 100.0 / totalChars) : 0;
        
        // Save high score
        highScores.update(diff, finalWpm);
        highScoreLabel.setText(highScores.toString());
        
        // Prompt for username and save to database
        String username = JOptionPane.showInputDialog(this, 
            "Enter your name to save your score:", 
            "Save Score", 
            JOptionPane.PLAIN_MESSAGE);
        
        if (username != null && !username.trim().isEmpty()) {
            // DatabaseManager.saveScore(username.trim(), finalWpm, finalAccuracy, diff);
        }
        
        // Show results with performance comment
        String performanceComment = getPerformanceComment(finalWpm);
        String timeInfo = completedEarly ? 
            String.format("Completed in %d seconds!", secondsTaken) : 
            "Time's up!";
        
        JOptionPane.showMessageDialog(this,
            "<html><div style='font-size:14pt;text-align:center;width:300px'>" +
            "<h2 style='color:" + primaryColor.getRGB() + "'>Test Complete!</h2>" +
            "<p style='margin-top:10px;color:" + accentColor.getRGB() + "'>" + timeInfo + "</p>" +
            "<p style='margin-top:10px'>" +
            "Final WPM: <b style='color:" + primaryColor.getRGB() + "'>" + finalWpm + "</b><br>" +
            "Accuracy: <b style='color:" + primaryColor.getRGB() + "'>" + String.format("%.1f%%", finalAccuracy) + "</b>" +
            "</p><p style='margin-top:15px;font-size:16pt;color:" + accentColor.getRGB() + "'>" +
            performanceComment +
            "</p></div></html>",
            "Results",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private String getPerformanceComment(int wpm) {
        if (wpm >= 80) return "Expert Typist! 🚀";
        if (wpm >= 50) return "Great Job! 👍";
        if (wpm >= 30) return "Good Start! 🙂";
        return "Keep Practicing! 💪";
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
            StringBuilder sb = new StringBuilder("<html><div style='text-align:center'>");
            for (Difficulty d : Difficulty.values()) {
                sb.append("<b>").append(d.name).append(":</b> ")
                 .append("<span style='color:").append(primaryColor.getRGB()).append("'>")
                 .append(scores.get(d)).append(" WPM</span><br>");
            }
            return sb.append("</div></html>").toString();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new Typegame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}