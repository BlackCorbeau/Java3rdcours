package com.archerygame;

import com.archerygame.server.GameServer;
import com.archerygame.server.ServerConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainServer {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(ServerConfig.class);
        GameServer server = context.getBean(GameServer.class);

        if (args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                server.setPort(port);
                System.out.println("Установлен порт из аргументов: " + port);
            } catch (NumberFormatException e) {
                System.err.println("Неверный номер порта, используется порт по умолчанию 12345");
            }
        }

        server.start();
    }
}