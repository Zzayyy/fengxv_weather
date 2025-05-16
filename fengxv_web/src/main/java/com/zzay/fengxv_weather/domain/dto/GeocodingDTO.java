package com.zzay.fengxv_weather.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeocodingDTO {
    @JsonProperty("name")
    private String name;
    private String localName;
    private Double lat;
    private Double lon;
}