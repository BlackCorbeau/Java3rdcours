package com.archerygame.server;

import com.archerygame.common.*;
import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private GameServer server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private PlayerInfo playerInfo;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setPlayerInfo(PlayerInfo info) { this.playerInfo = info; }
    public PlayerInfo getPlayerInfo() { return playerInfo; }

    public void sendState(GameState state) {
        try {
            out.writeObject(state);
            out.reset();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void sendMessage(String msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        try {
            String name = (String) in.readObject();
            // Проверка: можно ли добавить игрока?
            if (!server.addPlayer(name, this)) {
                out.writeObject("NAME_TAKEN");
                socket.close();
                return;
            }
            out.writeObject("OK");
            server.broadcastState();

            while (true) {
                String cmd = (String) in.readObject();
                switch (cmd) {
                    case "READY": server.setReady(this); break;
                    case "PAUSE": server.togglePause(this); break;
                    case "SHOOT":
                        int x = in.readInt();
                        int y = in.readInt();
                        server.shoot(this, x, y);
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            server.removeClient(this);
        }
    }
}