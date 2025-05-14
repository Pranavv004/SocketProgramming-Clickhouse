package com.example.socketprogrammingclickhouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class CountryLiveServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CountryLiveServiceApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:8000", "https://country-live-webpage.onrender.com")
                        .allowedMethods("GET", "POST", "OPTIONS")
                        .allowedHeaders("Content-Type")
                        .maxAge(3600);
            }
        };
    }
}