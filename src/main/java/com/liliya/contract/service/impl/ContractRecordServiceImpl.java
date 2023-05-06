package com.liliya.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liliya.contract.dao.ContractRecordMapper;
import com.liliya.contract.model.domain.Contract;
import com.liliya.contract.model.domain.ContractRecord;
import com.liliya.contract.model.domain.User;
import com.liliya.contract.service.ContractRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ContractRecordServiceImpl implements ContractRecordService {
    @Resource
    private UserServiceImpl userService;

    @Resource
    private ContractRecordMapper contractRecordMapper;

    @Override
    public ContractRecord add(Contract contract, ContractRecord record) {
        // 获取当前用户
        User user = userService.getAuthentication();
        // 写入操作员ID (当前用户)
        if (user != null){
            record.setOperatorId(user.getId());
        }
        // 写入合同ID
        record.setContractId(contract.getId());
        // 写入当前时间
        record.setTime(new Date(System.currentTimeMillis()));
        // 插入
        if(contractRecordMapper.insert(record) > 0){
            return record;
        }
        return null;
    }

    @Override
    public List<ContractRecord> getAll(Integer contractId) {
        QueryWrapper<ContractRecord> wrapper = new QueryWrapper<>();
        // 查询该合同的记录
        wrapper.eq("record_contract_id", contractId);
        // 根据时间排序
        wrapper.lambda().orderByDesc(ContractRecord::getTime);
        // 查询与返回
        return contractRecordMapper.selectList(wrapper);
    }
}
