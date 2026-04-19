package com.archerygame;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    private final GameController controller;
    private final GamePanel gamePanel;
    private JLabel scoreLabel;
    private JLabel shotsLabel;
    private JButton startButton;
    private JButton stopButton;
    private JButton pauseButton;

    public GameFrame(GameController controller, GamePanel gamePanel) {
        this.controller = controller;
        this.gamePanel = gamePanel;
        initUI();
        controller.setScoreLabel(scoreLabel);
        controller.setShotsLabel(shotsLabel);
    }

    private void initUI() {
        setTitle("Меткий стрелок");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(gamePanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        startButton = new JButton("Старт");
        stopButton = new JButton("Стоп");
        pauseButton = new JButton("Пауза");
        pauseButton.setEnabled(false);
        scoreLabel = new JLabel("Счёт: 0");
        shotsLabel = new JLabel("Выстрелы: 0");

        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(pauseButton);
        controlPanel.add(scoreLabel);
        controlPanel.add(shotsLabel);
        add(controlPanel, BorderLayout.SOUTH);

        startButton.addActionListener(e -> {
            controller.startGame(gamePanel.getHeight());
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            pauseButton.setEnabled(true);
        });
        stopButton.addActionListener(e -> {
            controller.stopGame();
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            pauseButton.setEnabled(false);
            pauseButton.setText("Пауза");
            scoreLabel.setText("Счёт: 0");
            shotsLabel.setText("Выстрелы: 0");
            gamePanel.repaint();
        });
        pauseButton.addActionListener(e -> {
            controller.togglePause();
            pauseButton.setText(controller.isPaused() ? "Возобновить" : "Пауза");
            gamePanel.repaint();
        });

        pack();
        setLocationRelativeTo(null);
    }
}