package com.liliya.contract.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ChartService {
    // 获取指定操作类型的合同数量
    Long getTotalByOperationType(Integer type, Date start, Date end);

    // 获取签订金额总数
    Long getTotalFunds(Date start, Date end);

    // 获取部门签订金额排行
    List<Map<String, Object>> getDepartmentRankByFunds(Date start, Date end);

    // 获取部门终结数量排行
    List<Map<String, Object>> getDepartmentRankByEnd(Date start, Date end);
}
