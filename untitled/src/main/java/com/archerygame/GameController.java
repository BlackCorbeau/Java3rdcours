package com.archerygame;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    private int score = 0;
    private int shots = 0;
    private volatile boolean gameRunning = false;
    private volatile boolean paused = false;

    private int arrowX = -100;
    private int arrowY = -100;
    private boolean arrowActive = false;
    private static final int ARROW_SPEED = 18;

    private final List<Target> targets = new ArrayList<>();
    private Timer gameTimer;
    private final Object lock = new Object();

    private GamePanel gamePanel;
    private JLabel scoreLabel;
    private JLabel shotsLabel;

    public void setGamePanel(GamePanel panel) { this.gamePanel = panel; }
    public void setScoreLabel(JLabel label) { this.scoreLabel = label; }
    public void setShotsLabel(JLabel label) { this.shotsLabel = label; }

    public void initTargets(int panelHeight) {
        targets.clear();
        // Разные мишени: позиция, размер, очки, путь к фото, скорость
        targets.add(new Target(550, 100, 60, 60, 1, "images/target1.png", 3));
        targets.add(new Target(680, 200, 45, 45, 2, "images/target2.png", -5));
    }

    public void startGame(int panelHeight) {
        stopGame();
        score = 0;
        shots = 0;
        arrowActive = false;
        initTargets(panelHeight);
        updateUI();

        gameRunning = true;
        paused = false;

        // Главный цикл обновлений (EDT-friendly)
        gameTimer = new Timer(16, e -> updateGameStep());
        gameTimer.start();
    }

    private void updateGameStep() {
        if (!gameRunning || paused) return;

        synchronized (lock) {
            for (Target t : targets) {
                t.move(gamePanel.getHeight());
            }

            if (arrowActive) {
                arrowX += ARROW_SPEED;
                checkCollisions();
                if (arrowX > gamePanel.getWidth()) {
                    arrowActive = false;
                }
            }
        }
        gamePanel.repaint();
    }

    private void checkCollisions() {
        Rectangle arrowRect = new Rectangle(arrowX, arrowY, 25, 5);
        for (Target t : targets) {
            if (arrowRect.intersects(t.getBounds())) {
                score += t.getPoints();
                arrowActive = false;
                updateUI();
                break;
            }
        }
    }

    public void shootArrow(int startX, int startY) {
        if (!gameRunning || paused || arrowActive) return;
        arrowX = startX;
        arrowY = startY;
        arrowActive = true;
        shots++;
        updateUI();
    }

    public void stopGame() {
        gameRunning = false;
        if (gameTimer != null) gameTimer.stop();
        arrowActive = false;
        if (gamePanel != null) gamePanel.repaint();
    }

    public void togglePause() {
        if (!gameRunning) return;
        paused = !paused;
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            if (scoreLabel != null) scoreLabel.setText("Счёт: " + score);
            if (shotsLabel != null) shotsLabel.setText("Выстрелы: " + shots);
        });
    }

    public List<Target> getTargets() { return targets; }
    public boolean isGameRunning() { return gameRunning; }
    public boolean isPaused() { return paused; }
    public boolean isArrowActive() { return arrowActive; }
    public int getArrowX() { return arrowX; }
    public int getArrowY() { return arrowY; }
    public Object getLock() { return lock; }
}