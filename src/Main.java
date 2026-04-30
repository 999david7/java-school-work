import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main extends JFrame {

    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir"));
    private static final Path BUILD_DIR = PROJECT_ROOT.resolve("out");

    // --- THEME CONFIGURATION ---
    public static Color BG_COLOR = new Color(18, 18, 20);
    public static Color PANEL_COLOR = new Color(32, 32, 36);
    public static Color FG_COLOR = new Color(210, 210, 210);
    public static Color ACCENT_COLOR = new Color(0, 255, 150); // Neon Mint
    public static Color ERROR_COLOR = new Color(255, 45, 85);

    private JComboBox<JavaFile> fileCombo;
    private JButton runBtn, refreshBtn, settingsBtn;
    private JTabbedPane tabs;
    private JLabel statusLabel;

    public Main() {
        super("Java-Hub Terminal Pro");
        applyGlobalTheme();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 800);
        setLocationRelativeTo(null);
        setupUI();
        refreshFiles();
    }

    private void applyGlobalTheme() {
        UIManager.put("nimbusBase", BG_COLOR);
        UIManager.put("control", PANEL_COLOR);
        UIManager.put("text", FG_COLOR);
        UIManager.put("nimbusSelectionBackground", ACCENT_COLOR);
        UIManager.put("nimbusSelectedText", BG_COLOR);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    private void setupUI() {
        getContentPane().setBackground(BG_COLOR);
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        topPanel.setBackground(PANEL_COLOR);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 50, 55)));

        fileCombo = new JComboBox<>();
        fileCombo.setPreferredSize(new Dimension(300, 30));

        refreshBtn = createStyledButton("REFRESH");
        runBtn = createStyledButton("EXECUTE");
        runBtn.setForeground(ACCENT_COLOR);
        settingsBtn = createStyledButton("THEME");

        statusLabel = new JLabel("SYSTEM READY");
        statusLabel.setForeground(Color.GRAY);
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

        topPanel.add(fileCombo);
        topPanel.add(refreshBtn);
        topPanel.add(runBtn);
        topPanel.add(settingsBtn);
        topPanel.add(statusLabel);

        tabs = new JTabbedPane();
        tabs.setBackground(PANEL_COLOR);

        add(topPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> refreshFiles());
        runBtn.addActionListener(e -> startCompilationAndRun());
        settingsBtn.addActionListener(e -> openSettings());
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(45, 45, 50));
        return btn;
    }

    private void refreshFiles() {
        fileCombo.removeAllItems();
        try (Stream<Path> stream = Files.walk(PROJECT_ROOT)) {
            stream.filter(p -> p.toString().endsWith(".java"))
                    .map(JavaFile::new)
                    .forEach(fileCombo::addItem);
        } catch (IOException ignored) {}
    }

    private void startCompilationAndRun() {
        JavaFile javaFile = (JavaFile) fileCombo.getSelectedItem();
        if (javaFile == null) return;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() throws Exception {
                Files.createDirectories(BUILD_DIR);
                List<String> sources;
                try (Stream<Path> stream = Files.walk(PROJECT_ROOT)) {
                    sources = stream.filter(p -> p.toString().endsWith(".java")).map(Path::toString).collect(Collectors.toList());
                }
                List<String> cmd = new ArrayList<>(List.of("javac", "-d", BUILD_DIR.toString()));
                cmd.addAll(sources);
                return new ProcessBuilder(cmd).directory(PROJECT_ROOT.toFile()).start().waitFor() == 0;
            }
            @Override protected void done() {
                try { if (get()) executeProgram(javaFile); } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    private void executeProgram(JavaFile javaFile) {
        try {
            Process proc = new ProcessBuilder("java", "-cp", BUILD_DIR.toString(), javaFile.className)
                    .directory(PROJECT_ROOT.toFile()).start();
            TerminalPanel term = new TerminalPanel(proc, javaFile.className, tabs);
            tabs.addTab(javaFile.className, term);
            tabs.setSelectedComponent(term);
        } catch (IOException ignored) {}
    }

    private void openSettings() {
        Color c = JColorChooser.showDialog(this, "Select Accent Color", ACCENT_COLOR);
        if (c != null) {
            ACCENT_COLOR = c;
            SwingUtilities.updateComponentTreeUI(this);
        }
    }

    // --- ASCII LIBRARY ENGINE ---
    static class AsciiLibrary {
        private static final Map<Character, String[]> font = new HashMap<>();

        static {
            font.put('R', new String[]{"██████  ", "██   ██ ", "██████  ", "██   ██ ", "██   ██ "});
            font.put('U', new String[]{"██   ██ ", "██   ██ ", "██   ██ ", "██   ██ ", " █████  "});
            font.put('N', new String[]{"███    ██ ", "████   ██ ", "██ ██  ██ ", "██  ██ ██ ", "██   ████ "});
            font.put('I', new String[]{" █████  ", "   ██   ", "   ██   ", "   ██   ", " █████  "});
            font.put('G', new String[]{" ██████  ", "██       ", "██   ███ ", "██    ██ ", " ██████  "});
            font.put('!', new String[]{" ██ ", " ██ ", " ██ ", "    ", " ██ "});
        }

        public static String generate(String text) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                for (char c : text.toUpperCase().toCharArray()) {
                    if (font.containsKey(c)) sb.append(font.get(c)[i]);
                    else sb.append("      ");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    private static class TerminalPanel extends JPanel {
        private final JTextPane area = new JTextPane();
        private final JTextField input = new JTextField();
        private final Process proc;

        public TerminalPanel(Process proc, String name, JTabbedPane tabs) {
            this.proc = proc;
            setLayout(new BorderLayout());
            area.setEditable(false);
            area.setBackground(BG_COLOR);
            area.setFont(new Font("Monospaced", Font.PLAIN, 14));

            // Print ASCII Banner
            append(AsciiLibrary.generate("RUNNING!"), ACCENT_COLOR);
            append(">> INITIALIZING PROCESS: " + name + "\n\n", FG_COLOR);

            input.setBackground(PANEL_COLOR);
            input.setForeground(ACCENT_COLOR);
            input.setCaretColor(Color.WHITE);
            input.setBorder(new EmptyBorder(10, 10, 10, 10));

            add(new JScrollPane(area), BorderLayout.CENTER);
            add(input, BorderLayout.SOUTH);

            new Thread(() -> read(proc.getInputStream(), FG_COLOR)).start();
            new Thread(() -> read(proc.getErrorStream(), ERROR_COLOR)).start();
            input.addActionListener(e -> {
                try {
                    proc.getOutputStream().write((input.getText() + "\n").getBytes());
                    proc.getOutputStream().flush();
                    append("> " + input.getText() + "\n", ACCENT_COLOR);
                    input.setText("");
                } catch (IOException ignored) {}
            });
        }

        private void read(InputStream s, Color c) {
            try (Scanner sc = new Scanner(s)) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    SwingUtilities.invokeLater(() -> append(line + "\n", c));
                }
            }
        }

        private void append(String s, Color c) {
            StyleContext sc = StyleContext.getDefaultStyleContext();
            AttributeSet as = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
            try { area.getDocument().insertString(area.getDocument().getLength(), s, as); } catch (Exception ignored) {}
        }
    }

    private static class JavaFile {
        final String className;
        JavaFile(Path p) {
            String name = p.getFileName().toString().replace(".java", "");
            String pkg = "";
            try {
                pkg = Files.lines(p).filter(l -> l.trim().startsWith("package "))
                        .map(l -> l.replace("package", "").replace(";", "").trim() + ".")
                        .findFirst().orElse("");
            } catch (IOException ignored) {}
            this.className = pkg + name;
        }
        @Override public String toString() { return className; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}