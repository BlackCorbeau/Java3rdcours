package com.archerygame.server;

import com.archerygame.server.entity.PersistentPlayer;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@org.springframework.context.annotation.Configuration
@ComponentScan(basePackages = "com.archerygame.server")
public class ServerConfig {

    @Bean
    public SessionFactory sessionFactory() {
        Configuration cfg = new Configuration();
        cfg.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:h2:file:./player_stats;DB_CLOSE_DELAY=-1");
        cfg.setProperty("hibernate.connection.username", "sa");
        cfg.setProperty("hibernate.connection.password", "");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        cfg.setProperty("hibernate.hbm2ddl.auto", "update");
        cfg.setProperty("hibernate.show_sql", "true");
        cfg.addAnnotatedClass(PersistentPlayer.class);
        return cfg.buildSessionFactory();
    }
}