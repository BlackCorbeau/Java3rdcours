package com.archerygame.client;

import com.archerygame.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.swing.*;
import java.awt.*;

@Component
public class GameFrame extends JFrame {
    private final GameClient client;
    private final GamePanel gamePanel;
    private JLabel scoreLabel, shotsLabel;
    private JButton readyButton, pauseButton;

    @Autowired
    public GameFrame(GameClient client, GamePanel gamePanel) {
        this.client = client;
        this.gamePanel = gamePanel;
        client.setGamePanel(gamePanel);
        initUI();
    }

    private void initUI() {
        setTitle("Меткий стрелок - " + client.getPlayerName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        Timer uiTimer = new Timer(50, e -> {
            GameState state = client.getCurrentState();
            if (state != null) {
                for (PlayerInfo p : state.getPlayers()) {
                    if (p.getName().equals(client.getPlayerName())) {
                        scoreLabel.setText("Счёт: " + p.getScore());
                        shotsLabel.setText("Выстрелы: " + p.getShots());
                        break;
                    }
                }
                if (state.getWinner() != null) {
                    readyButton.setEnabled(true);   // можно начать новую игру
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
        });
        uiTimer.start();

        pack();
        setLocationRelativeTo(null);
    }

    public void showWindow() {
        setVisible(true);
    }
}