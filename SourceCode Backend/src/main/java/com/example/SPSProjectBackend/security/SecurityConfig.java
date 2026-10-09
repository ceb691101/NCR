// SecInfoSessionData
package com.example.SPSProjectBackend.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Allow OPTIONS requests for all endpoints
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                         // Add journal endpoints
                        .requestMatchers("/api/journals/**").permitAll()
                        .requestMatchers("/api/journals/health").permitAll()

                        // Existing endpoints
                        .requestMatchers("/api/v1/**").permitAll()
                        .requestMatchers("/api/report/**").permitAll()
                        .requestMatchers("/api/application/**").permitAll()
                        .requestMatchers("/api/spestcnd/save").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()

                        //SecInfo Accounts
                        .requestMatchers("/api/v1/accounts/**").permitAll()
                        .requestMatchers("/api/v1/secinfo/**").permitAll()
                        
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }

//     @Bean
//     CorsConfigurationSource corsConfigurationSource(){
//         CorsConfiguration configuration = new CorsConfiguration();
//         configuration.setAllowedOrigins(List.of("http://localhost:3000"));
//         configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH" , "DELETE", "OPTIONS"));
//         configuration.setAllowCredentials(true);
//         configuration.addAllowedHeader("*");
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", configuration);
//         return source;
//     }
    @Bean
    CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        // Add both origins, or use just 8097
        configuration.setAllowedOrigins(List.of("http://localhost:8097", "http://localhost:3000", "http://10.128.1.59:8097", "http://10.128.1.227:8095", "http://127.0.0.1:8095", "http://localhost:8095", "http://10.128.28.29:8095"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CommandLineRunner printGeneratedPassword() {
        return args -> {
            String generatedPassword = "admin123";
            System.out.println("Generated Security Password: " + generatedPassword);
        };
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        UserDetails user = User.withUsername("user")
                .password(bCryptPasswordEncoder().encode("admin123"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}