package com.aifleetops.service.impl;

import com.aifleetops.entity.DailyForecast;
import com.aifleetops.mapper.DailyForecastMapper;
import com.aifleetops.service.DailyForecastService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DailyForecastServiceImpl extends ServiceImpl<DailyForecastMapper, DailyForecast> implements DailyForecastService {
}
