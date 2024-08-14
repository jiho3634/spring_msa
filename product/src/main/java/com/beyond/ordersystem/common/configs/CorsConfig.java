package com.beyond.ordersystem.common.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8081")    //  허용 url 명시 ; vue
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
        WebMvcConfigurer.super.addCorsMappings(registry);
    }
}
