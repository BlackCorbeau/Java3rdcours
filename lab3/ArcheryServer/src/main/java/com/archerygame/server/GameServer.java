package com.archerygame.server;

import com.archerygame.common.*;
import com.archerygame.server.entity.PersistentPlayer;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class GameServer {
    private int port;
    private static final int MAX_PLAYERS = 4;
    private static final int PANEL_HEIGHT = 500;

    private final List<ClientHandler> clients = new ArrayList<>();
    private final List<ClientHandler> observers = new ArrayList<>();
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
        NetworkPoint point;
        int ownerIndex;
        Arrow(NetworkPoint p, int idx) { point = p; ownerIndex = idx; }
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

    public boolean addPlayer(String name, ClientHandler handler) {
        synchronized (lock) {
            if (name.startsWith("OBSERVER_")) {
                observers.add(handler);
                handler.setPlayerInfo(null);
                System.out.println("[" + new Date() + "] Наблюдатель " + name + " подключён");
                return true;
            }

            if (gameRunning) return false;
            for (PlayerInfo p : players) {
                if (p.getName().equals(name)) return false;
            }

            int winsFromDb = 0;
            if (sessionFactory != null) {
                try (Session session = sessionFactory.openSession()) {
                    PersistentPlayer pp = session
                            .createQuery("from PersistentPlayer where name = :name", PersistentPlayer.class)
                            .setParameter("name", name)
                            .uniqueResult();
                    if (pp != null) winsFromDb = pp.getWins();
                } catch (Exception e) {
                    System.err.println("Ошибка загрузки статистики для " + name + ": " + e.getMessage());
                }
            }
            PlayerInfo newPlayer = new PlayerInfo(name);
            newPlayer.setTotalWins(winsFromDb);
            players.add(newPlayer);
            handler.setPlayerInfo(newPlayer);
            return true;
        }
    }

    public void setReady(ClientHandler handler) {
        synchronized (lock) {
            if (gameRunning) return;
            PlayerInfo pi = handler.getPlayerInfo();
            if (pi != null) pi.setReady(true);
        }
        broadcastState();
        checkAllReady();
    }

    private void checkAllReady() {
        synchronized (lock) {
            if (gameRunning) return;
            List<PlayerInfo> realPlayers = players.stream()
                    .filter(p -> p != null && !p.getName().startsWith("OBSERVER_"))
                    .collect(Collectors.toList());
            if (realPlayers.isEmpty()) return;
            for (PlayerInfo p : realPlayers) {
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
                        NetworkRectangle arrowRect = new NetworkRectangle(arrow.point.x, arrow.point.y, 25, 4);
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
        if (sessionFactory == null) return;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            PersistentPlayer pp = session
                    .createQuery("from PersistentPlayer where name = :name", PersistentPlayer.class)
                    .setParameter("name", winnerName)
                    .uniqueResult();
            if (pp == null) pp = new PersistentPlayer(winnerName, 0);
            pp.setWins(pp.getWins() + 1);
            session.saveOrUpdate(pp);
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                arrows.add(new Arrow(new NetworkPoint(x, y), idx));
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
        List<NetworkPoint> arrowPoints;
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
                winnerCopy, pausedCopy, gameRunningCopy, loadLeaderboard()
        );

        synchronized (lock) {
            for (ClientHandler client : clients) client.sendState(state);
            for (ClientHandler observer : observers) observer.sendState(state);
        }
    }

    public void removeClient(ClientHandler handler) {
        synchronized (lock) {
            clients.remove(handler);
            observers.remove(handler);
            players.remove(handler.getPlayerInfo());
            System.out.println("[" + new Date() + "] Клиент " +
                    (handler.getPlayerInfo() != null ? handler.getPlayerInfo().getName() : "наблюдатель") +
                    " отключён");
        }
        broadcastState();
    }

    private List<PlayerInfo> loadLeaderboard() {
        List<PlayerInfo> leaders = new ArrayList<>();
        if (sessionFactory == null) return leaders;
        try (Session session = sessionFactory.openSession()) {
            List<PersistentPlayer> topPlayers = session
                    .createQuery("from PersistentPlayer order by wins desc", PersistentPlayer.class)
                    .setMaxResults(10)
                    .list();
            for (PersistentPlayer pp : topPlayers) {
                PlayerInfo info = new PlayerInfo(pp.getName());
                info.setTotalWins(pp.getWins());
                leaders.add(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return leaders;
    }
}