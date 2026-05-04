package com.archerygame.client;

import com.archerygame.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Component
public class GameFrame extends JFrame {
    private final GameClient client;
    private final GamePanel gamePanel;
    private JLabel scoreLabel, shotsLabel;
    private JButton readyButton, pauseButton;
    private Thread uiUpdateThread;
    private volatile boolean running = true;

    @Autowired
    public GameFrame(GameClient client, GamePanel gamePanel) {
        this.client = client;
        this.gamePanel = gamePanel;
        client.setGamePanel(gamePanel);
        initUI();
        startUiUpdateThread();
    }

    private void initUI() {
        setTitle("Меткий стрелок - " + client.getPlayerName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                running = false;
                if (uiUpdateThread != null) {
                    uiUpdateThread.interrupt();
                }
                dispose();
                System.exit(0);
            }
        });
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        readyButton = new JButton("Готов");
        pauseButton = new JButton("Пауза");
        pauseButton.setEnabled(false);
        scoreLabel = new JLabel("Счёт: 0");
        shotsLabel = new JLabel("Выстрелы: 0");

        controlPanel.add(readyButton);
        controlPanel.add(pauseButton);
        controlPanel.add(scoreLabel);
        controlPanel.add(shotsLabel);
        add(controlPanel, BorderLayout.SOUTH);

        readyButton.addActionListener(e -> {
            client.sendReady();
            readyButton.setEnabled(false);
            pauseButton.setEnabled(true);
        });
        pauseButton.addActionListener(e -> client.sendPause());

        pack();
        setLocationRelativeTo(null);
    }

    private void startUiUpdateThread() {
        uiUpdateThread = new Thread(() -> {
            while (running) {
                try {
                    GameState state = client.getCurrentState();
                    if (state != null) {
                        SwingUtilities.invokeLater(() -> updateUi(state));
                    }
                    Thread.sleep(50); // примерно 20 FPS для интерфейса
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        uiUpdateThread.setDaemon(true);
        uiUpdateThread.start();
    }

    private void updateUi(GameState state) {
        // обновляем счёт и выстрелы текущего игрока
        for (PlayerInfo p : state.getPlayers()) {
            if (p.getName().equals(client.getPlayerName())) {
                scoreLabel.setText("Счёт: " + p.getScore());
                shotsLabel.setText("Выстрелы: " + p.getShots());
                break;
            }
        }
        if (state.getWinner() != null) {
            readyButton.setEnabled(true);
            pauseButton.setEnabled(false);
        } else if (!state.isGameRunning()) {
            readyButton.setEnabled(true);
            pauseButton.setEnabled(false);
        } else {
            readyButton.setEnabled(false);
            pauseButton.setEnabled(true);
        }
        pauseButton.setText(state.isPaused() ? "Возобновить" : "Пауза");
    }

    public void showWindow() {
        setVisible(true);
    }
}