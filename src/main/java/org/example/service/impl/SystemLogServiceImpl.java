package org.example.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
    @Override
    public PageInfo<SystemLog> getAllLogs(Integer page, Integer size) {
        // 1. 开启分页
        PageHelper.startPage(page, size);

        // 2. 查询所有
        // 🔴 修正点 1：您 Mapper 中的方法名是 selectAll
        List<SystemLog> logs = systemLogMapper.selectAll();

        // 3. 封装返回
        return new PageInfo<>(logs);
    }
}