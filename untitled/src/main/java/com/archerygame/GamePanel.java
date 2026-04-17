package com.archerygame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    private final GameController controller;
    private Image archerImage;
    private Image arrowImage;
    private volatile int archerY = 200;
    private static final int ARCHER_WIDTH = 60;
    private static final int ARCHER_HEIGHT = 80;
    private static final int ARCHER_X = 20;

    public GamePanel(GameController controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(800, 500));
        setBackground(new Color(220, 240, 255));
        setDoubleBuffered(true);
        loadImages();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int startX = ARCHER_X + ARCHER_WIDTH;
                int startY = archerY + ARCHER_HEIGHT / 2;
                controller.shootArrow(startX, startY, getWidth(), getHeight());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int newY = e.getY() - ARCHER_HEIGHT / 2;
                newY = Math.max(10, Math.min(newY, getHeight() - ARCHER_HEIGHT - 10));
                archerY = newY;
            }
        });

        controller.setGamePanel(this);
    }

    private void loadImages() {
        try {
            java.net.URL archerUrl = getClass().getClassLoader().getResource("images/archer.png");
            if (archerUrl != null) archerImage = new ImageIcon(archerUrl).getImage();
            java.net.URL arrowUrl = getClass().getClassLoader().getResource("images/arrow.png");
            if (arrowUrl != null) arrowImage = new ImageIcon(arrowUrl).getImage();
        } catch (Exception e) {
            // нет картинок – рисуем примитивы
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Фон
        g.setColor(new Color(220, 240, 255));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(34, 139, 34));
        g.fillRect(0, getHeight() - 30, getWidth(), 30);

        // Лучник
        if (archerImage != null) {
            g.drawImage(archerImage, ARCHER_X, archerY, ARCHER_WIDTH, ARCHER_HEIGHT, null);
        } else {
            g.setColor(Color.ORANGE);
            g.fillRect(ARCHER_X, archerY, ARCHER_WIDTH, ARCHER_HEIGHT);
            g.fillOval(ARCHER_X + 20, archerY - 20, 20, 20);
        }

        // Мишени
        synchronized (controller.getLock()) {
            for (Target t : controller.getTargets()) {
                t.draw(g);
            }
        }

        // Стрела
        if (controller.isArrowActive()) {
            int ax = controller.getArrowX();
            int ay = controller.getArrowY();
            if (ax > 0 && ay > 0) {
                if (arrowImage != null) {
                    g.drawImage(arrowImage, ax, ay, 20, 20, null);
                } else {
                    g.setColor(Color.BLACK);
                    g.fillOval(ax, ay, 12, 12);
                    g.drawLine(ax + 6, ay + 6, ax + 18, ay + 6);
                }
            }
        }

        // Сообщения
        if (controller.isPaused() && controller.isGameRunning()) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("ПАУЗА", getWidth() / 2 - 60, getHeight() / 2);
        }
        if (!controller.isGameRunning()) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Нажмите 'Старт'", getWidth() / 2 - 100, getHeight() / 2);
        }
    }
}