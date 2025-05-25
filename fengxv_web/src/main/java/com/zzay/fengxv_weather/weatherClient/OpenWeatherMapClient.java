package com.zzay.fengxv_weather.weatherClient;

import com.zzay.fengxv_weather.config.properties.OpenWeatherMapWeatherConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class OpenWeatherMapClient  {

    @Autowired
    @Qualifier("openWeatherWebClient")
    private WebClient webClient;
    @Autowired
    private OpenWeatherMapWeatherConfig openWeatherMapWeatherConfig;

    public String getCurrentWeatherByCityNameAPI(String city) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("q", city)
                        .queryParam("appid", openWeatherMapWeatherConfig.getAppid())
                        .queryParam("units", openWeatherMapWeatherConfig.getUnits())
                        .queryParam("lang", openWeatherMapWeatherConfig.getLang())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10)) // 超过 10 秒则抛出 TimeoutException
                .block();
    }

    public String getCurrentWeatherByCityIdAPI(String cityId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("id", cityId)
                        .queryParam("appid", openWeatherMapWeatherConfig.getAppid())
                        .queryParam("units", openWeatherMapWeatherConfig.getUnits())
                        .queryParam("lang", openWeatherMapWeatherConfig.getLang())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 注意：block() 是阻塞的，仅用于演示
    }

    public String getCurrentWeatherByZIPCodeAPI(String ZIPCode) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("zip", ZIPCode)
                        .queryParam("appid", openWeatherMapWeatherConfig.getAppid())
                        .queryParam("units", openWeatherMapWeatherConfig.getUnits())
                        .queryParam("lang", openWeatherMapWeatherConfig.getLang())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 注意：block() 是阻塞的，仅用于演示
    }


    // 获取英文城市名（Geocoding API）
    public String getGeocodingByCityNameAPI(String localCityName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geo/1.0/direct")
                        .queryParam("q", localCityName)
                        .queryParam("limit", 1)
                        .queryParam("appid", openWeatherMapWeatherConfig.getAppid())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.fixedDelay(1, Duration.ofSeconds(1)))
                .block();
    }

    public String get3Hours5DayForecastByCityNameAPI(String cityName) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/forecast")
                        .queryParam("q", cityName)
                        .queryParam("appid", openWeatherMapWeatherConfig.getAppid())
                        .queryParam("units", openWeatherMapWeatherConfig.getUnits())
                        .queryParam("lang", openWeatherMapWeatherConfig.getLang())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}