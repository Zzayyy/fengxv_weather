package com.zzay.fengxv_weather.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zzay.fengxv_weather.domain.dto.WeatherLishiAnalysisDTO;
import com.zzay.fengxv_weather.domain.po.WeatherLishi2025;
import com.zzay.fengxv_weather.domain.po.WeatherLishi2025Analysis;
import com.zzay.fengxv_weather.domain.po.WeatherLishi2025Monthly;
import com.zzay.fengxv_weather.mapper.WeatherLishi2025AnalysisMapper;
import com.zzay.fengxv_weather.mapper.WeatherLishi2025Mapper;
import com.zzay.fengxv_weather.service.IWeatherLishi2025Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Zzay
 * @since 2025-10-03
 */
@Service
@Slf4j
public class WeatherLishi2025ServiceImpl extends ServiceImpl<WeatherLishi2025Mapper, WeatherLishi2025> implements IWeatherLishi2025Service {

    private static final String NATIONWIDE_KEY = "NATIONWIDE";

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private WeatherLishi2025AnalysisMapper analysisResultMapper;

    // 锁：防止并发重复计算（key = city 或 NATIONWIDE）
    private final Map<String, Object> computeLocks = new ConcurrentHashMap<>();

    // =============== 对外接口 ===============

    @Override
    public WeatherLishiAnalysisDTO analyzeWeatherByCity(String city) {
        WeatherLishi2025Analysis result = getOrComputeAnalysis(city, () -> {
            Dataset<Row> df = readWeatherTable()
                    .filter(functions.col("city_place").equalTo(city));
            return doSparkAnalysis(df);
        });
        return toDTO(result);
    }
    @Override
    public WeatherLishiAnalysisDTO analyzeWeatherAllPlace() {
        WeatherLishi2025Analysis result = getOrComputeAnalysis(NATIONWIDE_KEY, () -> {
            Dataset<Row> df = readWeatherTable(); // 全表
            return doSparkAnalysis(df);
        });
        return toDTO(result);
    }


    // =============== 核心通用逻辑 ===============

    /**
     * 获取或计算分析结果（带缓存 + 并发控制）
     *
     * @param cacheKey    缓存键（city 名 或 NATIONWIDE）
     * @param analyzer    分析逻辑（返回 WeatherLishiAnalysisDTO）
     * @return 存入数据库后的实体
     */
    private WeatherLishi2025Analysis getOrComputeAnalysis(String cacheKey, Supplier<WeatherLishiAnalysisDTO> analyzer) {
        // 1. 尝试从 DB 获取
        WeatherLishi2025Analysis cached = analysisResultMapper.selectByCity(cacheKey);
        if (isFresh(cached)) {
            return cached;
        }

        // 2. 加锁防止并发重复计算
        synchronized (computeLocks.computeIfAbsent(cacheKey, k -> new Object())) {
            // 双重检查
            cached = analysisResultMapper.selectByCity(cacheKey);
            if (isFresh(cached)) {
                return cached;
            }

            // 3. 执行分析
            WeatherLishiAnalysisDTO dto = analyzer.get();

            // 4. 保存到 DB
            WeatherLishi2025Analysis entity = convertToEntity(dto, cacheKey);
            entity.setUpdatedAt(LocalDateTime.now());

            if (cached != null) {
                entity.setId(cached.getId());
                analysisResultMapper.updateById(entity);
            } else {
                analysisResultMapper.insert(entity);
            }

            return entity;
        }
    }

    /**
     * 判断缓存是否“新鲜”：
     * - 全国分析：必须是今天
     * - 城市分析：只要存在即可（或也可加过期策略）
     */
    private boolean isFresh(WeatherLishi2025Analysis result) {
        if (result == null) return false;
        if (NATIONWIDE_KEY.equals(result.getCity())) {
            return result.getUpdatedAt().toLocalDate().equals(LocalDate.now());
        }
        // 城市分析：永久有效（或可改为 24 小时内有效）
        return true;
    }

    // =============== Spark 分析核心 ===============

    private Dataset<Row> readWeatherTable() {
        return sparkSession.read()
                .format("jdbc")
                .option("url", dbUrl)
                .option("dbtable", "weather_lishi2025")
                .option("user", dbUser)
                .option("password", dbPassword)
                .option("driver", "com.mysql.cj.jdbc.Driver")
                .load();
    }


    // 注册UDF
    @PostConstruct
    public void initUDFs() {
        sparkSession.udf().register("categorizeWeather",
                (UDF1<String, String>) weather -> {
                    if (weather == null) return "other";
                    if (weather.contains("雨")) return "rainy";
                    if (weather.contains("雪")) return "snowy";
                    if (weather.contains("晴")) return "sunny";
                    if (weather.contains("云") || weather.contains("阴")) return "cloudy";
                    return "other";
                },
                DataTypes.StringType
        );
        log.info("Registered Spark UDF: categorizeWeather");
    }

    /**
     * 对给定的 DataFrame 执行统一分析逻辑
     */
    private WeatherLishiAnalysisDTO doSparkAnalysis(Dataset<Row> df) {

        Dataset<Row> dfWithCategory = df.withColumn("weather_category",
                functions.callUDF("categorizeWeather", functions.col("weather_day")));

        // 天数统计
        WeatherLishiAnalysisDTO dto = new WeatherLishiAnalysisDTO();
        dto.setSunnyDays(countByCategory(dfWithCategory, "sunny"));
        dto.setRainyDays(countByCategory(dfWithCategory, "rainy"));
        dto.setCloudyDays(countByCategory(dfWithCategory, "cloudy"));
        dto.setSnowyDays(countByCategory(dfWithCategory, "snowy"));
        dto.setTotalRecords(df.count());

        // 温度统计
        dto.setAvgMinTemp(df.agg(functions.avg("min_temp")).first().getDouble(0));
        dto.setAvgMaxTemp(df.agg(functions.avg("max_temp")).first().getDouble(0));
        dto.setHottestDayMaxTemp(df.agg(functions.max("max_temp")).first().getInt(0));
        dto.setColdestDayMinTemp(df.agg(functions.min("min_temp")).first().getInt(0));

        dto.setMostCommonWindDay(getMode(df, "wind_day"));
        dto.setMostCommonWindNight(getMode(df, "wind_night"));

        return dto;
    }

    // =============== 工具方法 ===============


    // 天数统计
    private long countByCategory(Dataset<Row> df, String category) {
        return df.filter(functions.col("weather_category").equalTo(category)).count();
    }

    // 出现最频繁的风向
    private String getMode(Dataset<Row> df, String column) {
        try {
            return df.groupBy(column)
                    .count()
                    .orderBy(functions.col("count").desc())
                    .first()
                    .getString(0);
        } catch (Exception e) {
            return "unknown";
        }
    }


    // =============== 转换方法 ===============


    // 分析完的数据转换成数据库实体
    private WeatherLishi2025Analysis convertToEntity(WeatherLishiAnalysisDTO dto, String city) {
        WeatherLishi2025Analysis e = new WeatherLishi2025Analysis();
        e.setCity(city);
        e.setSunnyDays(dto.getSunnyDays());
        e.setRainyDays(dto.getRainyDays());
        e.setCloudyDays(dto.getCloudyDays());
        e.setSnowyDays(dto.getSnowyDays());
        e.setTotalRecords(dto.getTotalRecords());
        e.setAvgMinTemp(dto.getAvgMinTemp());
        e.setAvgMaxTemp(dto.getAvgMaxTemp());
        e.setHottestDayMaxTemp(dto.getHottestDayMaxTemp());
        e.setColdestDayMinTemp(dto.getColdestDayMinTemp());
        e.setMostCommonWindDay(dto.getMostCommonWindDay());
        e.setMostCommonWindNight(dto.getMostCommonWindNight());
        return e;
    }

    // 实体转DTO
    private WeatherLishiAnalysisDTO toDTO(WeatherLishi2025Analysis e) {
        WeatherLishiAnalysisDTO dto = new WeatherLishiAnalysisDTO();
        BeanUtils.copyProperties(e, dto);
        return dto;
    }


    // =============== 定时任务 ===============

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void scheduledFullAnalysis() {
        log.info("开始定时全量天气分析...");

        // 1. 获取所有城市
        Dataset<Row> fullDf = readWeatherTable();
        List<String> cities = fullDf.select("city_place").distinct().as(Encoders.STRING()).collectAsList();

        // 2. 清空旧数据
        analysisResultMapper.delete(new QueryWrapper<>());

        // 3. 分析每个城市
        for (String city : cities) {
            try {
                Dataset<Row> cityDf = fullDf.filter(functions.col("city_place").equalTo(city));
                WeatherLishiAnalysisDTO dto = doSparkAnalysis(cityDf);
                WeatherLishi2025Analysis entity = convertToEntity(dto, city);
                entity.setUpdatedAt(LocalDateTime.now());
                analysisResultMapper.insert(entity);
                log.info("城市 {} 分析完成", city);
            } catch (Exception e) {
                log.error("分析城市 {} 失败", city, e);
            }
        }

        // 4. 分析全国
        try {
            WeatherLishiAnalysisDTO nationalDto = doSparkAnalysis(readWeatherTable());
            WeatherLishi2025Analysis nationalEntity = convertToEntity(nationalDto, NATIONWIDE_KEY);
            nationalEntity.setUpdatedAt(LocalDateTime.now());
            analysisResultMapper.insert(nationalEntity);
            log.info("全国分析完成");
        } catch (Exception e) {
            log.error("全国分析失败", e);
        }

        log.info("定时全量分析完成，共处理 {} 个城市 + 全国汇总", cities.size());
    }
}
