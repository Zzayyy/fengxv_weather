package com.zzay.fengxv_weather.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
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
 * @since 2025-05-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tencent_weather_forecast_24h")
public class TencentWeatherForecast24h implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value = "id", type = IdType.AUTO)
    @JsonIgnore
    private Long id;

    /**
     * 预报日期
     */
    @JsonProperty("time")
    private LocalDate forecastDate;

    /**
     * 白天天气描述
     */
    @JsonProperty("day_weather")
    private String dayWeather;

    /**
     * 白天天气代码
     */
    @JsonProperty("day_weather_code")
    private String dayWeatherCode;

    /**
     * 白天简短天气描述
     */
    @JsonProperty("day_weather_short")
    private String dayWeatherShort;

    /**
     * 白天天气图标URL
     */
    @JsonProperty("day_weather_url")
    private String dayWeatherUrl;

    /**
     * 白天风向
     */
    @JsonProperty("day_wind_direction")
    private String dayWindDirection;

    /**
     * 白天风向代码
     */
    @JsonProperty("day_wind_direction_code")
    private String dayWindDirectionCode;

    /**
     * 白天风力等级
     */
    @JsonProperty("day_wind_power")
    private String dayWindPower;

    /**
     * 白天风力代码
     */
    @JsonProperty("day_wind_power_code")
    private String dayWindPowerCode;

    /**
     * 晚上天气描述
     */
    @JsonProperty("night_weather")
    private String nightWeather;

    /**
     * 晚上天气代码
     */
    @JsonProperty("night_weather_code")
    private String nightWeatherCode;

    /**
     * 晚上简短天气描述
     */
    @JsonProperty("night_weather_short")
    private String nightWeatherShort;

    /**
     * 晚上天气图标URL
     */
    @JsonProperty("night_weather_url")
    private String nightWeatherUrl;

    /**
     * 晚上风向
     */
    @JsonProperty("night_wind_direction")
    private String nightWindDirection;

    /**
     * 晚上风向代码
     */
    @JsonProperty("night_wind_direction_code")
    private String nightWindDirectionCode;

    /**
     * 晚上风力等级
     */
    @JsonProperty("night_wind_power")
    private String nightWindPower;

    /**
     * 晚上风力代码
     */
    @JsonProperty("night_wind_power_code")
    private String nightWindPowerCode;

    /**
     * 最高温度
     */
    @JsonProperty("max_degree")
    private String maxDegree;

    /**
     * 最低温度
     */
    @JsonProperty("min_degree")
    private String minDegree;

    /**
     * 空气质量等级
     */
    @JsonProperty("aqi_level")
    private Integer aqiLevel;

    /**
     * 空气质量名称
     */
    @JsonProperty("aqi_name")
    private String aqiName;

    /**
     * 空气质量详情链接
     */
    @JsonProperty("aqi_url")
    private String aqiUrl;

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

    /**
     * 记录创建时间
     */
    @JsonIgnore
    private LocalDateTime createTime = LocalDateTime.now();

    /**
     * 最后更新时间
     */
    @JsonIgnore
    private LocalDateTime updateTime = LocalDateTime.now();


}
