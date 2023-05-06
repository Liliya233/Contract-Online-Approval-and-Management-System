package com.liliya.contract.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.liliya.contract.model.domain.Contract;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractMapper extends BaseMapper<Contract> {
    IPage<Contract> findPageWithUserInfo(IPage<Contract> page, @Param(Constants.WRAPPER) QueryWrapper<Contract> wrapper);
}
