package com.archerygame.common;

import java.io.Serializable;
import java.awt.Point;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Target> targets;
    private List<Point> arrows;
    private List<PlayerInfo> players;
    private String winner;
    private boolean paused;
    private boolean gameRunning;

    public GameState(List<Target> targets, List<Point> arrows, List<PlayerInfo> players,
                     String winner, boolean paused, boolean gameRunning) {
        this.targets = targets;
        this.arrows = arrows;
        this.players = players;
        this.winner = winner;
        this.paused = paused;
        this.gameRunning = gameRunning;
    }

    public List<Target> getTargets() { return targets; }
    public List<Point> getArrows() { return arrows; }
    public List<PlayerInfo> getPlayers() { return players; }
    public String getWinner() { return winner; }
    public boolean isPaused() { return paused; }
    public boolean isGameRunning() { return gameRunning; }
}