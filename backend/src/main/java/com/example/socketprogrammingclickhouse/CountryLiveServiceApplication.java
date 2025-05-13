package com.example.socketprogrammingclickhouse;

import com.clickhouse.jdbc.ClickHouseDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootApplication
public class CountryLiveServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CountryLiveServiceApplication.class, args);
    }

    @Bean
    public DataSource clickHouseDataSource() throws SQLException {
        String url = "jdbc:clickhouse://dlb3asadvs.ap-south-1.aws.clickhouse.cloud:8443/testdb?ssl=true";
        return new ClickHouseDataSource(url);
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