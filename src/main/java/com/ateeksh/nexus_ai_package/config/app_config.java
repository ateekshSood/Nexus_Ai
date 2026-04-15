package com.ateeksh.nexus_ai_package.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import java.time.Duration;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class app_config {
    
    @Bean 
    public RestTemplate restTemplate(RestTemplateBuilder builder){
        return builder
        .connectTimeout(Duration.ofSeconds(5))
        .readTimeout(Duration.ofSeconds(30))
        .build();

    }

    @Bean 
    public WebMvcConfigurer corsConfigure(){
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry){
                registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET" , "POST");
            }
        };
    }


}
