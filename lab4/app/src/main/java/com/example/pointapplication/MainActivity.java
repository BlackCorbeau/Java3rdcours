package com.example.pointapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.archerygame.common.GameState;
import com.archerygame.common.PlayerInfo;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private EditText ipInput, portInput;
    private Button connectButton;
    private TextView statusText, winnerText;
    private RecyclerView playersRecycler, leaderboardRecycler;
    private PlayerAdapter playerAdapter;
    private LeaderboardAdapter leaderboardAdapter;
    private GameModel model;
    private ClientSocket clientSocket;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipInput = findViewById(R.id.ipInput);
        portInput = findViewById(R.id.portInput);
        connectButton = findViewById(R.id.connectButton);
        statusText = findViewById(R.id.statusText);
        winnerText = findViewById(R.id.winnerText);
        playersRecycler = findViewById(R.id.playersRecyclerView);
        leaderboardRecycler = findViewById(R.id.leaderboardRecyclerView);

        model = new GameModel();
        playerAdapter = new PlayerAdapter(model.getPlayers());
        leaderboardAdapter = new LeaderboardAdapter(model.getLeaderboard());

        playersRecycler.setLayoutManager(new LinearLayoutManager(this));
        playersRecycler.setAdapter(playerAdapter);
        leaderboardRecycler.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecycler.setAdapter(leaderboardAdapter);

        model.addObserver(() -> uiHandler.post(() -> {
            playerAdapter.updatePlayers(model.getPlayers());
            leaderboardAdapter.updateLeaders(model.getLeaderboard());
            if (model.getWinner() != null && !model.getWinner().isEmpty())
                winnerText.setText("Победитель: " + model.getWinner());
            else winnerText.setText("");
            statusText.setText(model.isGameRunning() ?
                    (model.isPaused() ? "Игра на паузе" : "Игра идёт") :
                    "Игра не запущена / завершена");
        }));

        connectButton.setOnClickListener(v -> connectToServer());
    }

    private void connectToServer() {
        String ip = ipInput.getText().toString().trim();
        int port;
        try { port = Integer.parseInt(portInput.getText().toString().trim()); }
        catch (NumberFormatException e) { Toast.makeText(this, "Неверный порт", Toast.LENGTH_SHORT).show(); return; }

        connectButton.setEnabled(false);
        statusText.setText("Подключение...");

        new Thread(() -> {
            try {
                InetAddress address = InetAddress.getByName(ip);
                Socket socket = new Socket(address, port);
                ClientSocket cs = new ClientSocket(socket, model);
                String observerName = "OBSERVER_Android_" + System.currentTimeMillis();
                cs.sendName(observerName);
                clientSocket = cs;
                uiHandler.post(() -> {
                    statusText.setText("Подключён как наблюдатель");
                    connectButton.setEnabled(false);
                    Toast.makeText(MainActivity.this, "Подключено", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                uiHandler.post(() -> {
                    Toast.makeText(MainActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    statusText.setText("Не подключён");
                    connectButton.setEnabled(true);
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clientSocket != null) clientSocket.close();
    }
}