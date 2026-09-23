package firstclass.caesercode2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Caeser extends JFrame {

    private JTextArea inputText;
    private JTextArea outputText;
    private JSlider shiftSlider;
    private JLabel shiftLabel;

    public Caeser() {
        setTitle("Caesar Cipher • Advanced UI");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // ===== Main Panel =====
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(30, 30, 30));
        setContentPane(mainPanel);

        // ===== Title =====
        JLabel title = new JLabel("Caesar Cipher");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(title, BorderLayout.NORTH);

        // ===== Center Panel =====
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.setOpaque(false);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // ===== Input Area =====
        inputText = createTextArea("Enter text here...");
        centerPanel.add(wrapPanel("Input", inputText));

        // ===== Output Area =====
        outputText = createTextArea("");
        outputText.setEditable(false);
        outputText.setFont(new Font("Consolas", Font.PLAIN, 14));
        centerPanel.add(wrapPanel("Output", outputText));

        // ===== Bottom Controls =====
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Shift slider
        shiftSlider = new JSlider(0, 25, 3);
        shiftSlider.setOpaque(false);
        shiftSlider.setForeground(Color.WHITE);

        shiftLabel = new JLabel("Shift: 3");
        shiftLabel.setForeground(Color.WHITE);

        shiftSlider.addChangeListener(e ->
                shiftLabel.setText("Shift: " + shiftSlider.getValue())
        );

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setOpaque(false);
        sliderPanel.add(shiftLabel, BorderLayout.WEST);
        sliderPanel.add(shiftSlider, BorderLayout.CENTER);

        bottomPanel.add(sliderPanel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(false);

        JButton encryptBtn = createButton("Encrypt");
        JButton decryptBtn = createButton("Decrypt");
        JButton clearBtn = createButton("Clear");

        encryptBtn.addActionListener(e ->
                outputText.setText(encrypt(inputText.getText(), shiftSlider.getValue()))
        );

        decryptBtn.addActionListener(e ->
                outputText.setText(encrypt(inputText.getText(), 26 - shiftSlider.getValue()))
        );

        clearBtn.addActionListener(e -> {
            inputText.setText("");
            outputText.setText("");
        });

        buttonPanel.add(encryptBtn);
        buttonPanel.add(decryptBtn);
        buttonPanel.add(clearBtn);

        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    // ===== UI Helpers =====
    private JTextArea createTextArea(String placeholder) {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBackground(new Color(45, 45, 45));
        area.setForeground(Color.WHITE);
        area.setCaretColor(Color.WHITE);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        area.setText(placeholder);
        return area;
    }

    private JPanel wrapPanel(String title, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setForeground(Color.LIGHT_GRAY);
        panel.add(label, BorderLayout.NORTH);

        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        return btn;
    }

    // ===== Caesar Cipher Logic =====
    public static String encrypt(String text, int shift) {
        StringBuilder result = new StringBuilder();
        shift %= 26;

        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c)) {
                result.append((char) ((c - 'A' + shift) % 26 + 'A'));
            } else if (Character.isLowerCase(c)) {
                result.append((char) ((c - 'a' + shift) % 26 + 'a'));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // ===== Main =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Caeser().setVisible(true));
    }
}