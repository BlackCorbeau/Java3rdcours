package com.archerygame.client;

import com.archerygame.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

@Component
public class GamePanel extends JPanel {
    private final GameClient client;
    private Image archerImage;
    private int archerY = 200;
    private static final int ARCHER_WIDTH = 65;
    private static final int ARCHER_HEIGHT = 85;
    private static final int ARCHER_X = 25;

    @Autowired
    public GamePanel(GameClient client) {
        this.client = client;
        setPreferredSize(new Dimension(800, 500));
        setDoubleBuffered(true);
        loadArcherSprite();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                GameState state = client.getCurrentState();
                if (state != null && state.isGameRunning() && !state.isPaused()) {
                    client.sendShoot(ARCHER_X + ARCHER_WIDTH - 10, archerY + ARCHER_HEIGHT / 2);
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int newY = e.getY() - ARCHER_HEIGHT / 2;
                archerY = Math.max(5, Math.min(newY, getHeight() - ARCHER_HEIGHT - 45));
                repaint();
            }
        });
    }

    private void loadArcherSprite() {
        try {
            java.net.URL url = getClass().getClassLoader().getResource("images/archer.png");
            if (url != null) archerImage = new ImageIcon(url).getImage();
        } catch (Exception e) { System.err.println("Лучник не загружен, используется фигура."); }
    }

    public void updateState(GameState state) { repaint(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Фон (небо)
        g2d.setPaint(new GradientPaint(0, 0, new Color(135, 206, 250), 0, getHeight(), Color.WHITE));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        // Земля
        g2d.setColor(new Color(50, 160, 50));
        g2d.fillRoundRect(-10, getHeight() - 40, getWidth() + 20, 60, 30, 30);

        // Лучник
        if (archerImage != null) {
            g2d.drawImage(archerImage, ARCHER_X, archerY, ARCHER_WIDTH, ARCHER_HEIGHT, null);
        } else {
            g2d.setColor(new Color(44, 62, 80));
            g2d.fillRoundRect(ARCHER_X, archerY, 35, ARCHER_HEIGHT, 15, 15);
            g2d.fillOval(ARCHER_X + 2, archerY - 25, 30, 30);
        }

        GameState state = client.getCurrentState();
        if (state == null) return;

        // Стрелы
        g2d.setColor(new Color(101, 67, 33));
        g2d.setStroke(new BasicStroke(3f));
        for (Point p : state.getArrows()) {
            g2d.drawLine(p.x, p.y, p.x + 22, p.y);
            g2d.setColor(Color.RED);
            g2d.fillOval(p.x + 20, p.y - 2, 6, 5);
            g2d.setColor(new Color(101, 67, 33));
        }

        // Мишени
        for (Target t : state.getTargets()) {
            t.draw(g2d);
        }

        // Пауза
        if (state.isPaused()) {
            g2d.setColor(new Color(0, 0, 0, 120));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString("ПАУЗА", getWidth()/2 - 65, getHeight()/2);
        }

        // Победитель
        if (state.getWinner() != null) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            String msg = "Победитель: " + state.getWinner();
            g2d.drawString(msg, getWidth()/2 - 100, getHeight()/2);
        }
    }
}