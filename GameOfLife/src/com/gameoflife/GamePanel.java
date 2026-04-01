package com.gameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Панель для відображення та взаємодії з сіткою Гри Життя.
 * Працює як з GameGrid (послідовна), так і з ParallelGameGrid (паралельна).
 */
public class GamePanel extends JPanel implements MouseListener, MouseMotionListener {

    private final GameGrid grid;
    private int cellSize = 12;

    private Color colorBackground = new Color(15, 15, 25);
    private Color colorCell       = new Color(80, 220, 120);
    private Color colorGrid       = new Color(30, 30, 45);
    private Color colorCellGlow   = new Color(120, 255, 160);

    private boolean drawGrid    = true;
    private boolean lastMouseState = true;
    private int lastRow = -1;
    private int lastCol = -1;

    public GamePanel(GameGrid grid) {
        this.grid = grid;
        setBackground(colorBackground);
        addMouseListener(this);
        addMouseMotionListener(this);
        setPreferredSize(new Dimension(
            grid.getCols() * cellSize,
            grid.getRows() * cellSize
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelW = getWidth();
        int panelH = getHeight();
        int cols   = grid.getCols();
        int rows   = grid.getRows();

        int cw = panelW / cols;
        int ch = panelH / rows;
        int cs = Math.max(1, Math.min(cw, ch));

        int offsetX = (panelW - cols * cs) / 2;
        int offsetY = (panelH - rows * cs) / 2;

        g2d.setColor(colorBackground);
        g2d.fillRect(0, 0, panelW, panelH);

        if (drawGrid && cs >= 4) {
            g2d.setColor(colorGrid);
            for (int c = 0; c <= cols; c++)
                g2d.drawLine(offsetX + c * cs, offsetY, offsetX + c * cs, offsetY + rows * cs);
            for (int r = 0; r <= rows; r++)
                g2d.drawLine(offsetX, offsetY + r * cs, offsetX + cols * cs, offsetY + r * cs);
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid.getCell(r, c)) {
                    int x = offsetX + c * cs + 1;
                    int y = offsetY + r * cs + 1;
                    int w = cs - 1;
                    int h = cs - 1;
                    if (cs >= 6) {
                        GradientPaint gp = new GradientPaint(x, y, colorCellGlow, x + w, y + h, colorCell);
                        g2d.setPaint(gp);
                    } else {
                        g2d.setColor(colorCell);
                    }
                    if (cs >= 4) g2d.fillRoundRect(x, y, w, h, 3, 3);
                    else         g2d.fillRect(x, y, w, h);
                }
            }
        }
    }

    private int[] getCellFromPoint(Point p) {
        int panelW = getWidth();
        int panelH = getHeight();
        int cols   = grid.getCols();
        int rows   = grid.getRows();
        int cw = panelW / cols;
        int ch = panelH / rows;
        int cs = Math.max(1, Math.min(cw, ch));
        int offsetX = (panelW - cols * cs) / 2;
        int offsetY = (panelH - rows * cs) / 2;
        int col = (p.x - offsetX) / cs;
        int row = (p.y - offsetY) / cs;
        return new int[]{row, col};
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int[] cell = getCellFromPoint(e.getPoint());
        int row = cell[0], col = cell[1];
        if (row >= 0 && row < grid.getRows() && col >= 0 && col < grid.getCols()) {
            grid.toggleCell(row, col);
            lastMouseState = grid.getCell(row, col);
            lastRow = row;
            lastCol = col;
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int[] cell = getCellFromPoint(e.getPoint());
        int row = cell[0], col = cell[1];
        if (row >= 0 && row < grid.getRows() && col >= 0 && col < grid.getCols()) {
            if (row != lastRow || col != lastCol) {
                grid.setCell(row, col, lastMouseState);
                lastRow = row;
                lastCol = col;
                repaint();
            }
        }
    }

    public void setCellSize(int size)      { this.cellSize = size; }
    public void setDrawGrid(boolean draw)  { this.drawGrid = draw; repaint(); }

    public void setColorScheme(ColorScheme scheme) {
        switch (scheme) {
            case GREEN -> {
                colorBackground = new Color(15, 15, 25);
                colorCell       = new Color(80, 220, 120);
                colorCellGlow   = new Color(120, 255, 160);
                colorGrid       = new Color(30, 30, 45);
            }
            case BLUE -> {
                colorBackground = new Color(5, 10, 30);
                colorCell       = new Color(60, 160, 255);
                colorCellGlow   = new Color(100, 200, 255);
                colorGrid       = new Color(20, 30, 60);
            }
            case RED -> {
                colorBackground = new Color(20, 5, 5);
                colorCell       = new Color(255, 80, 60);
                colorCellGlow   = new Color(255, 140, 100);
                colorGrid       = new Color(45, 15, 15);
            }
            case WHITE -> {
                colorBackground = new Color(240, 240, 245);
                colorCell       = new Color(30, 30, 40);
                colorCellGlow   = new Color(60, 60, 80);
                colorGrid       = new Color(200, 200, 210);
            }
        }
        setBackground(colorBackground);
        repaint();
    }

    public enum ColorScheme { GREEN, BLUE, RED, WHITE }

    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseMoved(MouseEvent e)    {}
}
