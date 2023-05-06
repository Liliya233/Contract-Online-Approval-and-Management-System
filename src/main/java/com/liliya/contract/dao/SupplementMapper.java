package com.liliya.contract.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.liliya.contract.model.domain.Supplement;
import org.apache.ibatis.annotations.Param;

public interface SupplementMapper extends BaseMapper<Supplement> {
    IPage<Supplement> findPageWithContractAndUserInfo(IPage<Supplement> page, @Param(Constants.WRAPPER) QueryWrapper<Supplement> wrapper);
}
