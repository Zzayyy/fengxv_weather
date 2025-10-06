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
 * 天气分析结果表
 * </p>
 *
 * @author Zzay
 * @since 2025-10-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("weather_lishi2025_analysis")
public class WeatherLishi2025Analysis implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 城市名称，NATIONWIDE 表示全国汇总
     */
    private String city;

    /**
     * 晴天数量
     */
    private Long sunnyDays;

    /**
     * 雨天数量
     */
    private Long rainyDays;

    /**
     * 多云/阴天数量
     */
    private Long cloudyDays;

    /**
     * 雪天数量
     */
    private Long snowyDays;

    /**
     * 总记录数
     */
    private Long totalRecords;

    /**
     * 平均最低温度
     */
    private Double avgMinTemp;

    /**
     * 平均最高温度
     */
    private Double avgMaxTemp;

    /**
     * 历史最高温（单日最高）
     */
    private Integer hottestDayMaxTemp;

    /**
     * 历史最低温（单日最低）
     */
    private Integer coldestDayMinTemp;

    /**
     * 白天最常见风力
     */
    private String mostCommonWindDay;

    /**
     * 夜间最常见风力
     */
    private String mostCommonWindNight;

    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;


}
