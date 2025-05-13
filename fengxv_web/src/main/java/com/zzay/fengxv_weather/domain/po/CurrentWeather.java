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
 * @since 2025-05-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("current_weather")
//@ApiModel(value="CurrentWeather对象", description="")
public class CurrentWeather implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Integer cityId;

    private String weatherMain;

    private String weatherDescription;

    private String icon;

    private BigDecimal temp;

    private BigDecimal feelsLike;

    private BigDecimal tempMin;

    private BigDecimal tempMax;

    private Integer pressure;

    private Integer humidity;

    private Integer seaLevel;

    private Integer grndLevel;

    private Integer visibility;

    private BigDecimal windSpeed;

    private Integer windDeg;

    private BigDecimal windGust;

    private Integer cloudAll;

    private Integer dt;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
