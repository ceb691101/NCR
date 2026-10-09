package com.example.SPSProjectBackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import util.common.Encryption;

@Configuration
public class EncryptionConfig {

    @Bean
    public Encryption encryption() {
        return new Encryption();
    }
}