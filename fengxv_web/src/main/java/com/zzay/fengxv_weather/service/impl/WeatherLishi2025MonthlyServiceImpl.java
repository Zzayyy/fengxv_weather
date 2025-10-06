package com.zzay.fengxv_weather.service.impl;

import com.zzay.fengxv_weather.domain.dto.WeatherLishi2025MonthlyDTO;
import com.zzay.fengxv_weather.domain.po.WeatherLishi2025Monthly;
import com.zzay.fengxv_weather.mapper.WeatherLishi2025MonthlyMapper;
import com.zzay.fengxv_weather.service.IWeatherLishi2025MonthlyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 每月天气分析结果表 服务实现类
 * </p>
 *
 * @author Zzay
 * @since 2025-10-06
 */
@Service
public class WeatherLishi2025MonthlyServiceImpl extends ServiceImpl<WeatherLishi2025MonthlyMapper, WeatherLishi2025Monthly> implements IWeatherLishi2025MonthlyService {

    @Value("${spring.datasource.url}")
    private String dbUrl;
    @Value("${spring.datasource.username}")
    private String dbUser;
    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private WeatherLishi2025MonthlyMapper monthlyMapper;

    // 锁：防止并发重复计算
    private final Map<String, Object> computeLocks = new ConcurrentHashMap<>();

    // =============== 对外接口 ===============

    @Override
    public List<WeatherLishi2025Monthly> getMonthlyTemperatureNationwide() {
        String cacheKey = "NATIONWIDE_MONTHLY";

        List<WeatherLishi2025Monthly> cached = getFromCache(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        // 加锁防止并发重复计算
        synchronized (computeLocks.computeIfAbsent(cacheKey, k -> new Object())) {
            // 双重检查
            cached = getFromCache(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                return cached;
            }

            // 执行分析
            List<WeatherLishi2025Monthly> results = doMonthlyAnalysis();

            // 保存到 DB（先删除旧数据，再插入新数据）
//            monthlyMapper.deleteByCityPlace("NATIONWIDE"); // 假设用city_place作为标识
            for (WeatherLishi2025Monthly entity : results) {
                entity.setCityPlace("NATIONWIDE");
                entity.setCreatedAt(LocalDateTime.now());
                entity.setUpdatedAt(LocalDateTime.now());
                monthlyMapper.insert(entity);
            }

            return results;
        }
    }

    // =============== 核心逻辑 ===============

    /**
     * 从缓存获取数据
     */
    private List<WeatherLishi2025Monthly> getFromCache(String cacheKey) {
        return monthlyMapper.selectByCityPlace("NATIONWIDE");
    }

    /**
     * Spark 分析核心逻辑
     */
    private List<WeatherLishi2025Monthly> doMonthlyAnalysis() {
        Dataset<Row> df = readWeatherTable();

        // 注册UDF来提取年月
        sparkSession.udf().register("extractYearMonth",
                (UDF1<String, String>) dateStr -> {
                    if (dateStr == null || dateStr.length() < 7) return null;
                    return dateStr.substring(0, 7); // YYYY-MM
                },
                DataTypes.StringType
        );

        // 添加年月列
        Dataset<Row> dfWithMonth = df.withColumn("years_month",
                functions.callUDF("extractYearMonth", functions.col("date_str")));

        // 按年月分组进行统计
        Dataset<Row> grouped = dfWithMonth
                .filter(functions.col("years_month").isNotNull())
                .groupBy("years_month")
                .agg(
                        functions.min("min_temp").alias("min_temp_month"),
                        functions.max("max_temp").alias("max_temp_month"),
                        functions.avg(functions.col("min_temp").plus(functions.col("max_temp")).divide(2.0)).alias("avg_temp_month"),
                        functions.count("*").alias("data_count"),
                        functions.count(functions.lit(1)).alias("total_days")
                )
                .orderBy("years_month");

        // 转换为实体列表 - 简化转换逻辑
        List<WeatherLishi2025Monthly> results = new ArrayList<>();
        grouped.toJavaRDD().collect().forEach(row -> {
            WeatherLishi2025Monthly monthly = new WeatherLishi2025Monthly();
            monthly.setYearsMonth(row.getAs("years_month"));

            // 直接安全转换为 Integer
            monthly.setMinTempMonth(getAsInteger(row, "min_temp_month"));
            monthly.setMaxTempMonth(getAsInteger(row, "max_temp_month"));
            monthly.setAvgTempMonth(getAsBigDecimal(row, "avg_temp_month"));
            monthly.setTotalDays(getAsInteger(row, "total_days"));
            monthly.setDataCount(getAsInteger(row, "data_count"));

            monthly.setCreatedAt(LocalDateTime.now());
            monthly.setUpdatedAt(LocalDateTime.now());
            results.add(monthly);
        });

        return results;
    }

    // 辅助方法
    private Integer getAsInteger(Row row, String columnName) {
        Object obj = row.getAs(columnName);
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        return null;
    }

    private BigDecimal getAsBigDecimal(Row row, String columnName) {
        Object obj = row.getAs(columnName);
        if (obj instanceof Number) {
            return BigDecimal.valueOf(((Number) obj).doubleValue());
        }
        return null;
    }

    // =============== 工具方法 ===============

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
}
