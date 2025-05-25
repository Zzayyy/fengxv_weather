package com.zzay.fengxv_weather.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Zzay
 * @since 2025-05-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tencent_weather_forecast_1h")
public class TencentWeatherForecast1h implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @JsonIgnore
    private Long id;

    /**
     * 预报时间，格式：YYYYMMDDHHMMSS
     */
    @JsonProperty("update_time")
    private String forecastTime;

    /**
     * 温度
     */
    private String degree;

    /**
     * 天气描述
     */
    private String weather;

    /**
     * 天气代码
     */
    @JsonProperty("weather_code")
    private String weatherCode;

    /**
     * 简短天气描述
     */
    @JsonProperty("weather_short")
    private String weatherShort;

    /**
     * 天气图标URL（可为空）
     */
    @JsonProperty("weather_url")
    private String weatherUrl;

    /**
     * 风向
     */
    @JsonProperty("wind_direction")
    private String windDirection;

    /**
     * 风力等级
     */
    @JsonProperty("wind_power")
    private String windPower;

    /**
     * 省份
     */
    @JsonIgnore
    private String province;

    /**
     * 城市
     */
    @JsonIgnore
    private String city;

    private LocalDateTime updateTime = LocalDateTime.now();


}
