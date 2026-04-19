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

        try {
            java.net.URL url = getClass().getClassLoader().getResource(imagePath);
            if (url != null) image = new ImageIcon(url).getImage();
        } catch (Exception e) {}
    }

    public void move(int panelHeight) {
        y += vy;
        if (y < 10 || y > panelHeight - height - 50) vy = -vy;
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public int getPoints() { return points; }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        } else {
            // Если нет картинки — рисуем классическую мишень
            g2d.setColor(Color.RED);
            g2d.fillOval(x, y, width, height);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x + width/4, y + height/4, width/2, height/2);
            g2d.setColor(Color.RED);
            g2d.fillOval(x + width/3 + 2, y + height/3 + 2, width/4, height/4);
        }
    }
}