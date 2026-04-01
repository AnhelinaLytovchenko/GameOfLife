package com.gameoflife;

/**
 * Бенчмарк для порівняння продуктивності послідовної (GameGrid) та
 * паралельної (ParallelGameGrid) реалізацій Гри Життя.
 *
 * Запускає кілька конфігурацій (розміри сітки, кількість потоків)
 * і виводить таблицю з часом виконання та коефіцієнтом прискорення.
 */
public class GameOfLifeBenchmark {

    /** Кількість кроків еволюції в одному вимірюванні */
    private static final int STEPS = 200;

    /** Кількість «прогрівальних» кроків перед вимірюванням (JIT warm-up) */
    private static final int WARMUP_STEPS = 50;

    /** Густота початкового заповнення */
    private static final double DENSITY = 0.30;

    public static void main(String[] args) {
        System.out.println("=".repeat(72));
        System.out.println("  Game of Life — Benchmark: Sequential vs Parallel (Fork/Join)");
        System.out.println("=".repeat(72));
        System.out.printf("  Steps per run: %d  |  Warm-up: %d  |  Density: %.0f%%%n%n",
                STEPS, WARMUP_STEPS, DENSITY * 100);

        int[] gridSizes = {100, 200, 400, 800};
        int[] threadCounts = {2, 4, Runtime.getRuntime().availableProcessors()};

        printHeader();

        for (int size : gridSizes) {
            // --- Послідовна версія ---
            long seqMs = measureSequential(size, size);

            System.out.printf("| %4dx%-4d | %9s | %8d ms |   1.00x  |%n",
                    size, size, "sequential", seqMs);

            // --- Паралельна версія (різна кількість потоків) ---
            for (int threads : threadCounts) {
                long parMs = measureParallel(size, size, threads);
                double speedup = (double) seqMs / parMs;
                System.out.printf("| %4dx%-4d | %4d threads | %8d ms |   %.2fx  |%n",
                        size, size, threads, parMs, speedup);
            }

            printSeparator();
        }

        System.out.println("\nАвто-паралелізм (кількість логічних CPU): "
                + Runtime.getRuntime().availableProcessors());
    }

    /**
     * Вимірює час N кроків послідовної реалізації.
     *
     * @param rows кількість рядків
     * @param cols кількість стовпців
     * @return час у мілісекундах
     */
    public static long measureSequential(int rows, int cols) {
        GameGrid grid = new GameGrid(rows, cols);
        grid.randomize(DENSITY);

        // Прогрів JIT
        for (int i = 0; i < WARMUP_STEPS; i++) grid.step();

        // Вимірювання
        long start = System.currentTimeMillis();
        for (int i = 0; i < STEPS; i++) grid.step();
        return System.currentTimeMillis() - start;
    }

    /**
     * Вимірює час N кроків паралельної реалізації.
     *
     * @param rows      кількість рядків
     * @param cols      кількість стовпців
     * @param threads   кількість потоків у ForkJoinPool
     * @return час у мілісекундах
     */
    public static long measureParallel(int rows, int cols, int threads) {
        ParallelGameGrid grid = new ParallelGameGrid(rows, cols, threads);
        grid.randomize(DENSITY);

        // Прогрів JIT
        for (int i = 0; i < WARMUP_STEPS; i++) grid.step();

        // Вимірювання
        long start = System.currentTimeMillis();
        for (int i = 0; i < STEPS; i++) grid.step();
        long elapsed = System.currentTimeMillis() - start;

        grid.shutdown();
        return elapsed;
    }

    // -------------------------------------------------------------------------
    // Допоміжні методи форматування
    // -------------------------------------------------------------------------

    private static void printHeader() {
        printSeparator();
        System.out.println("| Grid       | Mode        |    Time     |  Speedup |");
        printSeparator();
    }

    private static void printSeparator() {
        System.out.println("+------------+-------------+-------------+----------+");
    }
}
