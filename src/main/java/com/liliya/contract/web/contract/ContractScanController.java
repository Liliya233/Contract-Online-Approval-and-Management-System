package com.liliya.contract.web.contract;

import com.liliya.contract.model.exception.ContractException;
import com.liliya.contract.model.response.SimpleResponse;
import com.liliya.contract.service.impl.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/contract/scan")
public class ContractScanController {
    @Resource
    private ContractApprovalServiceImpl contractApprovalService;


    @PostMapping("/submit")
    @ResponseBody
    public SimpleResponse<Object> submit(@RequestParam Integer id){
        try {
            // 尝试确认合同扫描版
            contractApprovalService.ScanSubmit(id);
            return SimpleResponse.ok("已确认提交合同扫描版");
        }catch (ContractException e){
            return SimpleResponse.fail(e.getMessage());
        }
    }
}
