package com.liliya.contract;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liliya.contract.dao.ContractRecordVoMapper;
import com.liliya.contract.model.vo.ContractRecordVo;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

@SpringBootTest
class ContractApplicationTests {

    @Resource
    private ContractRecordVoMapper contractRecordVoMapper;

    @Test
    void Test() throws ParseException {
        QueryWrapper<ContractRecordVo> wrapper = new QueryWrapper<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


        wrapper.ge("record_time", simpleDateFormat.parse("2001-01-01 12:00:00"));
        wrapper.le("record_time", simpleDateFormat.parse("2020-01-01 12:00:00"));

        wrapper.eq("record_operation_type", 4);

        wrapper.groupBy("department_id");

        wrapper.orderByDesc("su");


        Page<Map<String, Object>> queryPage = new Page<>(1, 5);

        queryPage.setOptimizeJoinOfCountSql(false);

        //IPage<ContractRecordVo> result = contractRecordVoMapper.getTotalFunds(wrapper);

        //result.getRecords().forEach(System.out::println);

        System.out.println(contractRecordVoMapper.getDepartmentRankByOperationType(queryPage, wrapper).getRecords());
    }

}
