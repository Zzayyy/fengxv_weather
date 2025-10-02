package com.zzay.fengxv_weather.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzay.fengxv_weather.domain.dto.TencentWeatherForecast1HoursDTO;
import com.zzay.fengxv_weather.domain.po.AmapGeo;
import com.zzay.fengxv_weather.domain.po.TencentWeatherForecast1h;
import com.zzay.fengxv_weather.mapper.TencentWeatherForecast1hMapper;
import com.zzay.fengxv_weather.service.GeocodingService;
import com.zzay.fengxv_weather.service.ITencentWeatherForecast1hService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzay.fengxv_weather.utils.CacheUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Zzay
 * @since 2025-05-23
 */
@Service
@Slf4j
public class TencentWeatherForecast1hServiceImpl extends ServiceImpl<TencentWeatherForecast1hMapper, TencentWeatherForecast1h> implements ITencentWeatherForecast1hService {
    @Autowired
    private GeocodingService geocodingService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CacheUtil cacheUtil;

    private final RestTemplate restTemplate = new RestTemplate();  // 用于请求 FastAPI


    @SneakyThrows
    @Override
    public TencentWeatherForecast1HoursDTO getDataFromTencentWeather(String city) {
        // 先用高德定位
        AmapGeo geocodingByCityNameOnAmap = geocodingService.getGeocodingByCityNameOnAmap(city);

        String cacheKey = "weather:" + city;

        // 从缓存中获取，如果没有就调用 FastAPI
        String json = cacheUtil.getOrFetchWeatherRedis(
                cacheKey,
                () -> {
                    JsonNode jsonNode;
                    try {
                        jsonNode = callFastApiFor1h(geocodingByCityNameOnAmap);
                    } catch (Exception e) {
                        throw new RuntimeException("调用 FastAPI 失败", e);
                    }
                    return jsonNode.toString(); // 缓存 JSON
                },
                (resultJson) -> saveTencentWeather(resultJson, geocodingByCityNameOnAmap.getProvince(), geocodingByCityNameOnAmap.getCity())
        );

        // 解析 JSON
        JsonNode root = objectMapper.readTree(json);
        JsonNode forecast1HNode = root.path("forecast_1h");

        TencentWeatherForecast1HoursDTO dto = new TencentWeatherForecast1HoursDTO();
        dto.setForecast1h(objectMapper.convertValue(forecast1HNode, new TypeReference<Map<String, TencentWeatherForecast1h>>() {}));

        return dto;
    }

    /**
     * 调用 FastAPI 的接口
     */
    public JsonNode callFastApiFor1h(AmapGeo amapGeo) throws IOException {
        // 拼接 FastAPI 的 URL
        String url = String.format(
                "http://127.0.0.1:8000/weather/forecast1h?province=%s&city=%s&county=%s",
                amapGeo.getProvince(),
                amapGeo.getCity(),
                amapGeo.getDistrict()
        );

        // 调用 FastAPI
        String response = restTemplate.getForObject(url, String.class);

        if (response == null) {
            throw new RuntimeException("FastAPI 返回空数据");
        }

        // 转换为 JSON
        return objectMapper.readTree(response);
    }

    @SneakyThrows
    public Boolean saveTencentWeather(String json, String province, String city) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode forecast1HNode = root.path("forecast_1h");

            if (!forecast1HNode.isObject()) {
                throw new RuntimeException("JSON 数据中未找到有效的 forecast_1h 字段");
            }

            Iterator<Map.Entry<String, JsonNode>> fields = forecast1HNode.fields();

            while (fields.hasNext()) {
                JsonNode node = fields.next().getValue();

                TencentWeatherForecast1h entity = new TencentWeatherForecast1h();
                entity.setForecastTime(node.path("update_time").asText());
                entity.setDegree(node.path("degree").asText());
                entity.setWeather(node.path("weather").asText());
                entity.setWeatherCode(node.path("weather_code").asText());
                entity.setWeatherShort(node.path("weather_short").asText());
                entity.setWeatherUrl(node.path("weather_url").asText());
                entity.setWindDirection(node.path("wind_direction").asText());
                entity.setWindPower(node.path("wind_power").asText());
                entity.setProvince(province);
                entity.setCity(city);

                this.save(entity);
            }

            return true;
        } catch (Exception e) {
            log.error("保存数据失败: {}", e.getMessage(), e);
            return false;
        }
    }


}




