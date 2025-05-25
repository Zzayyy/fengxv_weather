package com.zzay.fengxv_weather.service;

import com.zzay.fengxv_weather.domain.dto.GeocodingDTO;
import com.zzay.fengxv_weather.domain.po.AmapGeo;

public interface GeocodingService {
    GeocodingDTO getGeocodingByCityName(String localCityName);

    AmapGeo getGeocodingByCityNameOnAmap(String localCityName);
}
