package com.archerygame;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameController {
    private int score = 0;
    private int shots = 0;
    private volatile boolean gameRunning = false;
    private volatile boolean paused = false;

    private final List<Point> arrows = new CopyOnWriteArrayList<>();
    private final List<Target> targets = new CopyOnWriteArrayList<>();

    private Thread gameThread;

    private GamePanel gamePanel;
    private JLabel scoreLabel;
    private JLabel shotsLabel;

    private static final int ARROW_SPEED = 20;
    private static final int TARGET_FPS = 60;
    private static final long FRAME_TIME_MS = 1000 / TARGET_FPS; // ≈16.67 мс

    public void setGamePanel(GamePanel panel) {
        this.gamePanel = panel;
    }

    public void setScoreLabel(JLabel label) {
        this.scoreLabel = label;
    }

    public void setShotsLabel(JLabel label) {
        this.shotsLabel = label;
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

        gameThread = new Thread(this::gameLoop);
        gameThread.setDaemon(true);
        gameThread.start();
    }

    public void stopGame() {
        gameRunning = false;
        if (gameThread != null) {
            gameThread.interrupt();
            try {
                gameThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            gameThread = null;
        }
        arrows.clear();
        targets.clear();
        if (gamePanel != null) {
            gamePanel.repaint();
        }
        updateUI();
    }

    public void togglePause() {
        if (gameRunning) {
            paused = !paused;
        }
    }

    public void shootArrow(int startX, int startY) {
        if (!gameRunning || paused) return;
        arrows.add(new Point(startX, startY));
        shots++;
        updateUI();
    }

    private void initTargets(int panelHeight) {
        targets.clear();
        targets.add(new Target(580, 100, 55, 55, 1, "images/target1.png", 3));
        targets.add(new Target(700, 250, 45, 45, 3, "images/target2.png", -5));
    }

    private void gameLoop() {
        long lastTime = System.nanoTime();
        while (gameRunning) {
            long now = System.nanoTime();
            long deltaNs = now - lastTime;

            if (deltaNs >= FRAME_TIME_MS * 1_000_000) {
                lastTime = now;

                if (!paused) {
                    updateGameLogic();
                }

                SwingUtilities.invokeLater(() -> {
                    if (gamePanel != null) gamePanel.repaint();
                });
            } else {
                Thread.yield();
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void updateGameLogic() {
        if (gamePanel == null) return;
        int panelHeight = gamePanel.getHeight();

        for (Target t : targets) {
            t.move(panelHeight);
        }

        for (Point arrow : arrows) {
            arrow.x += ARROW_SPEED;

            Rectangle arrowRect = new Rectangle(arrow.x, arrow.y, 25, 4);
            boolean hit = false;
            for (Target t : targets) {
                if (arrowRect.intersects(t.getBounds())) {
                    score += t.getPoints();
                    arrows.remove(arrow);
                    updateUI();
                    hit = true;
                    break;
                }
            }
            if (hit) continue;

            if (arrow.x > gamePanel.getWidth()) {
                arrows.remove(arrow);
            }
        }
    }

    private void updateUI() {
        SwingUtilities.invokeLater(() -> {
            if (scoreLabel != null) scoreLabel.setText(" Счёт: " + score);
            if (shotsLabel != null) shotsLabel.setText(" Выстрелы: " + shots);
        });
    }

    public List<Target> getTargets() {
        return targets;
    }

    public List<Point> getArrows() {
        return arrows;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public boolean isPaused() {
        return paused;
    }
}