package com.archerygame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel {
    private final GameController controller;
    private Image archerImage;
    private volatile int archerY = 200;

    private static final int ARCHER_WIDTH = 60;
    private static final int ARCHER_HEIGHT = 80;
    private static final int ARCHER_X = 20;

    public GamePanel(GameController controller) {
        this.controller = controller;
        setPreferredSize(new Dimension(800, 500));
        setDoubleBuffered(true);
        loadImages();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Прямой вызов логики контроллера
                controller.shootArrow(ARCHER_X + ARCHER_WIDTH, archerY + ARCHER_HEIGHT / 2);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int newY = e.getY() - ARCHER_HEIGHT / 2;
                // Ограничиваем движение лучника границами экрана
                archerY = Math.max(10, Math.min(newY, getHeight() - ARCHER_HEIGHT - 40));
                repaint();
            }
        });

        controller.setGamePanel(this);
    }

    private void loadImages() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("images/archer.png");
            if (url != null) archerImage = new ImageIcon(url).getImage();
        } catch (Exception e) { }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Фон: градиентное небо
        GradientPaint sky = new GradientPaint(0, 0, new Color(135, 206, 250), 0, getHeight(), new Color(240, 248, 255));
        g2d.setPaint(sky);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Трава
        g2d.setColor(new Color(34, 139, 34));
        g2d.fillRoundRect(-10, getHeight() - 40, getWidth() + 20, 100, 40, 40);

        // Отрисовка лучника
        if (archerImage != null) {
            g2d.drawImage(archerImage, ARCHER_X, archerY, ARCHER_WIDTH, ARCHER_HEIGHT, null);
        } else {
            g2d.setColor(new Color(44, 62, 80));
            g2d.fillRoundRect(ARCHER_X, archerY, ARCHER_WIDTH - 20, ARCHER_HEIGHT, 15, 15);
        }

        // Отрисовка мишеней
        synchronized (controller.getLock()) {
            for (Target t : controller.getTargets()) {
                t.draw(g2d);
            }
        }

        // Отрисовка стрелы
        if (controller.isArrowActive()) {
            g2d.setColor(new Color(101, 67, 33));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(controller.getArrowX(), controller.getArrowY(), controller.getArrowX() + 25, controller.getArrowY());
        }

        drawStatusMessages(g2d);
    }

    private void drawStatusMessages(Graphics2D g2d) {
        if (controller.isPaused()) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString("ПАУЗА", getWidth() / 2 - 70, getHeight() / 2);
        }
        if (!controller.isGameRunning()) {
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString("Нажмите 'Старт'", getWidth() / 2 - 60, getHeight() / 2);
        }
    }
}