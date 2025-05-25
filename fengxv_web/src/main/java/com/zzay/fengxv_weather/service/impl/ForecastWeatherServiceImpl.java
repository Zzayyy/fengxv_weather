package com.zzay.fengxv_weather.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzay.fengxv_weather.domain.dto.CurrentWeatherDTO;
import com.zzay.fengxv_weather.domain.dto.GeocodingDTO;
import com.zzay.fengxv_weather.domain.po.City;
import com.zzay.fengxv_weather.domain.po.ForecastWeather;
import com.zzay.fengxv_weather.mapper.CityMapper;
import com.zzay.fengxv_weather.mapper.ForecastWeatherMapper;
import com.zzay.fengxv_weather.service.GeocodingService;
import com.zzay.fengxv_weather.service.IForecastWeatherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzay.fengxv_weather.utils.CacheUtil;
import com.zzay.fengxv_weather.weatherClient.OpenWeatherMapClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Zzay
 * @since 2025-05-16
 */
@Service
public class ForecastWeatherServiceImpl extends ServiceImpl<ForecastWeatherMapper, ForecastWeather> implements IForecastWeatherService {

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
    public String get5Day3hourForecastWeather(String localCityName) {
        GeocodingDTO geocodingByCityName = geocodingService.getGeocodingByCityName(localCityName);
        String englishCityName = geocodingByCityName.getName();
        String cacheKey = "weather:" + localCityName;

        String json = getOrFetchWeatherRedis(cacheKey, () -> openWeatherMapClient.get3Hours5DayForecastByCityNameAPI(englishCityName));
        return json;
//        try {
//            CurrentWeatherDTO currentWeatherDTO = objectMapper.readValue(json, CurrentWeatherDTO.class);
//            currentWeatherDTO.setLocalName(localCityName);
//            return currentWeatherDTO;
//        } catch (Exception e) {
//            throw new RuntimeException("JSON 解析失败", e);
//        }
    }

    public boolean saveForecastWeatherData(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            // 获取城市 ID
            Integer cityId = root.get("city").get("id").asInt();

            // ---------- 1. 先判断 city 是否存在 ----------
            City existingCity = cityMapper.selectById(cityId);
            if (existingCity == null) {
                City city = new City();
                city.setId(cityId);
                city.setName(root.get("city").get("name").asText());
                city.setCountry(root.get("city").get("country").asText());

                JsonNode coord = root.get("city").get("coord");
                city.setLon(BigDecimal.valueOf(coord.get("lat").asDouble()));
                city.setLat(BigDecimal.valueOf(coord.get("lon").asDouble()));

                city.setTimezone(root.get("city").get("timezone").asInt());
                city.setSunrise(root.get("city").get("sunrise").asInt());
                city.setSunset(root.get("city").get("sunset").asInt());
                city.setCreateTime(LocalDateTime.now());
                city.setUpdateTime(LocalDateTime.now());

                cityMapper.insert(city);
            }

            // ---------- 2. 遍历 list 保存每一条 forecast 数据 ----------
            JsonNode forecasts = root.get("list");

            for (JsonNode node : forecasts) {
                ForecastWeather forecast = new ForecastWeather();

                forecast.setCityId(cityId);
                forecast.setDt(node.get("dt").asLong());

                // main 字段
                JsonNode main = node.get("main");
                forecast.setTemp(BigDecimal.valueOf(main.get("temp").asDouble()));
                forecast.setFeelsLike(BigDecimal.valueOf(main.get("feels_like").asDouble()));
                forecast.setTempMin(BigDecimal.valueOf(main.get("temp_min").asDouble()));
                forecast.setTempMax(BigDecimal.valueOf(main.get("temp_max").asDouble()));
                forecast.setPressure(main.get("pressure").asInt());
                forecast.setSeaLevel(main.has("sea_level") ? main.get("sea_level").asInt() : null);
                forecast.setGrndLevel(main.has("grnd_level") ? main.get("grnd_level").asInt() : null);
                forecast.setHumidity(main.get("humidity").asInt());
                forecast.setTempKf(main.has("temp_kf") ? BigDecimal.valueOf(main.get("temp_kf").asDouble()) : null);

                // weather 字段
                JsonNode weather = node.get("weather").get(0);
                forecast.setWeatherId(weather.get("id").asInt());
                forecast.setWeatherMain(weather.get("main").asText());
                forecast.setWeatherDescription(weather.get("description").asText());
                forecast.setWeatherIcon(weather.get("icon").asText());

                // clouds
                forecast.setCloudsAll(node.get("clouds").get("all").asInt());

                // wind
                JsonNode wind = node.get("wind");
                forecast.setWindSpeed(BigDecimal.valueOf(wind.get("speed").asDouble()));
                forecast.setWindDeg(wind.get("deg").asInt());
                forecast.setWindGust(wind.has("gust") ? BigDecimal.valueOf(wind.get("gust").asDouble()) : null);

                // visibility
                forecast.setVisibility(node.get("visibility").asInt());

                // pop
                forecast.setPop(node.has("pop") ? BigDecimal.valueOf(node.get("pop").asDouble()) : null);

                // rain
                if (node.has("rain") && node.get("rain").has("3h")) {
                    forecast.setRain_3h(BigDecimal.valueOf(node.get("rain").get("3h").asDouble()));
                } else {
                    forecast.setRain_3h(null);
                }

                // sys
                forecast.setSysPod(node.get("sys").get("pod").asText());

                // dt_txt
                String dtTxtStr = node.get("dt_txt").asText();
                LocalDateTime dtTxt = LocalDateTime.parse(dtTxtStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                forecast.setDtTxt(dtTxt);

                // 设置创建/更新时间
                forecast.setCreateTime(LocalDateTime.now());
                forecast.setUpdateTime(LocalDateTime.now());

                // 插入数据库
                this.save(forecast);
            }

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
        if (!saveForecastWeatherData(result)) {
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
