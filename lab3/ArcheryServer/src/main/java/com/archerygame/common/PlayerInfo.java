package com.archerygame.common;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 2L; // изменён для совместимости
    private String name;
    private int score;
    private int shots;
    private boolean ready;
    private int totalWins; // новое поле – количество побед (из БД)

    public PlayerInfo(String name) {
        this.name = name;
        this.score = 0;
        this.shots = 0;
        this.ready = false;
        this.totalWins = 0;
    }

    public String getName() { return name; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getShots() { return shots; }
    public void setShots(int shots) { this.shots = shots; }
    public boolean isReady() { return ready; }
    public void setReady(boolean ready) { this.ready = ready; }
    public int getTotalWins() { return totalWins; }
    public void setTotalWins(int totalWins) { this.totalWins = totalWins; }
}