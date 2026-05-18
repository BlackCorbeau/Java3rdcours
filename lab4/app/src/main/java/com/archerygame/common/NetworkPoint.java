package com.archerygame.common;

import java.io.Serializable;

public class NetworkPoint implements Serializable {
    private static final long serialVersionUID = 1L;
    public int x;
    public int y;

    public NetworkPoint() {
        this(0, 0);
    }

    public NetworkPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "NetworkPoint{" + x + "," + y + "}";
    }
}