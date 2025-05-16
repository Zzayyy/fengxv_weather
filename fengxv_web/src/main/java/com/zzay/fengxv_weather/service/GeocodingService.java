package com.zzay.fengxv_weather.service;

import com.zzay.fengxv_weather.domain.dto.GeocodingDTO;

public interface GeocodingService {
    GeocodingDTO getGeocodingByCityName(String localCityName);
}
