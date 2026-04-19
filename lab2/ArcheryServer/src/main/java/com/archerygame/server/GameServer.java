package com.archerygame.server;

import com.archerygame.common.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.awt.Point;
import java.awt.Rectangle;

@Component
public class GameServer {
    private int port;
    private static final int MAX_PLAYERS = 4;
    private static final int PANEL_HEIGHT = 1000;

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private final List<PlayerInfo> players = new CopyOnWriteArrayList<>();
    private final List<Target> targets = new CopyOnWriteArrayList<>();
    private final List<Arrow> arrows = new CopyOnWriteArrayList<>();

    private boolean gameRunning = false;
    private boolean paused = false;
    private String winner = null;

    private static class Arrow implements Serializable {
        Point point;
        int ownerIndex;
        Arrow(Point p, int idx) { point = p; ownerIndex = idx; }
    }

    public GameServer() {
        this.port = 12345;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[" + new Date() + "] Сервер запущен на порту " + port + " и слушает все интерфейсы (0.0.0.0)");
            while (true) {
                Socket socket = serverSocket.accept();
                InetAddress clientAddr = socket.getInetAddress();
                System.out.println("[" + new Date() + "] Новое подключение от " + clientAddr.getHostAddress() + ":" + socket.getPort());
                if (clients.size() >= MAX_PLAYERS) {
                    System.out.println("Отказано: достигнуто максимальное число игроков (" + MAX_PLAYERS + ")");
                    socket.close();
                    continue;
                }
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.err.println("[" + new Date() + "] Ошибка сервера: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized boolean addPlayer(String name, ClientHandler handler) {
        if (players.stream().anyMatch(p -> p.getName().equals(name))) return false;
        PlayerInfo newPlayer = new PlayerInfo(name);
        players.add(newPlayer);
        handler.setPlayerInfo(newPlayer);
        return true;
    }

    public synchronized void setReady(ClientHandler handler) {
        if (gameRunning) return;
        handler.getPlayerInfo().setReady(true);
        broadcastState();
        checkAllReady();
    }

    private void checkAllReady() {
        if (gameRunning) return;
        if (players.isEmpty()) return;
        if (players.stream().allMatch(PlayerInfo::isReady)) {
            startGame();
        }
    }

    private void startGame() {
        gameRunning = true;
        paused = false;
        winner = null;
        System.out.println("[" + new Date() + "] Игра запущена! Игроков: " + players.size());
        for (PlayerInfo p : players) {
            p.setScore(0);
            p.setShots(0);
            p.setReady(false);
        }
        targets.clear();
        // Только вертикальное движение (vy)
        targets.add(new Target(580, 100, 55, 55, 1, 3));
        targets.add(new Target(700, 250, 45, 45, 3, -5));
        arrows.clear();
        broadcastState();
        new Thread(this::gameLoop).start();
    }

    private void gameLoop() {
        while (gameRunning && winner == null) {
            if (!paused) {
                for (Target t : targets) t.move(PANEL_HEIGHT);   // только высота
                List<Arrow> toRemove = new ArrayList<>();
                for (Arrow arrow : arrows) {
                    arrow.point.x += 20;
                    Rectangle arrowRect = new Rectangle(arrow.point.x, arrow.point.y, 25, 4);
                    boolean hit = false;
                    for (Target t : targets) {
                        if (arrowRect.intersects(t.getBounds())) {
                            PlayerInfo owner = players.get(arrow.ownerIndex);
                            owner.setScore(owner.getScore() + t.getPoints());
                            if (owner.getScore() >= 6) winner = owner.getName();
                            hit = true;
                            break;
                        }
                    }
                    if (hit) {
                        toRemove.add(arrow);
                        continue;
                    }
                    if (arrow.point.x > 800) {
                        toRemove.add(arrow);
                    }
                }
                arrows.removeAll(toRemove);
                broadcastState();
            }
            try { Thread.sleep(10); } catch (InterruptedException e) { }
        }
        gameRunning = false;
        System.out.println("[" + new Date() + "] Игра завершена. Победитель: " + (winner == null ? "нет" : winner));
        broadcastState();
    }

    public synchronized void shoot(ClientHandler handler, int x, int y) {
        if (!gameRunning || paused) return;
        int idx = players.indexOf(handler.getPlayerInfo());
        if (idx >= 0) {
            arrows.add(new Arrow(new Point(x, y), idx));
            handler.getPlayerInfo().setShots(handler.getPlayerInfo().getShots() + 1);
            broadcastState();
        }
    }

    public synchronized void togglePause(ClientHandler handler) {
        if (!gameRunning) return;
        paused = !paused;
        broadcastState();
    }

    public synchronized void broadcastState() {
        List<Point> arrowPoints = new ArrayList<>();
        for (Arrow a : arrows) arrowPoints.add(a.point);
        GameState state = new GameState(
                new ArrayList<>(targets),
                arrowPoints,
                new ArrayList<>(players),
                winner,
                paused,
                gameRunning
        );
        for (ClientHandler client : clients) client.sendState(state);
    }

    public synchronized void removeClient(ClientHandler handler) {
        clients.remove(handler);
        players.remove(handler.getPlayerInfo());
        System.out.println("[" + new Date() + "] Клиент " + handler.getPlayerInfo().getName() + " отключён. Осталось игроков: " + players.size());
        broadcastState();
    }
}