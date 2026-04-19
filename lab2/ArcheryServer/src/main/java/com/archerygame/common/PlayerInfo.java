package com.archerygame.common;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private int score;
    private int shots;
    private boolean ready;

    public PlayerInfo(String name) {
        this.name = name;
        this.score = 0;
        this.shots = 0;
        this.ready = false;
    }

    public String getName() { return name; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getShots() { return shots; }
    public void setShots(int shots) { this.shots = shots; }
    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }
}