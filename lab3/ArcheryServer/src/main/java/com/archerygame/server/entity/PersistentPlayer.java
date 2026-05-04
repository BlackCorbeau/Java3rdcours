package com.archerygame.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "players")
public class PersistentPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private int wins;

    public PersistentPlayer() {}

    public PersistentPlayer(String name, int wins) {
        this.name = name;
        this.wins = wins;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
}