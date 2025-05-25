package com.zzay.fengxv_weather.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzay.fengxv_weather.domain.dto.TencentWeatherForecast1HoursDTO;
import com.zzay.fengxv_weather.domain.dto.TencentWeatherForecast24HoursDTO;
import com.zzay.fengxv_weather.domain.po.AmapGeo;
import com.zzay.fengxv_weather.domain.po.TencentWeatherForecast1h;
import com.zzay.fengxv_weather.domain.po.TencentWeatherForecast24h;
import com.zzay.fengxv_weather.mapper.TencentWeatherForecast24hMapper;
import com.zzay.fengxv_weather.service.GeocodingService;
import com.zzay.fengxv_weather.service.ITencentWeatherForecast24hService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzay.fengxv_weather.utils.CacheUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Zzay
 * @since 2025-05-24
 */
@Service
public class TencentWeatherForecast24hServiceImpl extends ServiceImpl<TencentWeatherForecast24hMapper, TencentWeatherForecast24h> implements ITencentWeatherForecast24hService {
    @Autowired
    private GeocodingService geocodingService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CacheUtil cacheUtil;

    @SneakyThrows
    @Override
    public TencentWeatherForecast24HoursDTO getDataFromTencentWeather24h(String city) {
        AmapGeo geocodingByCityNameOnAmap = geocodingService.getGeocodingByCityNameOnAmap(city);
        String cacheKey = "weather:" + city;
        String json = cacheUtil.getOrFetchWeatherRedis(
                cacheKey,
                () -> {
                    JsonNode jsonNode = null;

                    jsonNode = callPythonScriptFor24h(geocodingByCityNameOnAmap);

                    return jsonNode.toString(); // 返回 JSON 字符串给 Redis 和 Save 方法
                },
                (resultJson) -> saveTencentWeatherFor24h(resultJson, geocodingByCityNameOnAmap.getProvince(), geocodingByCityNameOnAmap.getCity())

        );

        // 封装为对象
        // 解析为完整响应对象
        JsonNode root = objectMapper.readTree(json);
        JsonNode forecast24HNode = root.path("forecast_24h");

        TencentWeatherForecast24HoursDTO tencentWeatherForecast24HoursDTO= new TencentWeatherForecast24HoursDTO();

        // 将 forecast_1h 映射为 Map<String, TencentWeatherForecast1h>
        tencentWeatherForecast24HoursDTO.setForecast24h(objectMapper.convertValue(forecast24HNode, new TypeReference<Map<String, TencentWeatherForecast24h>>() {}));

        return tencentWeatherForecast24HoursDTO;
    }

    @SneakyThrows
    private Boolean saveTencentWeatherFor24h(String resultJson, String province, String city) {
        try {
            JsonNode root = objectMapper.readTree(resultJson);
            JsonNode forecast24HNode = root.path("forecast_24h");

            if (!forecast24HNode.isObject()) {
                throw new RuntimeException("JSON 数据中未找到有效的 forecast_1h 字段");
            }

            Iterator<Map.Entry<String, JsonNode>> fields = forecast24HNode.fields();

            while (fields.hasNext()) {
                JsonNode node = fields.next().getValue();

                // 构造实体对象
                TencentWeatherForecast24h tencentWeatherForecast24h = new TencentWeatherForecast24h();
                // 设置日期
                tencentWeatherForecast24h.setForecastDate(LocalDate.parse(node.path("time").asText()));  // "2025-05-23"

                // 白天天气
                tencentWeatherForecast24h.setDayWeather(node.path("day_weather").asText());
                tencentWeatherForecast24h.setDayWeatherCode(node.path("day_weather_code").asText());
                tencentWeatherForecast24h.setDayWeatherShort(node.path("day_weather_short").asText());
                tencentWeatherForecast24h.setDayWeatherUrl(node.path("day_weather_url").asText());
                tencentWeatherForecast24h.setDayWindDirection(node.path("day_wind_direction").asText());
                tencentWeatherForecast24h.setDayWindDirectionCode(node.path("day_wind_direction_code").asText());
                tencentWeatherForecast24h.setDayWindPower(node.path("day_wind_power").asText());
                tencentWeatherForecast24h.setDayWindPowerCode(node.path("day_wind_power_code").asText());

                // 晚上天气
                tencentWeatherForecast24h.setNightWeather(node.path("night_weather").asText());
                tencentWeatherForecast24h.setNightWeatherCode(node.path("night_weather_code").asText());
                tencentWeatherForecast24h.setNightWeatherShort(node.path("night_weather_short").asText());
                tencentWeatherForecast24h.setNightWeatherUrl(node.path("night_weather_url").asText());
                tencentWeatherForecast24h.setNightWindDirection(node.path("night_wind_direction").asText());
                tencentWeatherForecast24h.setNightWindDirectionCode(node.path("night_wind_direction_code").asText());
                tencentWeatherForecast24h.setNightWindPower(node.path("night_wind_power").asText());
                tencentWeatherForecast24h.setNightWindPowerCode(node.path("night_wind_power_code").asText());

                // 温度
                tencentWeatherForecast24h.setMaxDegree(node.path("max_degree").asText());
                tencentWeatherForecast24h.setMinDegree(node.path("min_degree").asText());

                // 空气质量
                tencentWeatherForecast24h.setAqiLevel(node.path("aqi_level").asInt());
                tencentWeatherForecast24h.setAqiName(node.path("aqi_name").asText());
                tencentWeatherForecast24h.setAqiUrl(node.path("aqi_url").asText());

                // 地理信息
                tencentWeatherForecast24h.setProvince(province);
                tencentWeatherForecast24h.setCity(city);

                // 存储到数据库
                this.save(tencentWeatherForecast24h);
            }

            return true;

        } catch (Exception e) {
            log.error("保存数据失败");
            return false;
        }

    }

    @SneakyThrows
    private JsonNode callPythonScriptFor24h(AmapGeo geocodingByCityNameOnAmap) {
        // 构建命令行参数(配置)
        ProcessBuilder processBuilder = new ProcessBuilder(
                "D:\\Program Files (x86)\\Python\\Python3.11.5\\python.exe",
                "E:\\Code_java\\fengxv_weather\\fengxv_web\\src\\main\\java\\com\\zzay\\fengxv_weather\\python\\TencentWeatherForecast24hPython.py",
                geocodingByCityNameOnAmap.getProvince(),
                geocodingByCityNameOnAmap.getCity()
        );

        // 合并标准输出和错误输出(配置2)
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // 先读取输出流的内容
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        // 然后再等待执行完成
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Python script 执行失败: 退出码=" + exitCode + ", 输出=" + output);
        }

        // 解析 JSON 输出
        return objectMapper.readTree(output.toString());
    }
}
