package com.zzay.fengxv_weather.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zzay.fengxv_weather.domain.dto.HistoryByCityDTO;
import com.zzay.fengxv_weather.domain.dto.HistoryWeatherDTO;

import java.util.List;

public interface IHistoryByCityService {
    List<HistoryByCityDTO> getHistoryWeatherByCity(String city);

    List<HistoryWeatherDTO> getMaxMinTempByMonth(String city);
}
