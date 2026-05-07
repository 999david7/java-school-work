import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.tree.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

public class Main extends JFrame {

    private static final Path PROJECT_ROOT = Paths.get(System.getProperty("user.dir"));
    private static final Path BUILD_DIR    = PROJECT_ROOT.resolve("out");

    // ══════════════════════════════════════════════════════════════════════════
    //  THEME ENGINE
    // ══════════════════════════════════════════════════════════════════════════
    static class Theme {
        final String name;
        final Color bg, panel, fg, accent, error, border, subtle;
        Theme(String n, Color bg, Color pa, Color fg, Color ac, Color er, Color bo, Color su) {
            name=n; this.bg=bg; panel=pa; this.fg=fg; accent=ac; error=er; border=bo; subtle=su;
        }
        static final Theme[] PRESETS = {
                new Theme("Neon Mint",
                        new Color(18,18,20),    new Color(28,28,32),    new Color(210,210,215),
                        new Color(0,230,130),   new Color(255,60,90),   new Color(48,48,54),
                        new Color(100,100,110)),
                new Theme("Cyber Blue",
                        new Color(10,13,24),    new Color(16,22,42),    new Color(195,210,255),
                        new Color(40,170,255),  new Color(255,60,100),  new Color(28,38,72),
                        new Color(80,100,160)),
                new Theme("Solar Orange",
                        new Color(20,14,8),     new Color(32,22,12),    new Color(240,220,190),
                        new Color(255,145,30),  new Color(220,55,55),   new Color(58,38,18),
                        new Color(140,100,60)),
                new Theme("Orchid",
                        new Color(15,10,22),    new Color(26,16,38),    new Color(220,210,235),
                        new Color(175,85,255),  new Color(255,65,90),   new Color(48,28,68),
                        new Color(110,80,150)),
                new Theme("Arctic",
                        new Color(235,240,250), new Color(248,250,255), new Color(28,38,58),
                        new Color(0,115,195),   new Color(195,30,50),   new Color(195,205,225),
                        new Color(100,120,160)),
        };
    }

    // Live colour slots — read these everywhere, never hardcode colours
    static Color BG     = Theme.PRESETS[0].bg;
    static Color PANEL  = Theme.PRESETS[0].panel;
    static Color FG     = Theme.PRESETS[0].fg;
    static Color ACCENT = Theme.PRESETS[0].accent;
    static Color ERR    = Theme.PRESETS[0].error;
    static Color BORDER = Theme.PRESETS[0].border;
    static Color SUBTLE = Theme.PRESETS[0].subtle;

    private final List<Runnable> themeHooks = new ArrayList<>();

    // ══════════════════════════════════════════════════════════════════════════
    //  VECTOR ICON LIBRARY  — pure Graphics2D, zero external deps
    // ══════════════════════════════════════════════════════════════════════════
    enum Ico { REFRESH, PLAY, SETTINGS, FOLDER_C, FOLDER_O, FILE_JAVA, CLOSE, CLEAR, TERMINAL }

    static class VIcon implements Icon {
        final Ico type; final int sz; Color col;
        VIcon(Ico t, int sz, Color c) { type=t; this.sz=sz; col=c; }
        void setColor(Color c) { col=c; }
        @Override public int getIconWidth()  { return sz; }
        @Override public int getIconHeight() { return sz; }
        @Override public void paintIcon(Component cmp, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            g2.setColor(col);
            float s = sz / 16f;
            BasicStroke round = new BasicStroke(1.6f*s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            g2.setStroke(round);
            switch (type) {
                case REFRESH -> {
                    g2.draw(new Arc2D.Float(2*s,2*s,12*s,12*s,50,265,Arc2D.OPEN));
                    g2.drawLine(r(11*s),r(1.5f*s), r(14*s),r(3.5f*s));
                    g2.drawLine(r(14*s),r(3.5f*s), r(13.5f*s),r(7*s));
                }
                case PLAY -> {
                    int[] px={r(3*s),r(3*s),r(14*s)}, py={r(2*s),r(14*s),r(8*s)};
                    g2.fillPolygon(px,py,3);
                }
                case SETTINGS -> {
                    g2.draw(new Ellipse2D.Float(5.5f*s,5.5f*s,5*s,5*s));
                    for (int i=0;i<8;i++) {
                        double a=Math.PI*2*i/8;
                        g2.drawLine(r((float)(8*s+Math.cos(a)*5.5*s)),r((float)(8*s+Math.sin(a)*5.5*s)),
                                r((float)(8*s+Math.cos(a)*7.5*s)),r((float)(8*s+Math.sin(a)*7.5*s)));
                    }
                }
                case FOLDER_C -> {
                    g2.fillRoundRect(r(1*s),r(5*s),r(14*s),r(9*s),r(2*s),r(2*s));
                    g2.setColor(blend(col,Color.BLACK,0.3f));
                    g2.fillRoundRect(r(1*s),r(3.5f*s),r(6*s),r(3*s),r(2*s),r(2*s));
                }
                case FOLDER_O -> {
                    Path2D.Float p=new Path2D.Float();
                    p.moveTo(1*s,6*s);p.lineTo(1*s,14*s);p.lineTo(15*s,14*s);
                    p.lineTo(15*s,8*s);p.lineTo(8*s,8*s);p.lineTo(6*s,6*s);p.closePath();
                    g2.fill(p);
                    g2.setColor(blend(col,Color.BLACK,0.3f));
                    g2.fillRoundRect(r(1*s),r(3.5f*s),r(5*s),r(3*s),r(1.5f*s),r(1.5f*s));
                }
                case FILE_JAVA -> {
                    Path2D.Float p=new Path2D.Float();
                    p.moveTo(2*s,1*s);p.lineTo(10*s,1*s);p.lineTo(14*s,5*s);
                    p.lineTo(14*s,15*s);p.lineTo(2*s,15*s);p.closePath();
                    g2.fill(p);
                    g2.setColor(blend(col,Color.BLACK,0.35f));
                    Path2D.Float fold=new Path2D.Float();
                    fold.moveTo(10*s,1*s);fold.lineTo(10*s,5*s);fold.lineTo(14*s,5*s);
                    g2.fill(fold);
                    g2.setColor(BG);
                    g2.setStroke(new BasicStroke(1.4f*s,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                    g2.draw(new Arc2D.Float(4*s,9*s,5*s,4*s,180,180,Arc2D.OPEN));
                    g2.drawLine(r(9*s),r(7*s),r(9*s),r(11*s));
                }
                case CLOSE -> {
                    g2.drawLine(r(3*s),r(3*s),r(13*s),r(13*s));
                    g2.drawLine(r(13*s),r(3*s),r(3*s),r(13*s));
                }
                case CLEAR -> {
                    g2.drawRect(r(3*s),r(5*s),r(10*s),r(8*s));
                    g2.drawLine(r(1*s),r(5*s),r(15*s),r(5*s));
                    g2.drawLine(r(6*s),r(3*s),r(10*s),r(3*s));
                    g2.drawLine(r(6*s),r(3*s),r(6*s),r(5*s));
                    g2.drawLine(r(10*s),r(3*s),r(10*s),r(5*s));
                }
                case TERMINAL -> {
                    g2.drawRoundRect(r(1*s),r(2*s),r(14*s),r(10*s),r(2*s),r(2*s));
                    g2.drawLine(r(6*s),r(12*s),r(5*s),r(15*s));
                    g2.drawLine(r(10*s),r(12*s),r(11*s),r(15*s));
                    g2.drawLine(r(3.5f*s),r(15*s),r(12.5f*s),r(15*s));
                    g2.setStroke(new BasicStroke(1.5f*s,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
                    g2.drawLine(r(3*s),r(5*s),r(6*s),r(7*s));
                    g2.drawLine(r(6*s),r(7*s),r(3*s),r(9*s));
                    g2.drawLine(r(8*s),r(9*s),r(12*s),r(9*s));
                }
            }
            g2.dispose();
        }
        private static int r(float v) { return Math.round(v); }
        static Color blend(Color a, Color b, float t) {
            return new Color(
                    clamp((int)(a.getRed()*(1-t)+b.getRed()*t)),
                    clamp((int)(a.getGreen()*(1-t)+b.getGreen()*t)),
                    clamp((int)(a.getBlue()*(1-t)+b.getBlue()*t)));
        }
        private static int clamp(int v) { return Math.max(0,Math.min(255,v)); }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  THEMED BUTTON  — fully custom painted, L&F-proof
    // ══════════════════════════════════════════════════════════════════════════
    static class TBtn extends JButton {
        private final VIcon vi; private boolean hov=false;
        TBtn(String text, VIcon icon) {
            super(text); vi=icon;
            if (icon!=null) setIcon(icon);
            setFont(new Font("Monospaced",Font.BOLD,12));
            setFocusPainted(false); setBorderPainted(false);
            setContentAreaFilled(false); setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(6,14,6,14));
            addMouseListener(new MouseAdapter(){
                @Override public void mouseEntered(MouseEvent e){hov=true; repaint();}
                @Override public void mouseExited(MouseEvent e) {hov=false;repaint();}
            });
        }
        void syncTheme() { setForeground(FG); if(vi!=null)vi.setColor(FG); repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hov ? blend(PANEL,ACCENT,0.14f) : PANEL);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
            g2.setColor(hov ? blend(BORDER,ACCENT,0.5f) : BORDER);
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  THEMED SCROLLBAR
    // ══════════════════════════════════════════════════════════════════════════
    static JScrollPane themedScroll(Component v) {
        JScrollPane sp=new JScrollPane(v);
        sp.setBorder(null); sp.getViewport().setBackground(BG); sp.setBackground(BG);
        applyScrollBar(sp.getVerticalScrollBar());
        applyScrollBar(sp.getHorizontalScrollBar());
        return sp;
    }
    static void applyScrollBar(JScrollBar bar) {
        bar.setUI(new BasicScrollBarUI(){
            @Override protected void configureScrollBarColors(){
                thumbColor=blend(PANEL,ACCENT,0.32f); trackColor=PANEL;
            }
            @Override protected JButton createDecreaseButton(int o){return z();}
            @Override protected JButton createIncreaseButton(int o){return z();}
            JButton z(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
        });
        bar.setBackground(PANEL);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FIELDS
    // ══════════════════════════════════════════════════════════════════════════
    private JTree fileTree; private DefaultTreeModel treeModel;
    private TBtn runBtn, refreshBtn, settingsBtn;
    private JTabbedPane tabs;
    private JLabel statusLabel;
    private JPanel topPanel, sidePanel;
    private JSplitPane splitPane;
    private final VIcon iconFolderC = new VIcon(Ico.FOLDER_C,  14, ACCENT);
    private final VIcon iconFolderO = new VIcon(Ico.FOLDER_O,  14, ACCENT);
    private final VIcon iconFile    = new VIcon(Ico.FILE_JAVA,  14, FG);

    // ══════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ══════════════════════════════════════════════════════════════════════════
    public Main() {
        super("Java-Hub Terminal Pro");
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 820);
        setLocationRelativeTo(null);
        setupUI();
        refreshFiles();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  APPLY THEME
    // ══════════════════════════════════════════════════════════════════════════
    private void applyTheme(Theme t) {
        BG=t.bg; PANEL=t.panel; FG=t.fg; ACCENT=t.accent;
        ERR=t.error; BORDER=t.border; SUBTLE=t.subtle;
        themeHooks.forEach(Runnable::run);
        repaint();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BUILD UI
    // ══════════════════════════════════════════════════════════════════════════
    private void setupUI() {
        // TOP BAR
        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,8)){
            @Override protected void paintComponent(Graphics g){
                g.setColor(PANEL); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(BORDER); g.fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        topPanel.setOpaque(false);
        themeHooks.add(topPanel::repaint);

        VIcon appIco=new VIcon(Ico.TERMINAL,20,ACCENT);
        JLabel appLbl=new JLabel(" JAVA-HUB",appIco,JLabel.LEFT);
        appLbl.setFont(new Font("Monospaced",Font.BOLD,14));
        appLbl.setForeground(ACCENT);
        themeHooks.add(()->{ appIco.setColor(ACCENT); appLbl.setForeground(ACCENT); appLbl.repaint(); });

        refreshBtn  = new TBtn("REFRESH", new VIcon(Ico.REFRESH,  14, FG));
        runBtn      = new TBtn("EXECUTE", new VIcon(Ico.PLAY,     14, FG));
        settingsBtn = new TBtn("THEME",   new VIcon(Ico.SETTINGS, 14, FG));
        themeHooks.add(()->{ refreshBtn.syncTheme(); runBtn.syncTheme(); settingsBtn.syncTheme(); });

        statusLabel=new JLabel("● SYSTEM READY");
        statusLabel.setForeground(SUBTLE);
        statusLabel.setFont(new Font("Monospaced",Font.PLAIN,11));
        themeHooks.add(()->statusLabel.setForeground(SUBTLE));

        topPanel.add(appLbl);
        topPanel.add(Box.createHorizontalStrut(6));
        topPanel.add(refreshBtn); topPanel.add(runBtn); topPanel.add(settingsBtn);
        topPanel.add(Box.createHorizontalStrut(8));
        topPanel.add(statusLabel);

        // SIDEBAR
        sidePanel = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                g.setColor(PANEL); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(BORDER); g.fillRect(getWidth()-1,0,1,getHeight());
            }
        };
        sidePanel.setOpaque(false);
        sidePanel.setPreferredSize(new Dimension(224,0));
        themeHooks.add(sidePanel::repaint);

        JPanel sideHead = new JPanel(new FlowLayout(FlowLayout.LEFT,8,6)){
            @Override protected void paintComponent(Graphics g){
                g.setColor(blend(PANEL,BORDER,0.5f)); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(BORDER); g.fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        sideHead.setOpaque(false);
        themeHooks.add(sideHead::repaint);

        VIcon folderIco=new VIcon(Ico.FOLDER_O,13,SUBTLE);
        JLabel sideLbl=new JLabel("PROJECT FILES",folderIco,JLabel.LEFT);
        sideLbl.setFont(new Font("Monospaced",Font.BOLD,10));
        sideLbl.setForeground(SUBTLE);
        themeHooks.add(()->{ folderIco.setColor(SUBTLE); sideLbl.setForeground(SUBTLE); sideLbl.repaint(); });
        sideHead.add(sideLbl);

        DefaultMutableTreeNode root=new DefaultMutableTreeNode("src");
        treeModel=new DefaultTreeModel(root);
        fileTree=new JTree(treeModel);
        fileTree.setOpaque(false);
        fileTree.setFont(new Font("Monospaced",Font.PLAIN,12));
        fileTree.setRowHeight(24);
        fileTree.setRootVisible(false);
        fileTree.setShowsRootHandles(false);
        styleTree();
        themeHooks.add(this::styleTree);

        JScrollPane treeScroll=themedScroll(fileTree);
        treeScroll.getViewport().setOpaque(false);
        treeScroll.setOpaque(false);
        themeHooks.add(()->treeScroll.getViewport().setBackground(PANEL));

        sidePanel.add(sideHead, BorderLayout.NORTH);
        sidePanel.add(treeScroll, BorderLayout.CENTER);

        // TABS
        tabs = new JTabbedPane(){
            @Override protected void paintComponent(Graphics g){
                g.setColor(BG); g.fillRect(0,0,getWidth(),getHeight());
                super.paintComponent(g);
            }
        };
        tabs.setOpaque(false);
        tabs.setFont(new Font("Monospaced",Font.BOLD,11));
        themeHooks.add(()->{ tabs.setBackground(BG); tabs.setForeground(FG); tabs.repaint(); });

        // SPLIT
        splitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidePanel, tabs);
        splitPane.setDividerLocation(224);
        splitPane.setDividerSize(2);
        splitPane.setBorder(null);
        splitPane.setBackground(BORDER);
        themeHooks.add(()->{ splitPane.setBackground(BORDER); splitPane.repaint(); });

        getContentPane().setBackground(BG);
        themeHooks.add(()->{ getContentPane().setBackground(BG); getContentPane().repaint(); });

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        refreshBtn.addActionListener(e->refreshFiles());
        settingsBtn.addActionListener(e->openSettings());
        runBtn.addActionListener(e->runSelected());
        fileTree.addMouseListener(new MouseAdapter(){
            @Override public void mouseClicked(MouseEvent e){
                if(e.getClickCount()==2) runSelected();
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TREE CELL RENDERER
    // ══════════════════════════════════════════════════════════════════════════
    private void styleTree() {
        iconFolderC.setColor(ACCENT); iconFolderO.setColor(ACCENT); iconFile.setColor(FG);
        fileTree.setCellRenderer(new DefaultTreeCellRenderer(){
            @Override public Component getTreeCellRendererComponent(
                    JTree t, Object v, boolean sel, boolean exp, boolean leaf, int row, boolean foc) {
                super.getTreeCellRendererComponent(t,v,sel,exp,leaf,row,foc);
                setOpaque(true);
                Color rowBg = sel ? blend(PANEL,ACCENT,0.2f) : PANEL;
                setBackground(rowBg);
                setForeground(leaf ? FG : ACCENT);
                setFont(new Font("Monospaced", leaf ? Font.PLAIN : Font.BOLD, 12));
                setBorderSelectionColor(new Color(0,0,0,0));
                setBackgroundSelectionColor(rowBg);
                setBackgroundNonSelectionColor(PANEL);
                setTextSelectionColor(leaf ? FG : ACCENT);
                setTextNonSelectionColor(leaf ? FG : ACCENT);
                setIcon(leaf ? iconFile : (exp ? iconFolderO : iconFolderC));
                setBorder(new EmptyBorder(1,leaf?4:2,1,4));
                return this;
            }
        });
        fileTree.repaint();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FILE TREE REFRESH
    // ══════════════════════════════════════════════════════════════════════════
    private void refreshFiles() {
        DefaultMutableTreeNode root=(DefaultMutableTreeNode)treeModel.getRoot();
        root.removeAllChildren();
        Map<String,List<Path>> groups=new TreeMap<>();
        try(var s=Files.walk(PROJECT_ROOT)){
            s.filter(p->p.toString().endsWith(".java")&&!p.startsWith(BUILD_DIR))
                    .forEach(p->{
                        Path rel=PROJECT_ROOT.relativize(p.getParent());
                        String pkg=rel.toString().isEmpty()?"(default)":rel.toString().replace(File.separator,".");
                        groups.computeIfAbsent(pkg,k->new ArrayList<>()).add(p);
                    });
        } catch(IOException ignored){}
        groups.forEach((pkg,files)->{
            DefaultMutableTreeNode pkgNode=new DefaultMutableTreeNode(pkg);
            files.stream().sorted(Comparator.comparing(p->p.getFileName().toString()))
                    .forEach(p->pkgNode.add(new DefaultMutableTreeNode(new JavaFile(p))));
            root.add(pkgNode);
        });
        treeModel.reload();
        for(int i=0;i<fileTree.getRowCount();i++) fileTree.expandRow(i);
        setStatus("REFRESHED — "+groups.values().stream().mapToInt(List::size).sum()+" file(s)");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RUN
    // ══════════════════════════════════════════════════════════════════════════
    private void runSelected() {
        TreePath tp=fileTree.getSelectionPath();
        if(tp==null){setStatus("SELECT A FILE FIRST");return;}
        DefaultMutableTreeNode node=(DefaultMutableTreeNode)tp.getLastPathComponent();
        if(!(node.getUserObject() instanceof JavaFile jf)){setStatus("SELECT A .java FILE");return;}
        runBtn.setEnabled(false); setStatus("COMPILING…");
        new SwingWorker<Boolean,Void>(){
            String err="";
            @Override protected Boolean doInBackground() throws Exception {
                Files.createDirectories(BUILD_DIR);
                List<String> src;
                try(var s=Files.walk(PROJECT_ROOT)){
                    src=s.filter(p->p.toString().endsWith(".java")&&!p.startsWith(BUILD_DIR))
                            .map(Path::toString).collect(Collectors.toList());
                }
                List<String> cmd=new ArrayList<>(List.of("javac","-d",BUILD_DIR.toString()));
                cmd.addAll(src);
                Process p=new ProcessBuilder(cmd).directory(PROJECT_ROOT.toFile())
                        .redirectErrorStream(true).start();
                err=new String(p.getInputStream().readAllBytes());
                return p.waitFor()==0;
            }
            @Override protected void done(){
                runBtn.setEnabled(true);
                try{
                    if(get()){setStatus("OK — LAUNCHING "+jf.shortName); launch(jf);}
                    else{setStatus("COMPILE ERROR"); showErr(jf.shortName,err);}
                }catch(Exception ex){setStatus("ERROR: "+ex.getMessage());}
            }
        }.execute();
    }

    private void launch(JavaFile jf) throws IOException {
        Process proc=new ProcessBuilder("java","-cp",BUILD_DIR.toString(),jf.className)
                .directory(PROJECT_ROOT.toFile()).start();
        addTab(jf.shortName, proc, false);
    }

    private void showErr(String name, String err) {
        TerminalPanel t=addTab("ERR: "+name, null, true);
        t.appendPublic("COMPILE ERRORS:\n\n"+err, ERR);
    }

    private TerminalPanel addTab(String title, Process proc, boolean isErr) {
        TerminalPanel term=new TerminalPanel(proc, title, this);
        if(proc!=null) term.startStreaming();
        tabs.addTab(title, term);
        int i=tabs.indexOfComponent(term);
        tabs.setTabComponentAt(i, makeTabHeader(title, term, isErr));
        tabs.setSelectedComponent(term);
        return term;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TAB HEADER WITH CUSTOM ICON + CLOSE
    // ══════════════════════════════════════════════════════════════════════════
    private JPanel makeTabHeader(String title, Component comp, boolean isErr) {
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,4,2));
        p.setOpaque(false);

        VIcon tabIco=new VIcon(isErr?Ico.CLOSE:Ico.TERMINAL,12,isErr?ERR:ACCENT);
        JLabel lbl=new JLabel(title, tabIco, JLabel.LEFT);
        lbl.setFont(new Font("Monospaced",Font.BOLD,11));
        lbl.setForeground(FG);
        themeHooks.add(()->{tabIco.setColor(isErr?ERR:ACCENT);lbl.setForeground(FG);lbl.repaint();});

        VIcon closeIco=new VIcon(Ico.CLOSE,10,SUBTLE);
        JButton cb=new JButton(closeIco);
        cb.setContentAreaFilled(false); cb.setBorderPainted(false);
        cb.setFocusPainted(false); cb.setOpaque(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cb.setBorder(new EmptyBorder(0,2,0,0));
        cb.addActionListener(e->{int i=tabs.indexOfComponent(comp);if(i!=-1)tabs.removeTabAt(i);});
        cb.addMouseListener(new MouseAdapter(){
            @Override public void mouseEntered(MouseEvent e){closeIco.setColor(ERR);   cb.repaint();}
            @Override public void mouseExited(MouseEvent e) {closeIco.setColor(SUBTLE);cb.repaint();}
        });
        themeHooks.add(()->{closeIco.setColor(SUBTLE);cb.repaint();});

        p.add(lbl); p.add(cb);
        return p;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SETTINGS DIALOG
    // ══════════════════════════════════════════════════════════════════════════
    private void openSettings() {
        JDialog dlg=new JDialog(this,"Theme Settings",true);
        dlg.setSize(560,450); dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel root=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){g.setColor(BG);g.fillRect(0,0,getWidth(),getHeight());}
        };
        root.setOpaque(false);

        // Title bar
        JPanel titleBar=new JPanel(new FlowLayout(FlowLayout.LEFT,12,10)){
            @Override protected void paintComponent(Graphics g){
                g.setColor(PANEL);g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(BORDER);g.fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        titleBar.setOpaque(false);
        VIcon si=new VIcon(Ico.SETTINGS,16,ACCENT);
        JLabel tl=new JLabel("THEME SETTINGS",si,JLabel.LEFT);
        tl.setFont(new Font("Monospaced",Font.BOLD,13));
        tl.setForeground(ACCENT);
        titleBar.add(tl);

        // Presets
        JPanel presetWrap=new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                g.setColor(PANEL);g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(BORDER);g.fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        presetWrap.setOpaque(false);
        presetWrap.setBorder(new EmptyBorder(10,14,10,14));

        JLabel pl=new JLabel("PRESETS");
        pl.setFont(new Font("Monospaced",Font.BOLD,10));
        pl.setForeground(SUBTLE); pl.setBorder(new EmptyBorder(0,0,8,0));

        JPanel presetRow=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        presetRow.setOpaque(false);
        for(Theme t:Theme.PRESETS){
            JButton btn=new JButton(t.name){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(t.panel); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor(t.accent); g2.fillRect(0,getHeight()-3,getWidth(),3);
                    g2.setColor(t.border); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Monospaced",Font.BOLD,11)); btn.setForeground(t.fg);
            btn.setContentAreaFilled(false); btn.setBorderPainted(false);
            btn.setFocusPainted(false); btn.setOpaque(false);
            btn.setBorder(new EmptyBorder(7,14,10,14));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e->{applyTheme(t);dlg.dispose();});
            presetRow.add(btn);
        }
        presetWrap.add(pl, BorderLayout.NORTH);
        presetWrap.add(presetRow, BorderLayout.CENTER);

        // Custom swatches
        JPanel customOuter=new JPanel(new BorderLayout());
        customOuter.setOpaque(false);
        customOuter.setBorder(new EmptyBorder(12,14,8,14));

        JLabel cl=new JLabel("CUSTOM COLORS");
        cl.setFont(new Font("Monospaced",Font.BOLD,10));
        cl.setForeground(SUBTLE); cl.setBorder(new EmptyBorder(0,0,10,0));

        String[] cLabels={"Background","Panel","Foreground","Accent","Error","Border"};
        Color[] cur={BG,PANEL,FG,ACCENT,ERR,BORDER};
        JPanel grid=new JPanel(new GridLayout(2,3,10,10));
        grid.setOpaque(false);

        for(int i=0;i<cLabels.length;i++){
            final int idx=i;
            JPanel cell=new JPanel(new BorderLayout(0,4)); cell.setOpaque(false);
            JLabel ll=new JLabel(cLabels[i]);
            ll.setFont(new Font("Monospaced",Font.PLAIN,10)); ll.setForeground(SUBTLE);
            JPanel sw=new JPanel(){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(cur[idx]); g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                    g2.setColor(BORDER);  g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);
                    g2.dispose();
                }
            };
            sw.setOpaque(false); sw.setPreferredSize(new Dimension(0,28));
            sw.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            sw.addMouseListener(new MouseAdapter(){
                @Override public void mouseClicked(MouseEvent e){
                    Color c=JColorChooser.showDialog(dlg,"Pick "+cLabels[idx],cur[idx]);
                    if(c!=null){cur[idx]=c;sw.repaint();}
                }
            });
            cell.add(ll,BorderLayout.NORTH); cell.add(sw,BorderLayout.CENTER);
            grid.add(cell);
        }
        customOuter.add(cl,BorderLayout.NORTH); customOuter.add(grid,BorderLayout.CENTER);

        // Bottom bar
        JPanel bot=new JPanel(new FlowLayout(FlowLayout.RIGHT,12,8)){
            @Override protected void paintComponent(Graphics g){
                g.setColor(PANEL);g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(BORDER);g.fillRect(0,0,getWidth(),1);
            }
        };
        bot.setOpaque(false);
        TBtn applyBtn=new TBtn("APPLY CUSTOM",new VIcon(Ico.PLAY,12,FG));
        applyBtn.setForeground(FG);
        applyBtn.addActionListener(e->{
            applyTheme(new Theme("Custom",cur[0],cur[1],cur[2],cur[3],cur[4],cur[5],
                    blend(cur[2],cur[1],0.5f)));
            dlg.dispose();
        });
        bot.add(applyBtn);

        JPanel center=new JPanel(new BorderLayout()); center.setOpaque(false);
        center.add(presetWrap,BorderLayout.NORTH);
        center.add(customOuter,BorderLayout.CENTER);

        root.add(titleBar,BorderLayout.NORTH);
        root.add(center,BorderLayout.CENTER);
        root.add(bot,BorderLayout.SOUTH);
        dlg.add(root);
        dlg.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  STATUS
    // ══════════════════════════════════════════════════════════════════════════
    private void setStatus(String msg){
        SwingUtilities.invokeLater(()->{
            statusLabel.setText("● "+msg);
            statusLabel.setForeground(msg.contains("ERROR")?ERR:SUBTLE);
        });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ASCII BANNER
    // ══════════════════════════════════════════════════════════════════════════
    static class Ascii {
        private static final Map<Character,String[]> F=new HashMap<>();
        static{
            F.put('R',new String[]{"██████  ","██   ██ ","██████  ","██   ██ ","██   ██ "});
            F.put('U',new String[]{"██   ██ ","██   ██ ","██   ██ ","██   ██ "," █████  "});
            F.put('N',new String[]{"███   ██ ","████  ██ ","██ ██ ██ ","██  ████ ","██   ███ "});
            F.put('I',new String[]{" █████ ","  ██   ","  ██   ","  ██   "," █████ "});
            F.put('G',new String[]{" █████ ","██     ","██  ██ ","██  ██ "," █████ "});
            F.put('!',new String[]{"██","██","██","  ","██"});
        }
        static String gen(String text){
            StringBuilder sb=new StringBuilder();
            for(int i=0;i<5;i++){
                for(char c:text.toUpperCase().toCharArray())
                    sb.append(F.containsKey(c)?F.get(c)[i]:"     ");
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TERMINAL PANEL
    // ══════════════════════════════════════════════════════════════════════════
    static class TerminalPanel extends JPanel {
        private final JTextPane  area =new JTextPane();
        private final JTextField input=new JTextField();
        private final Process    proc;
        private final Main       par;

        TerminalPanel(Process proc, String name, Main par){
            this.proc=proc; this.par=par;
            setLayout(new BorderLayout()); setOpaque(false);

            area.setEditable(false); area.setOpaque(true);
            area.setBackground(BG); area.setForeground(FG);
            area.setFont(new Font("Monospaced",Font.PLAIN,13));
            area.setCaretColor(ACCENT);
            area.setBorder(new EmptyBorder(10,12,10,12));
            par.themeHooks.add(()->{area.setBackground(BG);area.setForeground(FG);area.setCaretColor(ACCENT);area.repaint();});

            // Toolbar
            JPanel bar=new JPanel(new BorderLayout()){
                @Override protected void paintComponent(Graphics g){
                    g.setColor(PANEL);g.fillRect(0,0,getWidth(),getHeight());
                    g.setColor(BORDER);g.fillRect(0,getHeight()-1,getWidth(),1);
                }
            };
            bar.setOpaque(false); bar.setBorder(new EmptyBorder(0,12,0,8));
            par.themeHooks.add(bar::repaint);

            VIcon ni=new VIcon(Ico.TERMINAL,13,ACCENT);
            JLabel nl=new JLabel(" "+name,ni,JLabel.LEFT);
            nl.setFont(new Font("Monospaced",Font.BOLD,11)); nl.setForeground(ACCENT);
            nl.setBorder(new EmptyBorder(6,0,6,0));
            par.themeHooks.add(()->{ni.setColor(ACCENT);nl.setForeground(ACCENT);nl.repaint();});

            TBtn clearBtn=new TBtn("CLEAR",new VIcon(Ico.CLEAR,12,FG));
            clearBtn.setForeground(FG); clearBtn.setBorder(new EmptyBorder(4,10,4,10));
            clearBtn.addActionListener(e->area.setText(""));
            par.themeHooks.add(clearBtn::syncTheme);

            bar.add(nl,BorderLayout.WEST); bar.add(clearBtn,BorderLayout.EAST);

            // Input bar
            JPanel inputBar=new JPanel(new BorderLayout()){
                @Override protected void paintComponent(Graphics g){
                    g.setColor(PANEL);g.fillRect(0,0,getWidth(),getHeight());
                    g.setColor(ACCENT);g.fillRect(0,0,getWidth(),1);
                }
            };
            inputBar.setOpaque(false);
            par.themeHooks.add(inputBar::repaint);

            JLabel prompt=new JLabel(" ❯ ");
            prompt.setFont(new Font("Monospaced",Font.BOLD,14));
            prompt.setForeground(ACCENT);
            par.themeHooks.add(()->{prompt.setForeground(ACCENT);prompt.repaint();});

            input.setOpaque(true); input.setBackground(PANEL);
            input.setForeground(ACCENT); input.setCaretColor(FG);
            input.setBorder(new EmptyBorder(8,4,8,10));
            input.setFont(new Font("Monospaced",Font.PLAIN,13));
            par.themeHooks.add(()->{input.setBackground(PANEL);input.setForeground(ACCENT);input.repaint();});

            inputBar.add(prompt,BorderLayout.WEST); inputBar.add(input,BorderLayout.CENTER);

            JScrollPane scroll=themedScroll(area);
            par.themeHooks.add(()->scroll.getViewport().setBackground(BG));

            add(bar,BorderLayout.NORTH);
            add(scroll,BorderLayout.CENTER);
            add(inputBar,BorderLayout.SOUTH);

            input.addActionListener(e->send());
        }

        void startStreaming(){
            append(Ascii.gen("RUNNING!"),ACCENT);
            append("\n>> PROCESS: "+proc.info().command().orElse("?")+"\n\n",FG);
            new Thread(()->stream(proc.getInputStream(),FG)).start();
            new Thread(()->stream(proc.getErrorStream(),ERR)).start();
        }

        private void send(){
            if(proc==null||!proc.isAlive()) return;
            String t=input.getText();
            try{
                proc.getOutputStream().write((t+"\n").getBytes());
                proc.getOutputStream().flush();
                append("> "+t+"\n",ACCENT);
                input.setText("");
            }catch(IOException ignored){}
        }

        private void stream(InputStream s, Color c){
            try(Scanner sc=new Scanner(s)){
                while(sc.hasNextLine()){String l=sc.nextLine();SwingUtilities.invokeLater(()->append(l+"\n",c));}
            }
        }

        public void appendPublic(String s,Color c){append(s,c);}

        private void append(String s,Color c){
            StyleContext sc=StyleContext.getDefaultStyleContext();
            AttributeSet as=sc.addAttribute(SimpleAttributeSet.EMPTY,StyleConstants.Foreground,c);
            try{Document d=area.getDocument();d.insertString(d.getLength(),s,as);area.setCaretPosition(d.getLength());}
            catch(Exception ignored){}
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  JAVA FILE MODEL
    // ══════════════════════════════════════════════════════════════════════════
    private static class JavaFile {
        final String className, shortName;
        JavaFile(Path p){
            shortName=p.getFileName().toString().replace(".java","");
            String pkg="";
            try{
                pkg=Files.lines(p).filter(l->l.trim().startsWith("package "))
                        .map(l->l.replace("package","").replace(";","").trim()+".")
                        .findFirst().orElse("");
            }catch(IOException ignored){}
            this.className=pkg+shortName;
        }
        @Override public String toString(){return shortName;}
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  SHARED UTILS
    // ══════════════════════════════════════════════════════════════════════════
    static Color blend(Color a, Color b, float t){
        return new Color(
                Math.max(0,Math.min(255,(int)(a.getRed()*(1-t)+b.getRed()*t))),
                Math.max(0,Math.min(255,(int)(a.getGreen()*(1-t)+b.getGreen()*t))),
                Math.max(0,Math.min(255,(int)(a.getBlue()*(1-t)+b.getBlue()*t))));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->new Main().setVisible(true));
    }
}