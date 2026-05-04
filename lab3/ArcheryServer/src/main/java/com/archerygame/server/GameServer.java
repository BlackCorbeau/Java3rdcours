package com.archerygame.server;

import com.archerygame.common.*;
import com.archerygame.server.entity.PersistentPlayer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Date;

@Component
public class GameServer {
    private int port;
    private static final int MAX_PLAYERS = 4;
    private static final int PANEL_HEIGHT = 500;  // синхронизировано с клиентом

    private final List<ClientHandler> clients = new ArrayList<>();
    private final List<PlayerInfo> players = new ArrayList<>();
    private final List<Target> targets = new ArrayList<>();
    private final List<Arrow> arrows = new ArrayList<>();

    private boolean gameRunning = false;
    private boolean paused = false;
    private String winner = null;

    private final Object lock = new Object();

    @Autowired
    private SessionFactory sessionFactory;

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
            System.out.println("[" + new Date() + "] Сервер запущен на порту " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                synchronized (lock) {
                    if (clients.size() >= MAX_PLAYERS) {
                        socket.close();
                        continue;
                    }
                    ClientHandler handler = new ClientHandler(socket, this);
                    clients.add(handler);
                    new Thread(handler).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Возвращает: 0 – успех, -1 – имя занято, -2 – игра уже идёт
    public int addPlayer(String name, ClientHandler handler) {
        synchronized (lock) {
            if (gameRunning) return -2;
            for (PlayerInfo p : players) {
                if (p.getName().equals(name)) return -1;
            }
            // Получаем количество побед из БД
            int winsFromDb = 0;
            try (Session session = sessionFactory.openSession()) {
                PersistentPlayer pp = session.get(PersistentPlayer.class, name);
                if (pp != null) winsFromDb = pp.getWins();
            } catch (Exception e) { e.printStackTrace(); }
            PlayerInfo newPlayer = new PlayerInfo(name);
            newPlayer.setTotalWins(winsFromDb);
            players.add(newPlayer);
            handler.setPlayerInfo(newPlayer);
            return 0;
        }
    }

    public void setReady(ClientHandler handler) {
        synchronized (lock) {
            if (gameRunning) return;
            handler.getPlayerInfo().setReady(true);
        }
        broadcastState();
        checkAllReady();
    }

    private void checkAllReady() {
        synchronized (lock) {
            if (gameRunning) return;
            if (players.isEmpty()) return;
            for (PlayerInfo p : players) {
                if (!p.isReady()) return;
            }
            startGame();
        }
    }

    private void startGame() {
        synchronized (lock) {
            gameRunning = true;
            paused = false;
            winner = null;
            for (PlayerInfo p : players) {
                p.setScore(0);
                p.setShots(0);
                p.setReady(false);
            }
            targets.clear();
            targets.add(new Target(580, 100, 55, 55, 1, 3));
            targets.add(new Target(700, 250, 45, 45, 3, -5));
            arrows.clear();
        }
        broadcastState();
        new Thread(this::gameLoop).start();
    }

    private void gameLoop() {
        while (true) {
            synchronized (lock) {
                if (!gameRunning || winner != null) {
                    if (!gameRunning) break;
                    if (winner != null) {
                        gameRunning = false;
                        break;
                    }
                }
                if (!paused) {
                    for (Target t : targets) t.move(PANEL_HEIGHT);
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
                        if (arrow.point.x > 800) toRemove.add(arrow);
                    }
                    arrows.removeAll(toRemove);
                }
            }
            broadcastState();

            // Проверка победителя и обновление БД
            synchronized (lock) {
                if (winner != null && gameRunning) {
                    updateWinnerStats(winner);
                    gameRunning = false;
                    broadcastState();
                    break;
                }
                if (!gameRunning) break;
            }

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void updateWinnerStats(String winnerName) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            PersistentPlayer pp = session.get(PersistentPlayer.class, winnerName);
            if (pp == null) pp = new PersistentPlayer(winnerName, 0);
            pp.setWins(pp.getWins() + 1);
            session.saveOrUpdate(pp);
            tx.commit();
        } catch (Exception e) { e.printStackTrace(); }

        // Обновить totalWins в PlayerInfo для всех клиентов
        for (PlayerInfo p : players) {
            if (p.getName().equals(winnerName)) {
                p.setTotalWins(p.getTotalWins() + 1);
                break;
            }
        }
    }

    public void shoot(ClientHandler handler, int x, int y) {
        synchronized (lock) {
            if (!gameRunning || paused) return;
            int idx = players.indexOf(handler.getPlayerInfo());
            if (idx >= 0) {
                arrows.add(new Arrow(new Point(x, y), idx));
                handler.getPlayerInfo().setShots(handler.getPlayerInfo().getShots() + 1);
            }
        }
        broadcastState();
    }

    public void togglePause(ClientHandler handler) {
        synchronized (lock) {
            if (!gameRunning) return;
            paused = !paused;
        }
        broadcastState();
    }

    public void broadcastState() {
        List<Point> arrowPoints;
        List<Target> targetsCopy;
        List<PlayerInfo> playersCopy;
        String winnerCopy;
        boolean pausedCopy;
        boolean gameRunningCopy;

        synchronized (lock) {
            arrowPoints = new ArrayList<>();
            for (Arrow a : arrows) arrowPoints.add(a.point);
            targetsCopy = new ArrayList<>(targets);
            playersCopy = new ArrayList<>(players);
            winnerCopy = winner;
            pausedCopy = paused;
            gameRunningCopy = gameRunning;
        }

        GameState state = new GameState(
                targetsCopy, arrowPoints, playersCopy,
                winnerCopy, pausedCopy, gameRunningCopy
        );

        synchronized (lock) {
            for (ClientHandler client : clients) {
                client.sendState(state);
            }
        }
    }

    public void removeClient(ClientHandler handler) {
        synchronized (lock) {
            clients.remove(handler);
            players.remove(handler.getPlayerInfo());
            System.out.println("[" + new Date() + "] Клиент " + handler.getPlayerInfo().getName() + " отключён");
        }
        broadcastState();
    }
}