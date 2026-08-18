package com.aifleetops.service.impl;

import com.aifleetops.entity.Incident;
import com.aifleetops.mapper.IncidentMapper;
import com.aifleetops.service.IncidentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class IncidentServiceImpl extends ServiceImpl<IncidentMapper, Incident> implements IncidentService {

    @Override
    public List<Incident> listActive() {
        return lambdaQuery()
                .notIn(Incident::getStatus, Arrays.asList("RESOLVED", "REJECTED"))
                .list();
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        var update = lambdaUpdate()
                .eq(Incident::getId, id)
                .set(Incident::getStatus, status);
        // 状态置为 RESOLVED 时自动记录解决时间，其他状态不覆盖已有 resolvedAt
        if ("RESOLVED".equals(status)) {
            update.set(Incident::getResolvedAt, LocalDateTime.now());
        }
        return update.update();
    }
}
