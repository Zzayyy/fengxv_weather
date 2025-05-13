package com.zzay.fengxv_weather.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzay.fengxv_weather.domain.po.City;
import com.zzay.fengxv_weather.domain.po.CurrentWeather;
import com.zzay.fengxv_weather.mapper.CityMapper;
import com.zzay.fengxv_weather.mapper.CurrentWeatherMapper;
import com.zzay.fengxv_weather.service.ICurrentWeatherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzay.fengxv_weather.weatherClient.OpenWeatherMapClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Override
    public String getCurrentWeatherByCityName(String city) {
        String currentWeatherByCityName = openWeatherMapClient.getCurrentWeatherByCityName(city);
        saveWeatherData(currentWeatherByCityName);
        return currentWeatherByCityName;
    }

    @Override
    public String getCurrentWeatherBYCityId(String cityId) {
        String currentWeatherByCityId = openWeatherMapClient.getCurrentWeatherByCityId(cityId);
        saveWeatherData(currentWeatherByCityId);
        return currentWeatherByCityId;
    }

    @Override
    public String getCurrentWeatherByZIPCode(String zipCode) {
        String currentWeatherByZIPCode = openWeatherMapClient.getCurrentWeatherByZIPCode(zipCode);
        saveWeatherData(currentWeatherByZIPCode);
        return currentWeatherByZIPCode;
    }


    /**
     * 获取城市天气并保存到数据库
     */
    public boolean saveWeatherData(String json) {
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

            this.save(weather);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
