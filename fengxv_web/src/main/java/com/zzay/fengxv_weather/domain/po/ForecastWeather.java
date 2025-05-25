package com.zzay.fengxv_weather.domain.po;

import java.math.BigDecimal;
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
 * 
 * </p>
 *
 * @author Zzay
 * @since 2025-05-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("forecast_weather")
public class ForecastWeather implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer cityId;

    private Long dt;

    private BigDecimal temp;

    private BigDecimal feelsLike;

    private BigDecimal tempMin;

    private BigDecimal tempMax;

    private Integer pressure;

    private Integer seaLevel;

    private Integer grndLevel;

    private Integer humidity;

    private BigDecimal tempKf;

    private Integer weatherId;

    private String weatherMain;

    private String weatherDescription;

    private String weatherIcon;

    private Integer cloudsAll;

    private BigDecimal windSpeed;

    private Integer windDeg;

    private BigDecimal windGust;

    private Integer visibility;

    private BigDecimal pop;

    private BigDecimal rain_3h;

    private String sysPod;

    private LocalDateTime dtTxt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
