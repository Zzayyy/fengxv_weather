package com.zzay.fengxv_weather.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WeatherLishi2025MonthlyDTO {
    
    /**
     * 年月 (YYYY-MM)
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
     * 数据记录数
     */
    private Long dataCount;
    
    /**
     * 总天数
     */
    private Long totalDays;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * 地区标识
     */
    private String cityPlace;
}