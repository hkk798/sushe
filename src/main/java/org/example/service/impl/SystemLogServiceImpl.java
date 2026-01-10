package org.example.service.impl;

import org.example.entity.SystemLog;
import org.example.mapper.SystemLogMapper;
import org.example.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SystemLogServiceImpl implements SystemLogService {

    @Autowired
    private SystemLogMapper systemLogMapper;

    @Override
    public List<SystemLog> getAllLogs() {
        return systemLogMapper.selectAll();
    }

    @Override
    public void recordLog(String operator, String action, String detail, String ip) {
        SystemLog log = new SystemLog(operator, action, detail, ip);
        log.setCreateTime(LocalDateTime.now());
        systemLogMapper.insert(log);
    }
}