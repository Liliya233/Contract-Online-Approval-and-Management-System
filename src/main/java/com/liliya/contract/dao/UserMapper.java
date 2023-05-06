package com.liliya.contract.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liliya.contract.model.domain.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper extends BaseMapper<User> { }
