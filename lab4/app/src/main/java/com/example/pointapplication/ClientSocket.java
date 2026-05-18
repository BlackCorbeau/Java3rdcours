package com.example.pointapplication;

import com.archerygame.common.GameState;
import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private GameModel model;
    private Thread readerThread;
    private volatile boolean connected = false;

    public ClientSocket(Socket socket, GameModel model) throws IOException {
        this.socket = socket;
        this.model = model;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        connected = true;
        readerThread = new Thread(this::readLoop);
        readerThread.start();
    }

    private void readLoop() {
        try {
            while (connected) {
                Object obj = in.readObject();
                if (obj instanceof GameState) model.updateState((GameState) obj);
                else if (obj instanceof String) System.out.println("Сообщение сервера: " + obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (connected) e.printStackTrace();
        } finally { close(); }
    }

    public void sendName(String name) throws IOException {
        out.writeObject(name);
        out.flush();
    }

    public void close() {
        connected = false;
        try { if (in != null) in.close(); } catch (IOException e) {}
        try { if (out != null) out.close(); } catch (IOException e) {}
        try { if (socket != null) socket.close(); } catch (IOException e) {}
        if (readerThread != null) readerThread.interrupt();
    }
}