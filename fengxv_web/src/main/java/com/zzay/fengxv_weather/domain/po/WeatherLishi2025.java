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
 * 
 * </p>
 *
 * @author Zzay
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("weather_lishi2025")
public class WeatherLishi2025 implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String cityPlace;

    private String dateStr;

    private String weatherDay;

    private String weatherNight;

    private Integer minTemp;

    private Integer maxTemp;

    private String windDay;

    private String windNight;

    private LocalDateTime createdAt;


}
