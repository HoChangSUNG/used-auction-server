package com.auction.usedauction.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://bkkang1.github.io", "https://usedauction.shop",
                        "https://192.168.214.7",
                        "http://192.168.214.7")
                .allowCredentials(true);
    }
}