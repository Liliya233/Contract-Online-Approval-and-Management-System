package com.liliya.contract.service;

import com.liliya.contract.model.domain.Contract;
import com.liliya.contract.model.domain.ContractRecord;

import java.util.List;

public interface ContractRecordService {
    // 新增记录
    ContractRecord add(Contract contract, ContractRecord record);

    // 查询按时间排序的所有记录
    List<ContractRecord> getAll(Integer contractId);
}
