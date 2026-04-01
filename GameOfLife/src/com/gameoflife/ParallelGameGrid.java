package com.gameoflife;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Паралельна реалізація Гри Життя на основі Java Fork/Join Framework.
 *
 * Наслідує GameGrid і перевизначає тільки метод {@code step()}.
 * Уся логіка (правила Конвея, тороїдальні межі, патерни, UI-методи)
 * залишається незмінною — від батьківського класу.
 *
 * Кожен виклик step() розподіляє рядки сітки між потоками через
 * {@link ForkJoinPool}, використовуючи {@link GenerationTask}.
 */
public class ParallelGameGrid extends GameGrid {

    private final ForkJoinPool pool;

    /**
     * Створює паралельну сітку з пулом потоків за замовчуванням
     * (кількість потоків = кількість логічних процесорів).
     */
    public ParallelGameGrid(int rows, int cols) {
        super(rows, cols);
        this.pool = new ForkJoinPool();
    }

    /**
     * Створює паралельну сітку з явно вказаною кількістю потоків.
     *
     * @param parallelism кількість потоків у пулі
     */
    public ParallelGameGrid(int rows, int cols, int parallelism) {
        super(rows, cols);
        this.pool = new ForkJoinPool(parallelism);
    }

    /**
     * Виконує один крок еволюції паралельно.
     *
     * Запускає {@link GenerationTask} для всієї сітки (від рядка 0 до rows).
     * Після завершення всіх підзадач атомічно отримує загальну популяцію,
     * міняє сітки місцями та інкрементує лічильник поколінь.
     */
    @Override
    public void step() {
        AtomicInteger populationAccum = new AtomicInteger(0);

        GenerationTask task = new GenerationTask(
            grid, nextGrid, rows, cols,
            0, rows,
            populationAccum
        );

        pool.invoke(task);   // блокує до повного завершення

        // Оновлюємо стан
        boolean[][] temp = grid;
        grid = nextGrid;
        nextGrid = temp;

        population = populationAccum.get();
        generation++;
    }

    /**
     * Коректно завершує роботу пулу потоків.
     * Викликати при закритті застосунку або після бенчмарку.
     */
    public void shutdown() {
        pool.shutdown();
    }

    /** Повертає кількість потоків у пулі */
    public int getParallelism() {
        return pool.getParallelism();
    }
}
