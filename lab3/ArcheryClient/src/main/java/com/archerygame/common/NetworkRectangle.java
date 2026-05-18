package com.archerygame.common;

import java.io.Serializable;

public class NetworkRectangle implements Serializable {
    private static final long serialVersionUID = 1L;
    public int x, y, width, height;

    public NetworkRectangle() {
        this(0, 0, 0, 0);
    }

    public NetworkRectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean intersects(NetworkRectangle other) {
        return x < other.x + other.width &&
                x + width > other.x &&
                y < other.y + other.height &&
                y + height > other.y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}