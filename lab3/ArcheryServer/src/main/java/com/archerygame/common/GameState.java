package com.archerygame.common;

import java.io.Serializable;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 3L; // увеличили версию
    private List<Target> targets;
    private List<NetworkPoint> arrows;
    private List<PlayerInfo> players;
    private String winner;
    private boolean paused;
    private boolean gameRunning;
    private List<PlayerInfo> leaderboard;

    public GameState(List<Target> targets, List<NetworkPoint> arrows, List<PlayerInfo> players,
                     String winner, boolean paused, boolean gameRunning,
                     List<PlayerInfo> leaderboard) {
        this.targets = targets;
        this.arrows = arrows;
        this.players = players;
        this.winner = winner;
        this.paused = paused;
        this.gameRunning = gameRunning;
        this.leaderboard = leaderboard;
    }

    public List<Target> getTargets() { return targets; }
    public List<NetworkPoint> getArrows() { return arrows; }
    public List<PlayerInfo> getPlayers() { return players; }
    public String getWinner() { return winner; }
    public boolean isPaused() { return paused; }
    public boolean isGameRunning() { return gameRunning; }
    public List<PlayerInfo> getLeaderboard() { return leaderboard; }
}