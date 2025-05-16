package com.zzay.fengxv_weather.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzay.fengxv_weather.domain.dto.CurrentWeatherDTO;
import com.zzay.fengxv_weather.domain.dto.GeocodingDTO;
import com.zzay.fengxv_weather.service.GeocodingService;
import com.zzay.fengxv_weather.weatherClient.OpenWeatherMapClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Service
public class GeocodingServiceImpl implements GeocodingService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private OpenWeatherMapClient openWeatherMapClient;
    @Autowired
    private ObjectMapper  objectMapper;

    @Override
    public GeocodingDTO getGeocodingByCityName(String localCityName) {
        String cacheKey = "geocoding:" + localCityName;
        String json = getOrFetchGeocodingRedis(cacheKey, () -> openWeatherMapClient.getGeocodingByCityName(localCityName));
        try {
            // 使用 List 接收 OpenWeatherMap 的数组响应
            List<GeocodingDTO> dtos = objectMapper.readValue(json, new TypeReference<List<GeocodingDTO>>() {});

            if (dtos != null && !dtos.isEmpty()) {
                GeocodingDTO dto = dtos.get(0);
                dto.setLocalName(localCityName);  // 设置本地城市名
                return dto;
            } else {
                throw new RuntimeException("未找到对应城市的英文名: " + localCityName);
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败", e);
        }
    }

    public String getOrFetchGeocodingRedis(String cacheKey, Supplier<String> apiCall) {


        // 1. 尝试读取缓存
        String cached = null;
        try {
            cached = redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            // 日志：Redis 读取失败，但不阻止业务继续执行
            System.err.println("Redis 读取失败：" + e.getMessage());
        }

        // 2. 如果缓存命中，直接返回
        if (cached != null) {
            return cached;
        }

        // 3. 未命中缓存，调用 API
        String result;
        try {
            result = apiCall.get();
        } catch (Exception e) {
            throw new RuntimeException("远程 API 调用失败：" + e.getMessage(), e);
        }
        // 5. 尝试写入缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(10));
        } catch (Exception e) {
            // 日志：Redis 写入失败，但不影响主流程
            System.err.println("Redis 写入失败：" + e.getMessage());
        }
        return result;
    }


}
