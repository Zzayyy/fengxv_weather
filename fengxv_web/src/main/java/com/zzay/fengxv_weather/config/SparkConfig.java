package com.zzay.fengxv_weather.config;


import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Spark配置类
@Configuration
public class SparkConfig {
    @Bean(destroyMethod = "close")
    public SparkSession sparkSession() {
        return SparkSession
                .builder()
                .appName("WeatherDataAnalysis")
                .master("local[*]") // 根据实际部署环境调整
                .config("spark.ui.enabled", "false") // 👈 关键：禁用 Web UI
                .getOrCreate();
    }
}
