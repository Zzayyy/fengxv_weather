package com.zzay.fengxv_weather.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "openweathermap.key")
public class OpenWeatherMapWeatherConfig {
    private String appid;
    private String units;
    private String lang;
}
