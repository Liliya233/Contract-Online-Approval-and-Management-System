package com.liliya.contract.service;

import com.liliya.contract.model.domain.Supplement;
import com.liliya.contract.model.domain.SupplementRecord;

import java.util.List;

public interface SupplementRecordService {
    // 新增记录
    SupplementRecord add(Supplement supplement, SupplementRecord record);

    // 查询按时间排序的所有记录
    List<SupplementRecord> getAll(Integer supplementId);
}
