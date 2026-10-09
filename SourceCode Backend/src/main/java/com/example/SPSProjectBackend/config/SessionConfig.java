package com.example.SPSProjectBackend.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.example.SPSProjectBackend", "util"})
public class SessionConfig {
    // ensures both main package and util package are scanned
}