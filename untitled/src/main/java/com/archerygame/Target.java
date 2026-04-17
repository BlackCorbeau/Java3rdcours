package com.archerygame;

import javax.swing.*;
import java.awt.*;

public class Target {
    private final int x;
    private final int width;
    private final int height;
    private final int points;
    private int y;
    private int vy;
    private Image image;

    public Target(int x, int y, int width, int height, int points, String imagePath, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.points = points;
        this.vy = speed;
        loadImage(imagePath);
    }

    private void loadImage(String path) {
        try {
            java.net.URL imgURL = getClass().getClassLoader().getResource(path);
            if (imgURL != null) image = new ImageIcon(imgURL).getImage();
        } catch (Exception e) { image = null; }
    }

    public void move(int panelHeight) {
        y += vy;
        // Отскок от краев
        if (y < 20 || y > panelHeight - height - 50) {
            vy = -vy;
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public int getPoints() { return points; }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        } else {
            g2d.setColor(points == 1 ? Color.BLUE : Color.RED);
            g2d.fillOval(x, y, width, height);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, width, height);
        }
    }
}