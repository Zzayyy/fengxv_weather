package com.zzay.fengxv_weather.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentWeatherDTO {
    private Coord coord;
    private List<Weather> weather;
    private Main main;
    private Integer visibility;
    private Wind wind;
    private Clouds clouds;
    private Integer dt;
    private Sys sys;
    private Integer timezone;
    private Integer id;
    private String name;
    private String localName;
}

@Data
class Coord {
    private Double lon;
    private Double lat;
}

@Data
class Weather {
    private Integer id;
    private String main;
    private String description;
    private String icon;
}

@Data
class Main {
    private BigDecimal temp;

    @JsonProperty("feels_like")
    private BigDecimal feelsLike;

    @JsonProperty("temp_min")
    private BigDecimal tempMin;

    @JsonProperty("temp_max")
    private BigDecimal tempMax;

    private Integer pressure;

    private Integer humidity;

    @JsonProperty("sea_level")
    private Integer seaLevel;

    @JsonProperty("grnd_level")
    private Integer grndLevel;
}

@Data
class Wind {
    private BigDecimal speed;
    private Integer deg;
    private BigDecimal gust;
}

@Data
class Clouds {
    private Integer all;
}

@Data
class Sys {
    private String country;
    private Integer sunrise;
    private Integer sunset;
}