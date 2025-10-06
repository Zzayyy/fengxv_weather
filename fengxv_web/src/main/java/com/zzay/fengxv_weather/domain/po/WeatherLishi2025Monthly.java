package com.zzay.fengxv_weather.domain.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 每月天气分析结果表
 * </p>
 *
 * @author Zzay
 * @since 2025-10-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("weather_lishi2025_monthly")
public class WeatherLishi2025Monthly implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String cityPlace;

    /**
     * 年月格式：YYYY-MM
     */
    private String yearsMonth;

    /**
     * 月最低温度
     */
    private Integer minTempMonth;

    /**
     * 月最高温度
     */
    private Integer maxTempMonth;

    /**
     * 月平均温度
     */
    private BigDecimal avgTempMonth;

    /**
     * 该月总天数
     */
    private Integer  totalDays;

    /**
     * 该月有效数据条数
     */
    private Integer  dataCount;

    @JsonIgnore // 防止在API响应中返回创建时间
    private LocalDateTime createdAt;

    @JsonIgnore // 防止在API响应中返回创建时间
    private LocalDateTime updatedAt;


}
