package com.liliya.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liliya.contract.dao.SupplementRecordMapper;
import com.liliya.contract.model.domain.Supplement;
import com.liliya.contract.model.domain.SupplementRecord;
import com.liliya.contract.model.domain.User;
import com.liliya.contract.service.SupplementRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class SupplementRecordServiceImpl implements SupplementRecordService {
    @Resource
    private UserServiceImpl userService;

    @Resource
    private SupplementRecordMapper supplementRecordMapper;

    @Override
    public SupplementRecord add(Supplement supplement, SupplementRecord record) {
        User user = userService.getAuthentication();
        // 写入操作员ID (当前用户)
        if (user != null){
            record.setOperatorId(user.getId());
        }
        // 写入合同ID
        record.setSupplementId(supplement.getId());
        // 写入当前时间
        record.setTime(new Date(System.currentTimeMillis()));
        // 插入
        if(supplementRecordMapper.insert(record) > 0){
            return record;
        }
        return null;
    }

    @Override
    public List<SupplementRecord> getAll(Integer supplementId) {
        QueryWrapper<SupplementRecord> wrapper = new QueryWrapper<>();
        // 查询该合同的记录
        wrapper.eq("record_supplement_id", supplementId);
        // 根据时间排序
        wrapper.lambda().orderByDesc(SupplementRecord::getTime);
        // 查询与返回
        return supplementRecordMapper.selectList(wrapper);
    }
}
