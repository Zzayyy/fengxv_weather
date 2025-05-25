package com.zzay.fengxv_weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有来源
        config.addAllowedOrigin("http://localhost:5173");  // Vue 开发服务器地址

        // 允许所有头部
        config.addAllowedHeader("*");

        // 允许所有方法（GET、POST、PUT、DELETE 等）
        config.addAllowedMethod("*");

        // 是否允许携带凭证（如 Cookie）
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}