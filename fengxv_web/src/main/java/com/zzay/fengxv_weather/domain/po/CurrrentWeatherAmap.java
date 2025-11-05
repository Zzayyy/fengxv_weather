package com.zzay.fengxv_weather.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 高德天气API数据表
 * </p>
 *
 * @author Zzay
 * @since 2025-10-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("currrent_weather_amap")
public class CurrrentWeatherAmap implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 省份名称，如 上海
     */
    private String province;

    /**
     * 城市名称，如 上海市
     */
    private String city;

    /**
     * 行政区划编码，如 310000
     */
    private String adcode;

    /**
     * 天气现象，如 晴
     */
    private String weather;

    /**
     * 温度（字符串格式），如 25
     */
    private String temperature;

    /**
     * 风向，如 东北
     */
    private String windDirection;

    /**
     * 风力，如 ≤3
     */
    private String windPower;

    /**
     * 湿度（百分比字符串），如 73
     */
    private String humidity;

    /**
     * 数据报告时间，如 2025-10-08 19:31:42
     */
    private LocalDateTime reportTime;

    /**
     * 温度（浮点数格式），如 25.0
     */
    private Float temperatureFloat;

    /**
     * 湿度（浮点数格式），如 73.0
     */
    private Float humidityFloat;


}
