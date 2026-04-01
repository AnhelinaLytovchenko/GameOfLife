package com.gameoflife;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Паралельна задача (Fork/Join) для обчислення одного діапазону рядків сітки.
 *
 * Якщо кількість рядків у підзадачі перевищує поріг (THRESHOLD), задача
 * розбивається на дві менші підзадачі (fork). Інакше — виконується
 * послідовно (base case).
 *
 * Результат (кількість живих клітин у діапазоні) накопичується в
 * спільному AtomicInteger, переданому ззовні.
 */
public class GenerationTask extends RecursiveAction {

    /** Мінімальна кількість рядків, яку обробляє одна задача без розбиття */
    private static final int THRESHOLD = 8;

    private final boolean[][] currentGrid;
    private final boolean[][] nextGrid;
    private final int rows;
    private final int cols;
    private final int fromRow;
    private final int toRow;       // exclusive
    private final AtomicInteger populationAccum;

    /**
     * @param currentGrid    поточний стан сітки (тільки читання)
     * @param nextGrid       наступний стан сітки (запис)
     * @param rows           загальна кількість рядків
     * @param cols           загальна кількість стовпців
     * @param fromRow        перший рядок діапазону (inclusive)
     * @param toRow          останній рядок діапазону (exclusive)
     * @param populationAccum атомарний лічильник для підрахунку популяції
     */
    public GenerationTask(boolean[][] currentGrid, boolean[][] nextGrid,
                          int rows, int cols,
                          int fromRow, int toRow,
                          AtomicInteger populationAccum) {
        this.currentGrid = currentGrid;
        this.nextGrid = nextGrid;
        this.rows = rows;
        this.cols = cols;
        this.fromRow = fromRow;
        this.toRow = toRow;
        this.populationAccum = populationAccum;
    }

    @Override
    protected void compute() {
        int length = toRow - fromRow;

        if (length <= THRESHOLD) {
            // Base case: обробляємо рядки послідовно
            computeDirectly();
        } else {
            // Розбиваємо діапазон навпіл
            int mid = fromRow + length / 2;
            GenerationTask left  = new GenerationTask(currentGrid, nextGrid, rows, cols,
                                                       fromRow, mid, populationAccum);
            GenerationTask right = new GenerationTask(currentGrid, nextGrid, rows, cols,
                                                       mid, toRow, populationAccum);
            // invokeAll виконує обидві підзадачі паралельно
            invokeAll(left, right);
        }
    }

    /** Послідовна обробка рядків від fromRow до toRow (exclusive) */
    private void computeDirectly() {
        int localPop = 0;
        for (int r = fromRow; r < toRow; r++) {
            for (int c = 0; c < cols; c++) {
                int neighbors = countNeighbors(r, c);
                boolean alive = currentGrid[r][c];
                boolean next  = alive ? (neighbors == 2 || neighbors == 3) : (neighbors == 3);
                nextGrid[r][c] = next;
                if (next) localPop++;
            }
        }
        // Атомарне додавання локального результату до загального лічильника
        populationAccum.addAndGet(localPop);
    }

    /** Підраховує живих сусідів з тороїдальними межами */
    private int countNeighbors(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = (row + dr + rows) % rows;
                int c = (col + dc + cols) % cols;
                if (currentGrid[r][c]) count++;
            }
        }
        return count;
    }
}
