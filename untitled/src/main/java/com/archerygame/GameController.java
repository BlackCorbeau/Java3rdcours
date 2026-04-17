package com.archerygame;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    private int score = 0;
    private int shots = 0;
    private volatile boolean gameRunning = false;
    private volatile boolean paused = false;

    private volatile int arrowX = -100;
    private volatile int arrowY = -100;
    private volatile boolean arrowActive = false;
    private volatile boolean arrowHit = false;
    private volatile int arrowPoints = 0;

    private final List<Target> targets = new ArrayList<>();
    private Thread gameLoopThread;
    private Arrow arrowThread;
    private final Object lock = new Object();

    private GamePanel gamePanel;
    private JLabel scoreLabel;
    private JLabel shotsLabel;

    public void setGamePanel(GamePanel panel) { this.gamePanel = panel; }
    public GamePanel getGamePanel() { return gamePanel; }
    public void setScoreLabel(JLabel label) { this.scoreLabel = label; }
    public void setShotsLabel(JLabel label) { this.shotsLabel = label; }

    public void initTargets(int panelHeight) {
        targets.clear();
        int midY = panelHeight / 2 - 30;
        Target near = new Target(550, midY, 60, 60, 1, "images/target1.png");
        near.setVelocity(2);
        Target far = new Target(680, midY, 60, 60, 2, "images/target2.png");
        far.setVelocity(-3);
        targets.add(near);
        targets.add(far);
    }

    public void startGame(int panelHeight) {
        stopGame();  // полная остановка предыдущей игры
        score = 0;
        shots = 0;
        arrowActive = false;
        arrowHit = false;
        arrowX = -100;
        arrowY = -100;
        updateUI();

        initTargets(panelHeight);

        gameRunning = true;
        paused = false;
        startGameLoop();

        // Принудительная перерисовка, чтобы убрать "серый" экран
        if (gamePanel != null) {
            SwingUtilities.invokeLater(() -> gamePanel.repaint());
        }
    }

    public void stopGame() {
        gameRunning = false;
        paused = false;
        if (gameLoopThread != null) {
            gameLoopThread.interrupt();
            try { gameLoopThread.join(50); } catch (InterruptedException ignored) {}
            gameLoopThread = null;
        }
        if (arrowThread != null && arrowThread.isAlive()) {
            arrowThread.interrupt();
            try { arrowThread.join(50); } catch (InterruptedException ignored) {}
            arrowThread = null;
        }
        arrowActive = false;
        arrowHit = false;
        arrowX = -100;
        arrowY = -100;
        if (gamePanel != null) gamePanel.repaint();
    }

    public void togglePause() {
        if (!gameRunning) return;
        paused = !paused;
    }

    public void shootArrow(int startX, int startY, int panelWidth, int panelHeight) {
        if (!gameRunning || paused || arrowActive) return;
        arrowX = startX;
        arrowY = startY;
        arrowActive = true;
        arrowHit = false;
        shots++;
        updateUI();

        arrowThread = new Arrow(startX, startY, this, panelWidth);
        arrowThread.start();
    }

    public void moveArrow(int newX) {
        if (!arrowActive) return;
        arrowX = newX;
        synchronized (lock) {
            for (Target t : targets) {
                if (new Rectangle(arrowX, arrowY, 20, 20).intersects(t.getBounds())) {
                    arrowHit = true;
                    arrowPoints = t.getPoints();
                    break;
                }
            }
        }
    }

    public void finishArrow() {
        if (arrowHit) {
            score += arrowPoints;
            updateUI();
        }
        arrowActive = false;
        arrowHit = false;
        arrowX = -100;
        arrowY = -100;
        arrowThread = null;
        // Обновить экран, чтобы стрела исчезла
        if (gamePanel != null) gamePanel.repaint();
    }

    public boolean isArrowHit() {
        return arrowHit;
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            if (scoreLabel != null) scoreLabel.setText("Счёт: " + score);
            if (shotsLabel != null) shotsLabel.setText("Выстрелы: " + shots);
        });
    }

    private void startGameLoop() {
        gameLoopThread = new Thread(() -> {
            while (gameRunning) {
                long frameStart = System.nanoTime();
                if (!paused) {
                    synchronized (lock) {
                        for (Target t : targets) {
                            t.move(gamePanel.getHeight());
                        }
                    }
                    SwingUtilities.invokeLater(() -> gamePanel.repaint());
                }
                long frameTime = System.nanoTime() - frameStart;
                long sleepMs = 16 - (frameTime / 1_000_000);
                if (sleepMs > 0) {
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        gameLoopThread.start();
    }

    public List<Target> getTargets() { return targets; }
    public boolean isGameRunning() { return gameRunning; }
    public boolean isPaused() { return paused; }
    public boolean isArrowActive() { return arrowActive; }
    public int getArrowX() { return arrowX; }
    public int getArrowY() { return arrowY; }
    public Object getLock() { return lock; }
}