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

    public Target(int x, int y, int width, int height, int points, String imagePath) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.points = points;
        this.vy = 2;
        loadImage(imagePath);
    }

    private void loadImage(String path) {
        try {
            java.net.URL imgURL = getClass().getClassLoader().getResource(path);
            if (imgURL != null) image = new ImageIcon(imgURL).getImage();
        } catch (Exception e) {
            image = null;
        }
    }

    public void setVelocity(int vy) { this.vy = vy; }
    public void setY(int y) { this.y = y; }

    public void move(int panelHeight) {
        y += vy;
        int minY = 30;
        int maxY = panelHeight - height - 30;
        if (y < minY) {
            y = minY;
            vy = -vy;
        }
        if (y > maxY) {
            y = maxY;
            vy = -vy;
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public int getPoints() { return points; }

    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            g.setColor(points == 1 ? new Color(34, 139, 34) : new Color(178, 34, 34));
            g.fillOval(x, y, width, height);
            g.setColor(Color.BLACK);
            g.drawOval(x, y, width, height);
            g.drawString(String.valueOf(points), x + width/2 - 5, y + height/2 + 5);
        }
    }
}