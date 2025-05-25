package com.zzay.fengxv_weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.time.Duration;

@Configuration
public class OpenWeatherMapWebClientConfig {

    @Bean("openWeatherWebClient")
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30)) // 设置响应超时
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30)))// 读取超时
                .proxy(proxySpec -> proxySpec
                        .type(ProxyProvider.Proxy.HTTP)
                        .host("localhost") // 替换为你的代理主机地址
                        .port(10809)); // 替换为你的代理端口

        return WebClient.builder()
                .baseUrl("https://api.openweathermap.org")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
