package com.example.pointapplication;

import com.archerygame.common.PlayerInfo;
import com.archerygame.common.GameState;
import java.util.ArrayList;
import java.util.List;

public class GameModel {
    private List<PlayerInfo> players = new ArrayList<>();
    private List<PlayerInfo> leaderboard = new ArrayList<>();
    private String winner = null;
    private boolean gameRunning = false;
    private boolean paused = false;
    private final List<IEvent> observers = new ArrayList<>();

    public void addObserver(IEvent e) { observers.add(e); }
    private void notifyObservers() { for (IEvent obs : observers) obs.event(); }

    public void updateState(GameState state) {
        if (state == null) return;
        this.players = state.getPlayers() != null ? state.getPlayers() : new ArrayList<>();
        this.leaderboard = state.getLeaderboard() != null ? state.getLeaderboard() : new ArrayList<>();
        this.winner = state.getWinner();
        this.gameRunning = state.isGameRunning();
        this.paused = state.isPaused();
        notifyObservers();
    }

    public List<PlayerInfo> getPlayers() { return players; }
    public List<PlayerInfo> getLeaderboard() { return leaderboard; }
    public String getWinner() { return winner; }
    public boolean isGameRunning() { return gameRunning; }
    public boolean isPaused() { return paused; }
}