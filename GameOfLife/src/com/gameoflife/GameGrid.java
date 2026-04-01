package com.gameoflife;

import java.util.Random;

/**
 * Послідовна реалізація логіки Гри Життя Конвея.
 * Правила:
 * 1. Жива клітина з 2-3 сусідами виживає.
 * 2. Мертва клітина з рівно 3 сусідами народжується.
 * 3. Всі інші клітини вмирають або залишаються мертвими.
 */
public class GameGrid {

    protected final int rows;
    protected final int cols;
    protected boolean[][] grid;
    protected boolean[][] nextGrid;
    protected int generation;
    protected int population;

    public GameGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new boolean[rows][cols];
        this.nextGrid = new boolean[rows][cols];
        this.generation = 0;
        this.population = 0;
    }

    /** Заповнює сітку випадковим чином */
    public void randomize(double density) {
        Random random = new Random();
        generation = 0;
        population = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = random.nextDouble() < density;
                if (grid[r][c]) population++;
            }
        }
    }

    /** Очищає сітку */
    public void clear() {
        generation = 0;
        population = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = false;
            }
        }
    }

    /** Виконує один крок еволюції (послідовно) */
    public void step() {
        population = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int neighbors = countNeighbors(r, c);
                boolean alive = grid[r][c];
                nextGrid[r][c] = alive ? (neighbors == 2 || neighbors == 3) : (neighbors == 3);
                if (nextGrid[r][c]) population++;
            }
        }
        boolean[][] temp = grid;
        grid = nextGrid;
        nextGrid = temp;
        generation++;
    }

    /** Підраховує живих сусідів клітини (з тороїдальними межами) */
    protected int countNeighbors(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = (row + dr + rows) % rows;
                int c = (col + dc + cols) % cols;
                if (grid[r][c]) count++;
            }
        }
        return count;
    }

    /** Перемикає стан клітини */
    public void toggleCell(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            grid[row][col] = !grid[row][col];
            population += grid[row][col] ? 1 : -1;
        }
    }

    /** Встановлює стан клітини */
    public void setCell(int row, int col, boolean alive) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            if (grid[row][col] != alive) {
                grid[row][col] = alive;
                population += alive ? 1 : -1;
            }
        }
    }

    /** Вставляє патерн — Глайдер */
    public void insertGlider(int startRow, int startCol) {
        int[][] pattern = {
            {0, 1, 0},
            {0, 0, 1},
            {1, 1, 1}
        };
        insertPattern(pattern, startRow, startCol);
    }

    /** Вставляє патерн — Пульсар */
    public void insertPulsar(int startRow, int startCol) {
        int[][] pattern = {
            {0,0,1,1,1,0,0,0,1,1,1,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0},
            {1,0,0,0,0,1,0,1,0,0,0,0,1},
            {1,0,0,0,0,1,0,1,0,0,0,0,1},
            {1,0,0,0,0,1,0,1,0,0,0,0,1},
            {0,0,1,1,1,0,0,0,1,1,1,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,1,1,1,0,0,0,1,1,1,0,0},
            {1,0,0,0,0,1,0,1,0,0,0,0,1},
            {1,0,0,0,0,1,0,1,0,0,0,0,1},
            {1,0,0,0,0,1,0,1,0,0,0,0,1},
            {0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,1,1,1,0,0,0,1,1,1,0,0}
        };
        insertPattern(pattern, startRow, startCol);
    }

    /** Вставляє патерн — Гармата Госпера */
    public void insertGosperGun(int startRow, int startCol) {
        int[][] pattern = {
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1},
            {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,1,1},
            {1,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {1,1,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1,1,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
        };
        insertPattern(pattern, startRow, startCol);
    }

    protected void insertPattern(int[][] pattern, int startRow, int startCol) {
        for (int r = 0; r < pattern.length; r++) {
            for (int c = 0; c < pattern[r].length; c++) {
                int gr = startRow + r;
                int gc = startCol + c;
                if (gr >= 0 && gr < rows && gc >= 0 && gc < cols) {
                    boolean newVal = pattern[r][c] == 1;
                    if (grid[gr][gc] != newVal) {
                        grid[gr][gc] = newVal;
                        population += newVal ? 1 : -1;
                    }
                }
            }
        }
    }

    public boolean getCell(int row, int col) { return grid[row][col]; }
    public int getRows()       { return rows; }
    public int getCols()       { return cols; }
    public int getGeneration() { return generation; }
    public int getPopulation() { return population; }
}
