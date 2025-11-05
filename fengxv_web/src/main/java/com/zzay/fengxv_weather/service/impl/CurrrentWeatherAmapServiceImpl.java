package com.zzay.fengxv_weather.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzay.fengxv_weather.domain.dto.*;
import com.zzay.fengxv_weather.domain.po.CurrrentWeatherAmap;
import com.zzay.fengxv_weather.mapper.CurrrentWeatherAmapMapper;
import com.zzay.fengxv_weather.service.ICurrrentWeatherAmapService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzay.fengxv_weather.weatherClient.AmapClient;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import scala.Function1;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * <p>
 * 高德天气API数据表 服务实现类
 * </p>
 *
 * @author Zzay
 * @since 2025-10-08
 */
@Service
public class CurrrentWeatherAmapServiceImpl extends ServiceImpl<CurrrentWeatherAmapMapper, CurrrentWeatherAmap> implements ICurrrentWeatherAmapService {

    @Autowired
    private AmapClient amapClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SparkSession sparkSession;

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MILLIS = 1000;

    private static final List<String> MAP_DISPLAY_CITIES = Arrays.asList(
            "北京", "天津", "河北", "山西", "内蒙古",
            "辽宁", "吉林", "黑龙江",
            "上海", "江苏", "浙江", "安徽", "福建", "江西", "山东",
            "河南", "湖北", "湖南",
            "广东", "广西", "海南",
            "重庆", "四川", "贵州", "云南", "西藏",
            "陕西", "甘肃", "青海", "宁夏", "新疆"
    );

    // ========================
    // 1. 普通查询（走缓存）
    // ========================
    @Override
    public CurrrentWeatherAmap getWeatherByCityName(String cityName) {
        String cacheKey = "weatherAmap:" + cityName;
        String json = getOrFetchWeatherRedis(cacheKey, () -> amapClient.getWeatherAmap(cityName));
        return parseWeatherJson(json);      // 存 redis和mysql api
    }

    // ========================
    // 2. 前端地图展示（走缓存，不更新 DB）
    // ========================
    @Override
    public List<WeatherMapDataDTO> getWeatherDataForMap() {
        String cacheKey = "MapSparkDatas:all"; // 统一缓存 key



        // 1. 尝试从 Redis 读取缓存
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                // 反序列化 JSON 到 List<WeatherMapDataDTO>
                return objectMapper.readValue(cachedJson, new TypeReference<List<WeatherMapDataDTO>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse cached weather map data, falling back to fetch: {}");
            }
        }

        // 2. 缓存未命中，执行原始逻辑
        this.remove(new QueryWrapper<>());
        List<WeatherMapDataDTO> result = new ArrayList<>();
        for (String cityName : MAP_DISPLAY_CITIES) {
            try {
                CurrrentWeatherAmap weatherData = this.getWeatherByCityName(cityName);
                if (weatherData != null) {
                    Double temperature = extractTemperature(weatherData, cityName);
                    if (temperature != null) {
                        result.add(new WeatherMapDataDTO(cityName, temperature));
                    }
                }
            } catch (Exception e) {
                System.err.println("获取城市 " + cityName + " 天气数据时出错: " + e.getMessage());
                // 可考虑记录日志：log.error("...", e);
            }
        }

        // 3. 写入 Redis 缓存（设置过期时间，例如 10 分钟）
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(24));
        } catch (Exception e) {
            log.warn("Failed to cache weather map data: {}");
            // 即使缓存失败，也不影响返回结果
        }

        return result;
    }

    // ========================
    // 4. Spark 分析接口（依赖数据库）
    // ========================
    @Override
    public List<WeatherAmapSparkDTO> getWeatherSpark() {
        String cacheKey = "weatherMap:spark:temperature";

        // 1. 尝试从 Redis 读取缓存
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                return objectMapper.readValue(cachedJson, new TypeReference<List<WeatherAmapSparkDTO>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse cached weather spark data, falling back to compute: {}");
            }
        }
        // 2. 检查数据库中是否有数据
        if (!hasWeatherDataInDatabase()) {

            return new ArrayList<>();
        }
        // 2. 缓存未命中，执行 Spark 查询
        Dataset<Row> df = readWeatherTable();
        df.createOrReplaceTempView("weather_table");

        Dataset<Row> result = sparkSession.sql(
                "SELECT province, CAST(temperature_float AS FLOAT) AS temperatureFloat " +
                        "FROM weather_table"
        );
        List<WeatherAmapSparkDTO> sparkResult = result.as(Encoders.bean(WeatherAmapSparkDTO.class)).collectAsList();

        // 4. 只有在有数据时才写入缓存
        if (!sparkResult.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(sparkResult);
                redisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(24));
            } catch (Exception e) {
                log.warn("Failed to cache weather spark result: {}");
            }
        }

        return sparkResult;
    }

    @Override
    public List<WeatherAmapSparkHumidityDTO> getWeatherSparkHumidity() {
        String cacheKey = "weatherMap:spark:humidity";

        // 1. 尝试从 Redis 读取缓存
        String cachedJson = redisTemplate.opsForValue().get(cacheKey);
        if (cachedJson != null && !cachedJson.isEmpty()) {
            try {
                return objectMapper.readValue(cachedJson, new TypeReference<List<WeatherAmapSparkHumidityDTO>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse cached humidity spark data, falling back to compute: {}");
            }
        }
        // 2. 检查数据库中是否有数据
        if (!hasWeatherDataInDatabase()) {

            return new ArrayList<>();
        }

        // 2. 缓存未命中，执行 Spark 查询
        Dataset<Row> df = readWeatherTable();
        df.createOrReplaceTempView("weather2_table");

        Dataset<Row> result = sparkSession.sql(
                "SELECT province, CAST(humidity_float AS FLOAT) AS humidityFloat " +
                        "FROM weather2_table"
        );
        List<WeatherAmapSparkHumidityDTO> sparkResult = result.as(Encoders.bean(WeatherAmapSparkHumidityDTO.class)).collectAsList();


        // 4. 只有在有数据时才写入缓存
        if (!sparkResult.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(sparkResult);
                redisTemplate.opsForValue().set(cacheKey, json, Duration.ofHours(24));
            } catch (Exception e) {
                log.warn("Failed to cache weather spark result: {}");
            }
        }
        return sparkResult;
    }


    // ========================
    // 3. 强制刷新所有城市天气（用于 Spark 分析前调用！）
    // ========================
    public void refreshWeatherDataForMap() {
        // 清空旧数据
        boolean cleared = this.remove(new QueryWrapper<>());
        if (!cleared) {
            System.err.println("清空历史天气数据失败（可能表已为空）");
        }

        for (String cityName : MAP_DISPLAY_CITIES) {
            try {
                // 强制刷新：跳过缓存，调 API，写 DB
                String cacheKey = "weatherAmap:" + cityName;
                String json = getOrFetchWeatherRedis(cacheKey, () -> amapClient.getWeatherAmap(cityName));
                // 注意：saveAmapWeatherData 已在 getOrFetchWeatherRedis 中调用
            } catch (Exception e) {
                System.err.println("强制刷新城市 " + cityName + " 天气失败: " + e.getMessage());
            }
        }
    }


    // ========================
    // 核心方法：获取或拉取天气（支持强制刷新）
    // ========================
    public String getOrFetchWeatherRedis(String cacheKey, Supplier<String> apiCall) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                // 1. 如果不是强制刷新，尝试读缓存

                try {
                    String cached = redisTemplate.opsForValue().get(cacheKey);
                    if (cached != null) {
                        return cached;
                    }
                } catch (Exception e) {
                    System.err.println("Redis 读取失败：" + e.getMessage());
                }


                // 2. 调用 API
                String result;
                try {
                    result = apiCall.get();
                } catch (Exception e) {
                    throw new RuntimeException("远程 API 调用失败：" + e.getMessage(), e);
                }

                // 3. 保存到数据库（注意：不再清空！由上层控制）
                if (!saveAmapWeatherData(result)) {
                    throw new RuntimeException("天气数据保存到数据库失败");
                }

                // 4. 写入缓存
                try {
                    redisTemplate.opsForValue().set(cacheKey, result, Duration.ofHours(24));
                } catch (Exception e) {
                    System.err.println("Redis 写入失败：" + e.getMessage());
                }

                return result;

            } catch (Exception e) {
                lastException = e;
                attempt++;
                System.err.println("获取或保存天气数据失败 (尝试 " + attempt + "/" + MAX_RETRY_ATTEMPTS + "): " + e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MILLIS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试过程被中断", ie);
                    }
                }
            }
        }

        throw new RuntimeException("经过 " + MAX_RETRY_ATTEMPTS + " 次重试后仍然失败", lastException);
    }

    // ========================
    // 工具方法
    // ========================
    private CurrrentWeatherAmap parseWeatherJson(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            String status = rootNode.path("status").asText();
            JsonNode livesArray = rootNode.path("lives");

            if (!"1".equals(status) || !livesArray.isArray() || livesArray.isEmpty()) {
                System.err.println("API 返回数据状态异常或 lives 数据为空: status=" + status + ", lives size=" + livesArray.size());
                return null;
            }

            JsonNode weatherNode = livesArray.get(0);
            return objectMapper.treeToValue(weatherNode, CurrrentWeatherAmap.class);
        } catch (Exception e) {
            System.err.println("解析天气数据失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("解析天气数据失败", e);
        }
    }

    private Double extractTemperature(CurrrentWeatherAmap weatherData, String cityName) {
        if (weatherData.getTemperatureFloat() != null) {
            return weatherData.getTemperatureFloat().doubleValue();
        } else if (weatherData.getTemperature() != null) {
            try {
                return Double.parseDouble(weatherData.getTemperature());
            } catch (NumberFormatException e) {
                System.err.println("无法解析温度字符串: " + weatherData.getTemperature() + " for city: " + cityName);
            }
        }
        return null;
    }

    private boolean saveAmapWeatherData(String result) {
        try {
            JsonNode rootNode = objectMapper.readTree(result);
            String status = rootNode.path("status").asText();
            JsonNode livesArray = rootNode.path("lives");

            if (!"1".equals(status) || !livesArray.isArray() || livesArray.isEmpty()) {
                System.err.println("API 返回数据状态异常或 lives 数据为空: status=" + status + ", lives size=" + livesArray.size());
                return false;
            }

            for (JsonNode liveNode : livesArray) {
                CurrrentWeatherAmap weatherData = new CurrrentWeatherAmap();
                weatherData.setProvince(liveNode.path("province").asText())
                        .setCity(liveNode.path("city").asText())
                        .setAdcode(liveNode.path("adcode").asText())
                        .setWeather(liveNode.path("weather").asText())
                        .setTemperature(liveNode.path("temperature").asText())
                        .setWindDirection(liveNode.path("winddirection").asText())
                        .setWindPower(liveNode.path("windpower").asText())
                        .setHumidity(liveNode.path("humidity").asText());

                String reportTimeStr = liveNode.path("reporttime").asText();
                if (StringUtils.hasText(reportTimeStr)) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime reportTime = LocalDateTime.parse(reportTimeStr, formatter);
                        weatherData.setReportTime(reportTime);
                    } catch (Exception e) {
                        System.err.println("解析 reporttime 失败: " + reportTimeStr + ", 错误: " + e.getMessage());
                        weatherData.setReportTime(null);
                    }
                }

                String tempFloatStr = liveNode.path("temperature_float").asText();
                if (StringUtils.hasText(tempFloatStr)) {
                    try {
                        weatherData.setTemperatureFloat(Float.valueOf(tempFloatStr));
                    } catch (NumberFormatException e) {
                        System.err.println("解析 temperature_float 失败: " + tempFloatStr + ", 错误: " + e.getMessage());
                        weatherData.setTemperatureFloat(null);
                    }
                }

                String humidFloatStr = liveNode.path("humidity_float").asText();
                if (StringUtils.hasText(humidFloatStr)) {
                    try {
                        weatherData.setHumidityFloat(Float.valueOf(humidFloatStr));
                    } catch (NumberFormatException e) {
                        System.err.println("解析 humidity_float 失败: " + humidFloatStr + ", 错误: " + e.getMessage());
                        weatherData.setHumidityFloat(null);
                    }
                }

                boolean saved = this.save(weatherData);
                if (!saved) {
                    System.err.println("保存单条天气数据到数据库失败: " + weatherData);
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("解析天气API返回的JSON数据失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("保存天气数据到数据库时发生未知错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 检查数据库中是否有天气数据的方法
    private boolean hasWeatherDataInDatabase() {
        try {
            // 方法1：检查记录数量
            Long count = this.count();
            return count != null && count > 0;

            // 方法2：或者检查特定表的数据
            // Dataset<Row> df = readWeatherTable();
            // return df.count() > 0;
        } catch (Exception e) {
            log.warn("Failed to check weather data in database: {}");
            return false;
        }
    }

    private Dataset<Row> readWeatherTable() {
        return sparkSession.read()
                .format("jdbc")
                .option("url", dbUrl)
                .option("dbtable", "currrent_weather_amap")
                .option("user", dbUser)
                .option("password", dbPassword)
                .option("driver", "com.mysql.cj.jdbc.Driver")
                .load();
    }
}