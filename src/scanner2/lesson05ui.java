import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.ArrayList;

public class lesson05ui {
    private int targetNumber;
    private int attempts;
    private final int MAX_ATTEMPTS = 5;

    private JFrame frame;
    private JTextField inputField;
    private JLabel feedbackLabel;
    private JLabel attemptsLabel;
    private JTextArea historyArea;
    private JButton guessButton;

    private ArrayList<Integer> guesses;

    public lesson05ui() {
        chooseGameMode();
        attempts = 0;
        guesses = new ArrayList<>();

        frame = new JFrame("🎯 Zahlenratespiel");
        frame.setSize(450, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // TITLE
        JLabel title = new JLabel("Rate die Zahl (1-100)", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        frame.add(title, BorderLayout.NORTH);

        // CENTER PANEL
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        inputField = new JTextField();
        inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));
        centerPanel.add(inputField);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        guessButton = new JButton("Raten");
        guessButton.setFont(new Font("Arial", Font.BOLD, 14));
        guessButton.setFocusPainted(false);
        centerPanel.add(guessButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        feedbackLabel = new JLabel("Du hast 5 Versuche!", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(feedbackLabel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        attemptsLabel = new JLabel("Versuche: 0 / 5", SwingConstants.CENTER);
        attemptsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        centerPanel.add(attemptsLabel);

        frame.add(centerPanel, BorderLayout.CENTER);

        // HISTORY
        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        historyArea.setBorder(BorderFactory.createTitledBorder("📜 Deine Versuche"));
        frame.add(new JScrollPane(historyArea), BorderLayout.SOUTH);

        // ACTIONS
        guessButton.addActionListener(e -> checkGuess());
        inputField.addActionListener(e -> checkGuess());

        frame.setLocationRelativeTo(null); // center window
        frame.setVisible(true);
    }

    private void chooseGameMode() {
        String[] options = {"🎲 Zufallszahl", "👥 Freund-Modus"};
        int choice = JOptionPane.showOptionDialog(null,
                "Wähle einen Spielmodus:",
                "Spielmodus",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 1) {
            boolean valid = false;
            while (!valid) {
                try {
                    JPasswordField field = new JPasswordField();
                    int ok = JOptionPane.showConfirmDialog(null, field,
                            "Freund: Gib eine geheime Zahl (1-100) ein:",
                            JOptionPane.OK_CANCEL_OPTION);

                    if (ok == JOptionPane.OK_OPTION) {
                        String input = new String(field.getPassword());
                        targetNumber = Integer.parseInt(input);
                        if (targetNumber >= 1 && targetNumber <= 100) {
                            valid = true;
                        }
                    }
                } catch (Exception e) {
                    // retry
                }
            }
        } else {
            targetNumber = new Random().nextInt(100) + 1;
        }
    }

    private void checkGuess() {
        if (attempts >= MAX_ATTEMPTS) return;

        try {
            int guess = Integer.parseInt(inputField.getText());

            if (guess < 1 || guess > 100) {
                feedbackLabel.setText("⚠ Bitte Zahl zwischen 1 und 100!");
                return;
            }

            attempts++;
            guesses.add(guess);

            attemptsLabel.setText("Versuche: " + attempts + " / 5");
            updateHistory();

            if (guess < targetNumber) {
                feedbackLabel.setText("⬆ Größer als " + guess);
            } else if (guess > targetNumber) {
                feedbackLabel.setText("⬇ Kleiner als " + guess);
            } else {
                feedbackLabel.setText("🎉 Richtig geraten!");
                guessButton.setEnabled(false);
                endGame(true);
                return;
            }

            if (attempts >= MAX_ATTEMPTS) {
                feedbackLabel.setText("❌ Verloren! Zahl war: " + targetNumber);
                guessButton.setEnabled(false);
                endGame(false);
            }

            inputField.setText("");
        } catch (NumberFormatException ex) {
            feedbackLabel.setText("⚠ Bitte gültige Zahl eingeben!");
        }
    }

    private void updateHistory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < guesses.size(); i++) {
            sb.append("#").append(i + 1).append(" → ").append(guesses.get(i)).append("\n");
        }
        historyArea.setText(sb.toString());
    }

    private void endGame(boolean won) {
        int option = JOptionPane.showConfirmDialog(frame,
                (won ? "🎉 Gewonnen!" : "❌ Verloren! Zahl war: " + targetNumber) + "\nNochmal spielen?",
                "Spiel beendet",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }

    private void resetGame() {
        chooseGameMode();
        attempts = 0;
        guesses.clear();
        guessButton.setEnabled(true);
        attemptsLabel.setText("Versuche: 0 / 5");
        feedbackLabel.setText("Neues Spiel! Viel Glück!");
        historyArea.setText("");
        inputField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(lesson05ui::new);
    }
}