package com.zzay.fengxv_weather.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzay.fengxv_weather.domain.dto.AiRequest;
import com.zzay.fengxv_weather.domain.dto.WeatherLishiAnalysisDTO;
import com.zzay.fengxv_weather.domain.result.Result;
import com.zzay.fengxv_weather.utils.CacheUtil;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/weather/ai")
public class AiController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Operation(summary = "AI 回答：分析2025年月度天气数据")
    @PostMapping("/analyze/")
    public Result<String> aiServer(@RequestParam String request) {
        // 拼接 FastAPI 的 URL
        String url = String.format(
                "http://127.0.0.1:8000/weather/ai?content=%s",
                request
        );

        // 调用 FastAPI
        String response = restTemplate.getForObject(url, String.class);
        System.out.println(response);
        // 去除外层双引号（如果存在）
        if (response.startsWith("\"") && response.endsWith("\"")) {
            // 使用 JSON 反序列化安全地去除引号和转义字符
            try {
                response = new ObjectMapper().readValue(response, String.class);
            } catch (Exception e) {
                // fallback: 简单去除首尾引号（不处理内部转义）
                response = response.substring(1, response.length() - 1);
            }
        }
        return Result.success(response);
    }
}
