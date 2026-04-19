package com.archerygame.client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String server = JOptionPane.showInputDialog("Введите адрес сервера:", "localhost");
            String name = JOptionPane.showInputDialog("Введите ваше имя:");
            if (name == null || name.trim().isEmpty()) return;

            ApplicationContext context = new AnnotationConfigApplicationContext(ClientConfig.class);
            GameClient client = context.getBean(GameClient.class);
            try {
                client.connect(server, name.trim());
                GameFrame frame = context.getBean(GameFrame.class);
                frame.showWindow();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Ошибка подключения: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}