package com.aifleetops.service;

import com.aifleetops.entity.Incident;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IncidentService extends IService<Incident> {

    List<Incident> listActive();

    boolean updateStatus(Long id, String status);
}
