package com.liliya.contract.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.liliya.contract.model.vo.ContractRecordVo;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

public interface ContractRecordVoMapper extends BaseMapper<ContractRecordVo> {
    Long getTotal(@Param(Constants.WRAPPER) QueryWrapper<ContractRecordVo> wrapper);

    Long getTotalFunds(@Param(Constants.WRAPPER) QueryWrapper<ContractRecordVo> wrapper);

    IPage<Map<String, Object>> getDepartmentRankByFunds(IPage<Map<String, Object>> page, @Param(Constants.WRAPPER) QueryWrapper<ContractRecordVo> wrapper);

    IPage<Map<String, Object>> getDepartmentRankByOperationType(IPage<Map<String, Object>> page, @Param(Constants.WRAPPER) QueryWrapper<ContractRecordVo> wrapper);
}
