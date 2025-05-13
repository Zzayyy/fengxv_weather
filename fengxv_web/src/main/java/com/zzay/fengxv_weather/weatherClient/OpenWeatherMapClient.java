package com.zzay.fengxv_weather.weatherClient;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenWeatherMapClient  {

    private final WebClient webClient;


    public OpenWeatherMapClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public String getCurrentWeatherByCityName(String city) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("q", city)
                        .queryParam("appid", "85a206bcf67aec117ce89505717ec31c")
                        .queryParam("units", "metric")
                        .queryParam("lang", "zh_cn")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 注意：block() 是阻塞的，仅用于演示
    }

    public String getCurrentWeatherByCityId(String cityId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("id", cityId)
                        .queryParam("appid", "85a206bcf67aec117ce89505717ec31c")
                        .queryParam("units", "metric")
                        .queryParam("lang", "zh_cn")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 注意：block() 是阻塞的，仅用于演示
    }

    public String getCurrentWeatherByZIPCode(String ZIPCode) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/weather")
                        .queryParam("zip", ZIPCode)
                        .queryParam("appid", "85a206bcf67aec117ce89505717ec31c")
                        .queryParam("units", "metric")
                        .queryParam("lang", "zh_cn")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 注意：block() 是阻塞的，仅用于演示
    }
}