package com.example.SPSProjectBackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import redis.embedded.RedisServer;
import java.io.IOException;

@Configuration
public class EmbeddedRedisConfig {

    @Value("${spring.data.redis.port:6388}") //Changed from 6383 to 6388
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            redisServer = RedisServer.builder()
                    .port(redisPort)
                    .setting("maxheap 128M")
                    .build();
            redisServer.start();
            System.out.println("[EmbeddedRedisConfig] Embedded Redis started successfully on port " + redisPort);
        } catch (Exception e) {
            System.err.println("[EmbeddedRedisConfig] Warning: Failed to start embedded Redis: " + e.getMessage());
        }
    }

    @PreDestroy
    public void stopRedis() {
        try {
            if (redisServer != null && redisServer.isActive()) {
                redisServer.stop();
            }
        } catch (Exception e) {
            System.err.println("[EmbeddedRedisConfig] Warning: Error stopping Redis: " + e.getMessage());
        }
    }
}