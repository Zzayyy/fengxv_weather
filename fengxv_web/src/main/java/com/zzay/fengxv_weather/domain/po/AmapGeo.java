package com.zzay.fengxv_weather.domain.po;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmapGeo {
    @JsonProperty("formatted_address")
    private String formattedAddress;
    private String country;
    private String province;
    @JsonProperty("citycode")
    private String cityCode;
    private String city;
    private String location;
}
