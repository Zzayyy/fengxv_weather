package com.zzay.fengxv_weather.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "openweathermap.key")
public class WeatherConfig {
    private String appid;
    private String units;
    private String lang;
}
