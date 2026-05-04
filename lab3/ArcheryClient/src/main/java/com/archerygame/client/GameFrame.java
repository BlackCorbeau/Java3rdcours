package com.archerygame.client;

import com.archerygame.common.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

@Component
public class GameFrame extends JFrame {
    private final GameClient client;
    private final GamePanel gamePanel;
    private JLabel scoreLabel, shotsLabel, winsLabel;
    private JButton readyButton, pauseButton;
    private JList<String> leadersList;
    private DefaultListModel<String> leadersModel;
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
                if (uiUpdateThread != null) uiUpdateThread.interrupt();
                dispose();
                System.exit(0);
            }
        });
        setLayout(new BorderLayout());
        add(gamePanel, BorderLayout.CENTER);

        // ----- Панель управления внизу -----
        JPanel controlPanel = new JPanel();
        readyButton = new JButton("Готов");
        pauseButton = new JButton("Пауза");
        pauseButton.setEnabled(false);
        scoreLabel = new JLabel("Счёт: 0");
        shotsLabel = new JLabel("Выстрелы: 0");
        winsLabel = new JLabel("Побед: 0");

        controlPanel.add(readyButton);
        controlPanel.add(pauseButton);
        controlPanel.add(scoreLabel);
        controlPanel.add(shotsLabel);
        controlPanel.add(winsLabel);
        add(controlPanel, BorderLayout.SOUTH);

        // ----- Панель лидеров в правом верхнем углу -----
        JPanel topRightPanel = new JPanel();
        topRightPanel.setLayout(new BorderLayout());
        topRightPanel.setBackground(new Color(0, 0, 0, 150));
        topRightPanel.setOpaque(true);
        topRightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel leadersTitle = new JLabel("Топ игроков (всех времён)");
        leadersTitle.setForeground(Color.WHITE);
        leadersTitle.setFont(new Font("Arial", Font.BOLD, 14));
        topRightPanel.add(leadersTitle, BorderLayout.NORTH);

        leadersModel = new DefaultListModel<>();
        leadersList = new JList<>(leadersModel);
        leadersList.setBackground(new Color(0, 0, 0, 180));
        leadersList.setForeground(Color.WHITE);
        leadersList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        leadersList.setFixedCellHeight(20);
        JScrollPane scrollPane = new JScrollPane(leadersList);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        topRightPanel.add(scrollPane, BorderLayout.CENTER);

        add(topRightPanel, BorderLayout.NORTH);
        ((JPanel) getContentPane()).setComponentZOrder(topRightPanel, 0);

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
                    Thread.sleep(50);
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
        // Обновляем данные текущего игрока
        int myTotalWins = 0;
        for (PlayerInfo p : state.getPlayers()) {
            if (p.getName().equals(client.getPlayerName())) {
                scoreLabel.setText("Счёт: " + p.getScore());
                shotsLabel.setText("Выстрелы: " + p.getShots());
                myTotalWins = p.getTotalWins();
                winsLabel.setText("Побед: " + myTotalWins);
                break;
            }
        }

        // Обновляем глобальный рейтинг (всех игроков из БД)
        List<PlayerInfo> leaders = state.getLeaderboard();
        leadersModel.clear();
        if (leaders != null) {
            for (PlayerInfo p : leaders) {
                leadersModel.addElement(String.format("%-10s %3d", p.getName(), p.getTotalWins()));
            }
        }

        // Управление кнопками
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