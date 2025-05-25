package com.zzay.fengxv_weather.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.BeanUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzay.fengxv_weather.domain.dto.CurrentWeatherDTO;
import com.zzay.fengxv_weather.domain.dto.GeocodingDTO;
import com.zzay.fengxv_weather.domain.po.AmapGeo;
import com.zzay.fengxv_weather.domain.po.City;
import com.zzay.fengxv_weather.domain.po.CurrentWeather;
import com.zzay.fengxv_weather.mapper.CityMapper;
import com.zzay.fengxv_weather.mapper.CurrentWeatherMapper;
import com.zzay.fengxv_weather.service.GeocodingService;
import com.zzay.fengxv_weather.service.ICurrentWeatherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzay.fengxv_weather.weatherClient.OpenWeatherMapClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Zzay
 * @since 2025-05-08
 */
@Service
public class CurrentWeatherServiceImpl extends ServiceImpl<CurrentWeatherMapper, CurrentWeather> implements ICurrentWeatherService {

    @Autowired
    private OpenWeatherMapClient openWeatherMapClient;
    @Autowired
    private ObjectMapper objectMapper;  // 用于解析 JSON
    @Autowired
    private CityMapper cityMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private GeocodingService geocodingService;

    @Override
    public CurrentWeatherDTO getCurrentWeatherByCityName(String city) {
        GeocodingDTO geocodingByCityName = geocodingService.getGeocodingByCityName(city);
        String englishCityName = geocodingByCityName.getName();
        String cacheKey = "weather:" + city;
        String json = getOrFetchWeatherRedis(cacheKey, () -> openWeatherMapClient.getCurrentWeatherByCityNameAPI(englishCityName));
        try {
            CurrentWeatherDTO currentWeatherDTO = objectMapper.readValue(json, CurrentWeatherDTO.class);
            currentWeatherDTO.setLocalName(city);
            return currentWeatherDTO;
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败", e);
        }
    }


    @Override
    public String getCurrentWeatherBYCityId(String cityId) {
        String cacheKey = "weather:" + cityId;
        return getOrFetchWeatherRedis(cacheKey, () -> openWeatherMapClient.getCurrentWeatherByCityIdAPI(cityId));
    }

    @Override
    public String getCurrentWeatherByZIPCode(String zipCode) {
        String cacheKey = "weather" + zipCode;
        return getOrFetchWeatherRedis(cacheKey, () -> openWeatherMapClient.getCurrentWeatherByZIPCodeAPI(zipCode));
    }




    /**
     * 获取OpenWeather城市天气并保存到数据库
     */
    public boolean saveOpenWeatherData(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            Integer cityId = root.get("id").asInt();

            // ---------- 1. 先判断 city 是否存在 ----------
            City existingCity = cityMapper.selectById(cityId);
            if (existingCity == null) {
                City city = new City();
                city.setId(cityId);
                city.setName(root.get("name").asText());
                city.setCountry(root.get("sys").get("country").asText());

                JsonNode coord = root.get("coord");
                city.setLon(BigDecimal.valueOf(coord.get("lon").asDouble()));
                city.setLat(BigDecimal.valueOf(coord.get("lat").asDouble()));

                city.setTimezone(root.get("timezone").asInt());
                city.setSunrise(root.get("sys").get("sunrise").asInt());
                city.setSunset(root.get("sys").get("sunset").asInt());
                city.setCreateTime(LocalDateTime.now());
                city.setUpdateTime(LocalDateTime.now());

                cityMapper.insert(city);
            }


            // ---------- 2. 构造并保存天气数据 ----------
            CurrentWeather weather = new CurrentWeather();
            weather.setCityId(root.get("id").asInt());
            weather.setWeatherMain(root.get("weather").get(0).get("main").asText());
            weather.setWeatherDescription(root.get("weather").get(0).get("description").asText());
            weather.setIcon(root.get("weather").get(0).get("icon").asText());

            JsonNode main = root.get("main");
            weather.setTemp(BigDecimal.valueOf(main.get("temp").asDouble()));
            weather.setFeelsLike(BigDecimal.valueOf(main.get("feels_like").asDouble()));
            weather.setTempMin(BigDecimal.valueOf(main.get("temp_min").asDouble()));
            weather.setTempMax(BigDecimal.valueOf(main.get("temp_max").asDouble()));
            weather.setPressure(main.get("pressure").asInt());
            weather.setHumidity(main.get("humidity").asInt());
            weather.setSeaLevel(main.has("sea_level") ? main.get("sea_level").asInt() : null);
            weather.setGrndLevel(main.has("grnd_level") ? main.get("grnd_level").asInt() : null);

            weather.setVisibility(root.get("visibility").asInt());

            JsonNode wind = root.get("wind");
            weather.setWindSpeed(BigDecimal.valueOf(wind.get("speed").asDouble()));
            weather.setWindDeg(wind.get("deg").asInt());
            weather.setWindGust(wind.has("gust") ? BigDecimal.valueOf(wind.get("gust").asDouble()) : null);

            weather.setCloudAll(root.get("clouds").get("all").asInt());
            weather.setDt(root.get("dt").asInt());

            weather.setCreateTime(LocalDateTime.now());
            weather.setUpdateTime(LocalDateTime.now());

            BeanUtil.copyProperties(weather, CurrentWeatherDTO.class);

            this.save(weather);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public String getOrFetchWeatherRedis(String cacheKey, Supplier<String> apiCall) {
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

        // 4. 保存数据到数据库
        if (!saveOpenWeatherData(result)) {
            throw new RuntimeException("天气数据保存到数据库失败");
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
