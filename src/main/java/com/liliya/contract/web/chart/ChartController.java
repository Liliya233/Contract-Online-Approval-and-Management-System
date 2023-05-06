package com.liliya.contract.web.chart;

import com.liliya.contract.model.define.OperationType;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.ChartServiceImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("/chart")
public class ChartController {
    @Resource
    private ChartServiceImpl chartService;

    @GetMapping
    public String web() {
        return "chart";
    }

    @PostMapping("/get")
    @ResponseBody
    public SimpleResponse<Object> get(@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                       @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate) {
        Map<String, Object> result = new HashMap<>();
        // 合同签订数量
        result.put("contractRecord", chartService.getTotalByOperationType(OperationType.RECORD_ALLOW, startDate, endDate));
        // 合同签订金额
        result.put("contractFunds", chartService.getTotalFunds(startDate, endDate));
        // 协议签订数量
        result.put("supplementRecord", chartService.getTotalByOperationType(OperationType.CREATE_SUPPLEMENT, startDate, endDate));
        // 合同签订金额
        result.put("contractEnd", chartService.getTotalByOperationType(OperationType.END_ALLOW, startDate, endDate));
        // 合同终结数量
        List<Map<String, Object>> fundsRank = chartService.getDepartmentRankByFunds(startDate, endDate);
        if (fundsRank != null && !fundsRank.isEmpty()){
            List<Object> departmentNames = new ArrayList<>();
            List<Object> funds = new ArrayList<>();
            fundsRank.forEach(map -> {
                departmentNames.add(map.get("departmentName"));
                funds.add(map.get("funds"));
            });
            result.put("fundsRank", Map.of(
                    "departmentNames", departmentNames,
                    "funds", funds
            ));
        }
        // 部门签订金额排行
        List<Map<String, Object>> endRank = chartService.getDepartmentRankByEnd(startDate, endDate);
        if (endRank != null && !endRank.isEmpty()){
            List<Object> departmentNames = new ArrayList<>();
            List<Object> ends = new ArrayList<>();
            endRank.forEach(map -> {
                departmentNames.add(map.get("departmentName"));
                ends.add(map.get("sum"));
            });
            result.put("endRank", Map.of(
                    "departmentNames", departmentNames,
                    "ends", ends
            ));
        }
        return SimpleResponse.ok("成功返回", result);
    }
}
