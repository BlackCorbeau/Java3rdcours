package com.archerygame;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameController {
    private int score = 0;
    private int shots = 0;
    private volatile boolean gameRunning = false;
    private volatile boolean paused = false;

    // Список активных стрел (CopyOnWriteArrayList безопасен для перебора и удаления одновременно)
    private final List<Point> arrows = new CopyOnWriteArrayList<>();
    private static final int ARROW_SPEED = 20;

    private final List<Target> targets = new CopyOnWriteArrayList<>();
    private Timer gameTimer;
    private GamePanel gamePanel;
    private JLabel scoreLabel;
    private JLabel shotsLabel;

    public void setGamePanel(GamePanel panel) { this.gamePanel = panel; }
    public void setScoreLabel(JLabel label) { this.scoreLabel = label; }
    public void setShotsLabel(JLabel label) { this.shotsLabel = label; }

    public void initTargets(int panelHeight) {
        targets.clear();
        // Создаем мишени: X, Y, W, H, Очки, Путь, Скорость
        targets.add(new Target(580, 100, 55, 55, 1, "images/target1.png", 3));
        targets.add(new Target(700, 250, 45, 45, 3, "images/target2.png", -5));
    }

    public void startGame(int panelHeight) {
        stopGame();
        score = 0;
        shots = 0;
        arrows.clear();
        initTargets(panelHeight);
        updateUI();

        gameRunning = true;
        paused = false;

        // 16ms ≈ 60 FPS. Самый стабильный вариант для Swing.
        gameTimer = new Timer(16, e -> updateTick());
        gameTimer.start();
    }

    private void updateTick() {
        if (!gameRunning || paused) return;

        // Двигаем мишени
        for (Target t : targets) {
            t.move(gamePanel.getHeight());
        }

        // Двигаем и проверяем все стрелы
        for (Point arrow : arrows) {
            arrow.x += ARROW_SPEED;

            // Коллизия (попадание)
            Rectangle arrowRect = new Rectangle(arrow.x, arrow.y, 25, 4);
            for (Target t : targets) {
                if (arrowRect.intersects(t.getBounds())) {
                    score += t.getPoints();
                    arrows.remove(arrow);
                    updateUI();
                    break;
                }
            }

            // Удаляем, если улетела за экран
            if (arrow.x > gamePanel.getWidth()) {
                arrows.remove(arrow);
            }
        }

        gamePanel.repaint(); // Отрисовка кадра
    }

    public void shootArrow(int startX, int startY) {
        if (!gameRunning || paused) return;
        arrows.add(new Point(startX, startY));
        shots++;
        updateUI();
    }

    public void stopGame() {
        gameRunning = false;
        if (gameTimer != null) gameTimer.stop();
        arrows.clear();
        if (gamePanel != null) gamePanel.repaint();
    }

    public void togglePause() {
        if (gameRunning) paused = !paused;
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            if (scoreLabel != null) scoreLabel.setText(" Счёт: " + score);
            if (shotsLabel != null) shotsLabel.setText(" Выстрелы: " + shots);
        });
    }

    public List<Target> getTargets() { return targets; }
    public List<Point> getArrows() { return arrows; }
    public boolean isGameRunning() { return gameRunning; }
    public boolean isPaused() { return paused; }
}