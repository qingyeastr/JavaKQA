package com.qingyun.systemconfig.service;

import com.qingyun.systemconfig.model.OperationLog;
import com.qingyun.systemconfig.repository.OperationLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;

    public OperationLogService(OperationLogRepository operationLogRepository) {
        this.operationLogRepository = operationLogRepository;
    }

    public void record(Long operatorId, String moduleName, String operationType, String operationContent) {
        operationLogRepository.insert(operatorId, moduleName, operationType, operationContent, LocalDateTime.now());
    }

    public List<OperationLog> listRecentLogs() {
        return operationLogRepository.findRecent(50);
    }
}