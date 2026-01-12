package org.example.service;

import org.example.entity.SystemLog;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface SystemLogService {
    List<SystemLog> getAllLogs();
    void recordLog(String operator, String action, String detail, String ip);

    PageInfo<SystemLog> getAllLogs(Integer page, Integer size);
}
