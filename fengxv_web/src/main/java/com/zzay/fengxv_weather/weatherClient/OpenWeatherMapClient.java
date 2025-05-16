package com.zzay.fengxv_weather.weatherClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.zzay.fengxv_weather.config.WeatherConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class OpenWeatherMapClient  {

    @Autowired
    private WebClient webClient;
    @Autowired
    private WeatherConfig weatherConfig;

    public String getCurrentWeatherByCityName(String city) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("q", city)
                        .queryParam("appid", weatherConfig.getAppid())
                        .queryParam("units", weatherConfig.getUnits())
                        .queryParam("lang", weatherConfig.getLang())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10)) // 超过 10 秒则抛出 TimeoutException
                .block();
    }

    public String getCurrentWeatherByCityId(String cityId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("id", cityId)
                        .queryParam("appid", weatherConfig.getAppid())
                        .queryParam("units", weatherConfig.getUnits())
                        .queryParam("lang", weatherConfig.getLang())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 注意：block() 是阻塞的，仅用于演示
    }

    public String getCurrentWeatherByZIPCode(String ZIPCode) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("zip", ZIPCode)
                        .queryParam("appid", weatherConfig.getAppid())
                        .queryParam("units", weatherConfig.getUnits())
                        .queryParam("lang", weatherConfig.getLang())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 注意：block() 是阻塞的，仅用于演示
    }


    // 获取英文城市名（Geocoding API）
    public String getGeocodingByCityName(String localCityName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geo/1.0/direct")
                        .queryParam("q", localCityName)
                        .queryParam("limit", 1)
                        .queryParam("appid", weatherConfig.getAppid())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(1)))
                .block();
    }
}