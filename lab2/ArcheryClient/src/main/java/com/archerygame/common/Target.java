package com.archerygame.common;

import java.awt.*;
import java.io.Serializable;

public class Target implements Serializable {
    private static final long serialVersionUID = 1L;
    private int x, y, width, height, points, vy;

    public Target(int x, int y, int width, int height, int points, int vy) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.points = points;
        this.vy = vy;
    }

    public void move(int panelHeight) {
        y += vy;
        // Верхняя граница (как в первой лабе – 10 пикселей от верха)
        if (y < 10) {
            y = 10;
            vy = -vy;
        }
        // Нижняя граница (не заезжаем на землю, отступ 50)
        if (y > panelHeight - height - 50) {
            y = panelHeight - height - 50;
            vy = -vy;
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public int getPoints() { return points; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.fillOval(x, y, width, height);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + width/4, y + height/4, width/2, height/2);
        g2d.setColor(Color.RED);
        g2d.fillOval(x + width/3 + 2, y + height/3 + 2, width/4, height/4);
    }
}