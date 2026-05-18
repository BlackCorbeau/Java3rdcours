package com.archerygame.common;

import java.io.Serializable;

public class Target implements Serializable {
    private static final long serialVersionUID = 2L;
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
        if (y < 10) {
            y = 10;
            vy = -vy;
        }
        if (y > panelHeight - height - 50) {
            y = panelHeight - height - 50;
            vy = -vy;
        }
    }

    public NetworkRectangle getBounds() {
        return new NetworkRectangle(x, y, width, height);
    }

    public int getPoints() { return points; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}