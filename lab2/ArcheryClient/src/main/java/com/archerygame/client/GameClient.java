package com.archerygame.client;

import com.archerygame.common.GameState;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.*;
import java.net.*;

@Component
public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GamePanel gamePanel;
    private String playerName;
    private GameState currentState;

    public void connect(String serverAddress, String name) throws IOException, ClassNotFoundException {
        this.playerName = name;
        socket = new Socket(serverAddress, 12345);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        out.writeObject(name);
        String response = (String) in.readObject();
        if ("NAME_TAKEN".equals(response)) throw new IOException("Имя уже используется");
        new Thread(this::receiveLoop).start();
    }

    private void receiveLoop() {
        try {
            while (true) {
                GameState state = (GameState) in.readObject();
                this.currentState = state;
                if (gamePanel != null) SwingUtilities.invokeLater(() -> gamePanel.updateState(state));
            }
        } catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
    }

    public void sendReady() { try { out.writeObject("READY"); } catch (IOException e) {} }
    public void sendPause() { try { out.writeObject("PAUSE"); } catch (IOException e) {} }
    public void sendShoot(int x, int y) {
        try {
            out.writeObject("SHOOT");
            out.writeInt(x);
            out.writeInt(y);
        } catch (IOException e) {}
    }

    public void setGamePanel(GamePanel panel) { this.gamePanel = panel; }
    public String getPlayerName() { return playerName; }
    public GameState getCurrentState() { return currentState; }
}