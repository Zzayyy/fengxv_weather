package com.zzay.fengxv_weather.weatherClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class AmapClient {
    @Autowired
    @Qualifier("amapWebClient")
    private WebClient webClient;

    public String getGeocoding(String data) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v3/geocode/geo")
                        .queryParam("address", data)
                        .queryParam("key", "68804100d724401022c6ac53586a382d")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10)) // 超过 10 秒则抛出 TimeoutException
                .block();
    }

}
