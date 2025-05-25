package com.zzay.fengxv_weather.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class CacheUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * 通用缓存读取 + 数据获取 + 自定义保存逻辑
     *
     * @param cacheKey      Redis 缓存键
     * @param apiCall       获取远程数据的方法（如调用 Python 脚本）
     * @param saveFunction  不同的数据保存逻辑（如保存到数据库的不同表）
     * @return              返回结果字符串
     */
    public String getOrFetchWeatherRedis(
            String cacheKey,
            Supplier<String> apiCall,
            Function<String, Boolean> saveFunction) {

        // 1. 尝试读取缓存
        String cached = null;
        try {
            cached = redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
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

        // 4. 使用传入的保存逻辑保存数据
        if (result != null && !saveFunction.apply(result)) {
            throw new RuntimeException("天气数据保存到数据库失败");
        }

        // 5. 尝试写入缓存
        try {
            if (result != null) {
                redisTemplate.opsForValue().set(cacheKey, result, Duration.ofMinutes(10));
            }
        } catch (Exception e) {
            System.err.println("Redis 写入失败：" + e.getMessage());
        }

        return result;
    }
}