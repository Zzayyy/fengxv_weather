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


    @SneakyThrows
    @Override
    public TencentWeatherForecast1HoursDTO getDataFromTencentWeather(String city) {
        AmapGeo geocodingByCityNameOnAmap = geocodingService.getGeocodingByCityNameOnAmap(city);
        String cacheKey = "weather:" + city;
        String json = cacheUtil.getOrFetchWeatherRedis(
                cacheKey,
                () -> {
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = callPythonScriptFor1h(geocodingByCityNameOnAmap);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException("python script 运行失败");
                    }
                    return jsonNode.toString(); // 返回 JSON 字符串给 Redis 和 Save 方法
                },
                (resultJson) -> saveTencentWeather(resultJson, geocodingByCityNameOnAmap.getProvince(), geocodingByCityNameOnAmap.getCity())

        );

        // 封装为对象
        // 解析为完整响应对象
        JsonNode root = objectMapper.readTree(json);
        JsonNode forecast1HNode = root.path("forecast_1h");

        TencentWeatherForecast1HoursDTO tencentWeatherForecast1HoursDTO = new TencentWeatherForecast1HoursDTO();

        // 将 forecast_1h 映射为 Map<String, TencentWeatherForecast1h>
        tencentWeatherForecast1HoursDTO.setForecast1h(objectMapper.convertValue(forecast1HNode, new TypeReference<Map<String, TencentWeatherForecast1h>>() {}));

        return tencentWeatherForecast1HoursDTO;

    }


    // 执行python文件获取腾讯天气
    public JsonNode callPythonScriptFor1h(AmapGeo amapGeo) throws IOException, InterruptedException {
        // 构建命令行参数(配置)
        ProcessBuilder processBuilder = new ProcessBuilder(
                "D:\\Program Files (x86)\\Python\\Python3.11.5\\python.exe",
                "E:\\Code_java\\fengxv_weather\\fengxv_web\\src\\main\\java\\com\\zzay\\fengxv_weather\\python\\TencentWeatherForecast1hPython.py",
                amapGeo.getProvince(),
                amapGeo.getCity()
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

                // 构造实体对象
                TencentWeatherForecast1h tencentWeatherForecast1h = new TencentWeatherForecast1h();

                tencentWeatherForecast1h.setForecastTime(node.path("update_time").asText());
                tencentWeatherForecast1h.setDegree(node.path("degree").asText());
                tencentWeatherForecast1h.setWeather(node.path("weather").asText());
                tencentWeatherForecast1h.setWeatherCode(node.path("weather_code").asText());
                tencentWeatherForecast1h.setWeatherShort(node.path("weather_short").asText());
                tencentWeatherForecast1h.setWeatherUrl(node.path("weather_url").asText());
                tencentWeatherForecast1h.setWindDirection(node.path("wind_direction").asText());
                tencentWeatherForecast1h.setWindPower(node.path("wind_power").asText());
                tencentWeatherForecast1h.setProvince(province);
                tencentWeatherForecast1h.setCity(city);

                // 调用 Mapper 或 JPA 存储
                this.save(tencentWeatherForecast1h); // 假设你有这个 mapper
            }

            return true;

        } catch (Exception e) {
            log.error("保存数据失败: {}", e.getMessage());
            return false;
        }

    }

}




