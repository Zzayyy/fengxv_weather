package com.zzay.fengxv_weather.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzay.fengxv_weather.config.SparkConfig;
import com.zzay.fengxv_weather.domain.dto.HistoryByCityDTO;
import com.zzay.fengxv_weather.domain.dto.HistoryWeatherDTO;
import com.zzay.fengxv_weather.domain.po.AmapGeo;
import com.zzay.fengxv_weather.service.IHistoryByCityService;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import static org.apache.spark.sql.functions.*;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class HistoryByCityServiceImpl implements IHistoryByCityService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SparkSession sparkSession;

    private final RestTemplate restTemplate = new RestTemplate();


    @Override
    public List<HistoryByCityDTO> getHistoryWeatherByCity(String city) {
        try {
            String redisKey = "weather:" + city + ":2025";

            // 先从Redis获取数据
            String jsonData = redisTemplate.opsForValue().get(redisKey);

            if (jsonData != null && !jsonData.trim().isEmpty()) {
                // 如果Redis中有数据，直接返回
                return objectMapper.readValue(
                        jsonData,
                        new TypeReference<List<HistoryByCityDTO>>() {
                        }
                );
            }

            // 如果Redis中没有数据，请求FastAPI获取数据
            String url = String.format(
                    "http://0.0.0.0:8000/weather/history/byCity?city=%s",
                    city
            );

            String response = restTemplate.getForObject(url, String.class);

            // 将从FastAPI获取的数据存入Redis（设置过期时间，比如24小时）
            redisTemplate.opsForValue().set(redisKey, response, Duration.ofHours(24));

            // 将响应数据转换为对象列表并返回
            return objectMapper.readValue(
                    response,
                    new TypeReference<List<HistoryByCityDTO>>() {
                    }
            );

        } catch (Exception e) {
            throw new RuntimeException("获取天气数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HistoryWeatherDTO> getMaxMinTempByMonth(String city) {
        try {


        String redisKey = "weather:" + city + ":2025历史";
        String redisKey2 = "weather:" + city + ":历史Spark分析";

        // 先从Redis获取数据
        String jsonData2 = redisTemplate.opsForValue().get(redisKey2);

        if (jsonData2 != null && !jsonData2.trim().isEmpty()) {
            // 如果Redis中有数据，直接返回
            return objectMapper.readValue(
                    jsonData2,
                    new TypeReference<List<HistoryWeatherDTO>>() {
                    }
            );
        }


        // 先从Redis获取数据
        String jsonData = redisTemplate.opsForValue().get(redisKey);
        List<HistoryByCityDTO> historyByCityDTOSList = objectMapper.readValue(
                jsonData,
                new TypeReference<List<HistoryByCityDTO>>() {
                }
        );
        // 4. Convert to Dataset<Row> for Spark
        Dataset<Row> df = sparkSession.createDataFrame(historyByCityDTOSList, HistoryByCityDTO.class);

        // 5. Extract month from "2025年08月01日" → use regexp or udf
        // We'll use a UDF to parse month from Chinese date format
        sparkSession.udf().register("extractMonth", (UDF1<String, Integer>) dateStr -> {
            if (dateStr == null) return null;
            // Example: "2025年08月01日" → extract "08"
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("年(\\d{2})月");
            java.util.regex.Matcher m = p.matcher(dateStr);
            if (m.find()) {
                return Integer.valueOf(m.group(1));
            }
            return null;
        }, DataTypes.IntegerType);

        Dataset<Row> dfWithMonth = df.withColumn("month", callUDF("extractMonth", col("date")));

        // 6. Cast temp strings to integers
        Dataset<Row> dfTyped = dfWithMonth
                .withColumn("minTempInt", col("minTemp").cast(DataTypes.IntegerType))
                .withColumn("maxTempInt", col("maxTemp").cast(DataTypes.IntegerType));

        // 7. Aggregate by month
        Dataset<Row> resultDF = dfTyped
                .groupBy("month")
                .agg(
                        max("maxTempInt").alias("maxTemp"),
                        min("minTempInt").alias("minTemp")
                )
                .filter(col("month").isNotNull())
                .orderBy("month");

        // 8. Convert to List<HistoryWeatherDTO>
        List<HistoryWeatherDTO> result = new ArrayList<>();
        for (Row row : resultDF.collectAsList()) {
            HistoryWeatherDTO dto = new HistoryWeatherDTO();
            dto.setCity(city);
            dto.setMonth(row.getInt(0));           // month
            dto.setMaxTemp(row.getInt(1));         // maxTemp
            dto.setMinTemp(row.getInt(2));         // minTemp
            result.add(dto);
        }
// 9. Cache result in Redis
        String resultJson = objectMapper.writeValueAsString(result);
        redisTemplate.opsForValue().set(redisKey2, resultJson, Duration.ofHours(24));

        return result;
        } catch (Exception e) {
            throw new RuntimeException("获取天气数据失败: " + e.getMessage(), e);
        }
    }
}