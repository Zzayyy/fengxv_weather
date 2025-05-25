package com.zzay.fengxv_weather.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class RestApiWebClientConfig {
    @Bean("amapWebClient")
    public WebClient RestApiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30)) // 设置响应超时
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30)));// 读取超时
//                .proxy(proxySpec -> proxySpec
//                        .type(ProxyProvider.Proxy.HTTP)
//                        .host("localhost") // 替换为你的代理主机地址
//                        .port(10809)); // 替换为你的代理端口

        return WebClient.builder()
                .baseUrl("https://restapi.amap.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
