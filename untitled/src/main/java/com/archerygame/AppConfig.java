package com.archerygame;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public GameController gameController() {
        return new GameController();
    }

    @Bean
    public GameFrame gameFrame(GameController controller, GamePanel panel) {
        return new GameFrame(controller, panel);
    }

    @Bean
    public GamePanel gamePanel(GameController controller) {
        return new GamePanel(controller);
    }
}