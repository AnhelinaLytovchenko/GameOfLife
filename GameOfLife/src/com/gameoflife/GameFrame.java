package com.gameoflife;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Головне вікно застосунку Гра Життя.
 *
 * Підтримує перемикання між послідовним (GameGrid) та паралельним
 * (ParallelGameGrid) режимами в реальному часі.
 * У нижньому рядку відображається поточний режим і кількість потоків.
 */
public class GameFrame extends JFrame {

    private static final int GRID_ROWS = 60;
    private static final int GRID_COLS = 80;

    private GameGrid       grid;
    private GamePanel      gamePanel;
    private Timer          timer;
    private boolean        running = false;
    private int            speed   = 100;

    private JLabel  genLabel;
    private JLabel  popLabel;
    private JLabel  modeLabel;
    private JButton startStopBtn;
    private JPanel  gamePanelWrapper;

    /** Поточний режим: true = паралельний */
    private boolean parallelMode = false;

    public GameFrame() {
        super("🧬 Гра Життя — Conway's Game of Life");
        this.grid      = new GameGrid(GRID_ROWS, GRID_COLS);
        this.gamePanel = new GamePanel(grid);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupUI();
        setupTimer();

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(980, 680));

        // Закриваємо пул при виході
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (grid instanceof ParallelGameGrid pg) pg.shutdown();
            }
        });
    }

    // -------------------------------------------------------------------------
    // UI
    // -------------------------------------------------------------------------

    private void setupUI() {
        add(createTopPanel(), BorderLayout.NORTH);

        gamePanelWrapper = new JPanel(new BorderLayout());
        gamePanelWrapper.setBackground(new Color(10, 10, 20));
        gamePanelWrapper.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 80), 2));
        gamePanelWrapper.add(gamePanel, BorderLayout.CENTER);
        add(gamePanelWrapper, BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);
        add(createRightPanel(),  BorderLayout.EAST);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setBackground(new Color(20, 20, 35));
        panel.setBorder(new EmptyBorder(4, 8, 4, 8));

        // Старт/Стоп
        startStopBtn = createButton("▶  Старт", new Color(40, 160, 80));
        startStopBtn.addActionListener(e -> toggleRunning());
        panel.add(startStopBtn);

        // Крок
        JButton stepBtn = createButton("⏭  Крок", new Color(60, 100, 180));
        stepBtn.addActionListener(e -> {
            if (!running) { grid.step(); gamePanel.repaint(); updateStats(); }
        });
        panel.add(stepBtn);

        // Очистити
        JButton clearBtn = createButton("🗑  Очистити", new Color(180, 60, 60));
        clearBtn.addActionListener(e -> {
            stopSimulation(); grid.clear(); gamePanel.repaint(); updateStats();
        });
        panel.add(clearBtn);

        // Випадкове
        JButton randomBtn = createButton("🎲  Випадкове", new Color(120, 60, 180));
        randomBtn.addActionListener(e -> {
            stopSimulation(); grid.randomize(0.30); gamePanel.repaint(); updateStats();
        });
        panel.add(randomBtn);

        panel.add(Box.createHorizontalStrut(12));

        // Перемикач режиму
        JToggleButton modeToggle = new JToggleButton("⚡ Паралельний");
        modeToggle.setBackground(new Color(40, 40, 65));
        modeToggle.setForeground(new Color(160, 220, 255));
        modeToggle.setFont(new Font("Arial", Font.BOLD, 12));
        modeToggle.setFocusPainted(false);
        modeToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        modeToggle.addActionListener(e -> switchMode(modeToggle.isSelected()));
        panel.add(modeToggle);

        panel.add(Box.createHorizontalStrut(12));

        // Швидкість
        JLabel speedLabel = new JLabel("Швидкість:");
        speedLabel.setForeground(Color.LIGHT_GRAY);
        speedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(speedLabel);

        JSlider speedSlider = new JSlider(1, 20, 5);
        speedSlider.setBackground(new Color(20, 20, 35));
        speedSlider.setForeground(Color.LIGHT_GRAY);
        speedSlider.setPreferredSize(new Dimension(130, 28));
        speedSlider.addChangeListener(e -> {
            speed = Math.max(20, 500 / speedSlider.getValue());
            if (running) timer.setDelay(speed);
        });
        panel.add(speedSlider);

        panel.add(Box.createHorizontalStrut(12));

        // Колір
        JLabel colorLabel = new JLabel("Колір:");
        colorLabel.setForeground(Color.LIGHT_GRAY);
        colorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(colorLabel);

        String[] schemes = {"🟢 Зелений", "🔵 Синій", "🔴 Червоний", "⚪ Білий"};
        JComboBox<String> colorBox = new JComboBox<>(schemes);
        colorBox.setBackground(new Color(35, 35, 55));
        colorBox.setForeground(Color.WHITE);
        colorBox.addActionListener(e -> {
            switch (colorBox.getSelectedIndex()) {
                case 0 -> gamePanel.setColorScheme(GamePanel.ColorScheme.GREEN);
                case 1 -> gamePanel.setColorScheme(GamePanel.ColorScheme.BLUE);
                case 2 -> gamePanel.setColorScheme(GamePanel.ColorScheme.RED);
                case 3 -> gamePanel.setColorScheme(GamePanel.ColorScheme.WHITE);
            }
        });
        panel.add(colorBox);

        // Сітка
        JCheckBox gridCheck = new JCheckBox("Сітка", true);
        gridCheck.setBackground(new Color(20, 20, 35));
        gridCheck.setForeground(Color.LIGHT_GRAY);
        gridCheck.addActionListener(e -> gamePanel.setDrawGrid(gridCheck.isSelected()));
        panel.add(gridCheck);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        panel.setBackground(new Color(15, 15, 28));
        panel.setBorder(new EmptyBorder(3, 12, 3, 12));

        genLabel = new JLabel("Покоління: 0");
        genLabel.setForeground(new Color(100, 220, 150));
        genLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(genLabel);

        popLabel = new JLabel("Популяція: 0");
        popLabel.setForeground(new Color(100, 180, 255));
        popLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(popLabel);

        modeLabel = new JLabel("Режим: послідовний");
        modeLabel.setForeground(new Color(200, 160, 80));
        modeLabel.setFont(new Font("Monospaced", Font.BOLD, 13));
        panel.add(modeLabel);

        JLabel hint = new JLabel("  Клік/Перетяг — малювати | Пробіл — Старт/Стоп");
        hint.setForeground(new Color(100, 100, 130));
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        panel.add(hint);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(18, 18, 32));
        panel.setBorder(new EmptyBorder(12, 10, 12, 10));
        panel.setPreferredSize(new Dimension(160, 0));

        JLabel title = new JLabel("Патерни");
        title.setForeground(new Color(200, 200, 255));
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        addPatternButton(panel, "🛸 Глайдер",  () -> { grid.clear(); grid.insertGlider(5, 5); });
        panel.add(Box.createVerticalStrut(6));
        addPatternButton(panel, "💫 Пульсар",  () -> { grid.clear(); grid.insertPulsar(10, 10); });
        panel.add(Box.createVerticalStrut(6));
        addPatternButton(panel, "🔫 Гармата",  () -> { grid.clear(); grid.insertGosperGun(5, 5); });
        panel.add(Box.createVerticalStrut(6));
        addPatternButton(panel, "🌊 5 Глайдерів", () -> {
            grid.clear();
            for (int i = 0; i < 5; i++) grid.insertGlider(2 + i * 10, 2 + i * 12);
        });
        panel.add(Box.createVerticalStrut(6));
        addPatternButton(panel, "✨ Комбо", () -> {
            grid.clear();
            grid.insertPulsar(20, 30);
            grid.insertGlider(2, 2);
            grid.insertGlider(2, 60);
            grid.insertGlider(50, 2);
        });
        panel.add(Box.createVerticalStrut(12));

        // Кнопка бенчмарку
        JButton benchBtn = createPatternButton("📊 Бенчмарк");
        benchBtn.setForeground(new Color(255, 220, 100));
        benchBtn.addActionListener(e -> runBenchmarkDialog());
        panel.add(benchBtn);

        panel.add(Box.createVerticalGlue());

        JTextArea help = new JTextArea(
            "Правила Конвея:\n\n" +
            "• 2-3 сусіди →\n  клітина живе\n\n" +
            "• < 2 сусідів →\n  самотність\n\n" +
            "• > 3 сусідів →\n  перенаселення\n\n" +
            "• 3 сусіди →\n  народження"
        );
        help.setEditable(false);
        help.setBackground(new Color(25, 25, 42));
        help.setForeground(new Color(140, 140, 180));
        help.setFont(new Font("Arial", Font.PLAIN, 10));
        help.setBorder(new EmptyBorder(8, 6, 8, 6));
        panel.add(help);

        return panel;
    }

    // -------------------------------------------------------------------------
    // Логіка перемикання режиму
    // -------------------------------------------------------------------------

    /**
     * Перемикає між послідовним і паралельним режимами.
     * Зберігає поточний стан сітки (популяцію, покоління).
     */
    private void switchMode(boolean toParallel) {
        stopSimulation();

        // Зберігаємо стан поточної сітки
        boolean[][] state    = copyGrid();
        int         gen      = grid.getGeneration();
        int         pop      = grid.getPopulation();

        // Закриваємо старий пул (якщо паралельний)
        if (grid instanceof ParallelGameGrid pg) pg.shutdown();

        // Створюємо нову сітку потрібного типу
        if (toParallel) {
            grid = new ParallelGameGrid(GRID_ROWS, GRID_COLS);
            modeLabel.setText("Режим: паралельний (" +
                    ((ParallelGameGrid) grid).getParallelism() + " потоків)");
        } else {
            grid = new GameGrid(GRID_ROWS, GRID_COLS);
            modeLabel.setText("Режим: послідовний");
        }

        // Відновлюємо стан
        restoreGrid(state);
        // generation/population відновлюються через restoreGrid (toggleCell lasts)
        // але покоління — вручну через рефлексію неможливо без сеттера.
        // Для простоти скидаємо до 0.
        parallelMode = toParallel;

        // Підміняємо gamePanel
        gamePanelWrapper.remove(gamePanel);
        gamePanel = new GamePanel(grid);
        gamePanelWrapper.add(gamePanel, BorderLayout.CENTER);
        gamePanelWrapper.revalidate();
        gamePanelWrapper.repaint();

        updateStats();
    }

    private boolean[][] copyGrid() {
        boolean[][] copy = new boolean[GRID_ROWS][GRID_COLS];
        for (int r = 0; r < GRID_ROWS; r++)
            for (int c = 0; c < GRID_COLS; c++)
                copy[r][c] = grid.getCell(r, c);
        return copy;
    }

    private void restoreGrid(boolean[][] state) {
        for (int r = 0; r < GRID_ROWS; r++)
            for (int c = 0; c < GRID_COLS; c++)
                grid.setCell(r, c, state[r][c]);
    }

    // -------------------------------------------------------------------------
    // Бенчмарк у діалозі
    // -------------------------------------------------------------------------

    private void runBenchmarkDialog() {
        stopSimulation();
        JDialog dlg = new JDialog(this, "Бенчмарк", true);
        dlg.setSize(460, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JTextArea output = new JTextArea("Запускаємо бенчмарк...\n");
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 12));
        output.setBackground(new Color(15, 15, 25));
        output.setForeground(new Color(100, 220, 150));
        dlg.add(new JScrollPane(output), BorderLayout.CENTER);

        JButton closeBtn = new JButton("Закрити");
        closeBtn.addActionListener(e -> dlg.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(new Color(20, 20, 35));
        btnPanel.add(closeBtn);
        dlg.add(btnPanel, BorderLayout.SOUTH);

        // Запускаємо бенчмарк у фоновому потоці, щоб не блокувати EDT
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            int[] sizes = {100, 200, 400};
            int[] threads = {2, 4, Runtime.getRuntime().availableProcessors()};

            sb.append(String.format("%-12s %-14s %10s %10s%n",
                    "Розмір", "Режим", "Час (мс)", "Прискор."));
            sb.append("-".repeat(50)).append("\n");

            for (int size : sizes) {
                long seqMs = GameOfLifeBenchmark.measureSequential(size, size);
                sb.append(String.format("%-12s %-14s %10d %10s%n",
                        size + "x" + size, "sequential", seqMs, "1.00x"));

                for (int t : threads) {
                    long parMs = GameOfLifeBenchmark.measureParallel(size, size, t);
                    double sp  = (double) seqMs / parMs;
                    sb.append(String.format("%-12s %-14s %10d %9.2fx%n",
                            size + "x" + size, t + " потоки", parMs, sp));
                }
                sb.append("\n");
            }

            String result = sb.toString();
            SwingUtilities.invokeLater(() -> output.setText(result));
        }).start();

        dlg.setVisible(true);
    }

    // -------------------------------------------------------------------------
    // Таймер та стан
    // -------------------------------------------------------------------------

    private void setupTimer() {
        timer = new Timer(speed, e -> {
            grid.step();
            gamePanel.repaint();
            updateStats();
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(ke -> {
            if (ke.getID() == KeyEvent.KEY_PRESSED && ke.getKeyCode() == KeyEvent.VK_SPACE) {
                toggleRunning();
                return true;
            }
            return false;
        });
    }

    private void toggleRunning() {
        if (running) stopSimulation(); else startSimulation();
    }

    private void startSimulation() {
        running = true;
        timer.setDelay(speed);
        timer.start();
        startStopBtn.setText("⏸  Пауза");
        startStopBtn.setBackground(new Color(180, 120, 20));
    }

    private void stopSimulation() {
        running = false;
        timer.stop();
        startStopBtn.setText("▶  Старт");
        startStopBtn.setBackground(new Color(40, 160, 80));
    }

    private void updateStats() {
        genLabel.setText("Покоління: " + grid.getGeneration());
        popLabel.setText("Популяція: " + grid.getPopulation());
    }

    // -------------------------------------------------------------------------
    // Допоміжні методи
    // -------------------------------------------------------------------------

    private void addPatternButton(JPanel panel, String text, Runnable action) {
        JButton btn = createPatternButton(text);
        btn.addActionListener(e -> {
            stopSimulation();
            action.run();
            gamePanel.repaint();
            updateStats();
        });
        panel.add(btn);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        return btn;
    }

    private JButton createPatternButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(35, 35, 60));
        btn.setForeground(new Color(200, 210, 255));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 100)));
        btn.setFont(new Font("Arial", Font.PLAIN, 11));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(140, 28));
        btn.setPreferredSize(new Dimension(140, 28));
        return btn;
    }
}
